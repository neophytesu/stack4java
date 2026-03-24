package http;


import http.base.HttpRequest;
import http.base.HttpResponse;
import http.base.HttpSession;
import http.dispatch.RequestDispatcher;
import http.dispatch.RequestDispatcherImpl;
import http.filter.FilterChain;
import http.filter.FilterChainImpl;
import http.filter.HttpFilter;
import http.servlet.HttpServlet;
import http.servlet.NotFoundServlet;
import http.servlet.ServerErrorServlet;
import http.servlet.StaticResourceServlet;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpServer {

    private final HttpServletContext servletContext = new HttpServletContext();

    private final int port;

    private final List<HttpServlet> initServletOrders = new ArrayList<>();


    private final List<HttpFilter> filters = new ArrayList<>();

    private final HttpHelper httpHelper = new HttpHelper();

    private final UrlMappingRegistry urlMappingRegistry = new UrlMappingRegistry();

    private final ConcurrentHashMap<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    @Setter
    private long maxInactiveIntervalMillis = -1;

    private final AtomicBoolean shutDownRequested = new AtomicBoolean(false);
    private volatile ServerSocket serverSocketRef;
    private final AtomicBoolean isRegister = new AtomicBoolean(false);

    public HttpServer(int port) {
        this.port = port;
    }

    public void addServlet(String path, HttpServlet servlet) {
        urlMappingRegistry.addMapping(httpHelper.normalizePath(path), servlet);
    }

    public void addFilter(HttpFilter filter) {
        filters.add(filter);
    }

    public void start() {
        try (InputStream in = HttpServer.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (in != null) {
                Map<String, Object> root = new Yaml().load(in);
                if (root != null) {
                    Object sc = root.get("servletContext");
                    if (sc instanceof Map<?, ?> scMap) {
                        Object initParams = scMap.get("initParams");
                        if (initParams instanceof Map<?, ?> initParamsMap) {
                            initParamsMap.forEach((key, value) -> servletContext.addInitParam(String.valueOf(key), String.valueOf(value)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("读取初始化配置失败！");
        }
        urlMappingRegistry.addMapping("/static/*", new StaticResourceServlet());
        urlMappingRegistry.addMapping("/404", new NotFoundServlet());
        urlMappingRegistry.addMapping("/error", new ServerErrorServlet());
        urlMappingRegistry.getServlets().forEach(servlet -> {
            try {
                if (!initServletOrders.contains(servlet)) {
                    servlet.init();
                    initServletOrders.add(servlet);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(port);
             ScheduledExecutorService cleanSessionsService = Executors.newScheduledThreadPool(1)) {
            serverSocketRef = serverSocket;
            cleanSessionsService.scheduleAtFixedRate(() -> {
                Set<String> expiredSessions = new HashSet<>();
                sessionMap.forEach((s, httpSession) -> {
                    if (httpSession.isExpired()) {
                        expiredSessions.add(s);
                    }
                });
                expiredSessions.forEach(sessionMap::remove);
            }, 24, 1, TimeUnit.HOURS);
            if (!isRegister.get()) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    shutDownRequested.compareAndSet(false, true);
                    if (serverSocketRef != null) {
                        try {
                            serverSocketRef.close();
                            serverSocketRef = null;
                        } catch (IOException e) {
                            System.out.println("serverSocket close error");
                        }
                    }
                }));
                isRegister.compareAndSet(false, true);
            }
            while (!shutDownRequested.get()) {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> {
                    try {
                        handleConnection(socket);
                    } catch (IOException e) {
                        System.out.println("task io error");
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            System.out.println("socket close error");
                        }
                    }
                });
            }

        } catch (IOException e) {
            if (!shutDownRequested.get()) {
                System.out.println("socket io error");
            } else {
                System.out.println("server socket closed for shutdown");
            }
        } finally {
            executorService.shutdown();
            try {
                boolean isCompleted = executorService.awaitTermination(60, TimeUnit.SECONDS);
                for (int i = initServletOrders.size() - 1; i >= 0; i--) {
                    try {
                        initServletOrders.get(i).destroy();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                if (isCompleted) {
                    System.out.println("shutdown completed");
                } else {
                    executorService.shutdownNow();
                    System.out.println("shutdown timed out, some tasks may still be running");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleConnection(Socket socket) throws IOException {
        boolean responseSent = false;
        socket.setSoTimeout(60 * 1000);
        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
        try (socket) {
            while (true) {
                HttpRequest httpRequest = httpHelper.parseRequest(input);
                if (httpRequest == null) {
                    break;
                }
                httpRequest.setServletContext(this.servletContext);
                httpRequest.setHttpServer(this);
                boolean isClosed = httpHelper.checkClose(httpRequest);
                HttpResponse httpResponse = new HttpResponse();
                String sessionId = Optional.ofNullable(httpRequest.getCookies()).orElse(Collections.emptyMap()).get("JSESSIONID");
                HttpSession session = null;
                if (sessionId != null) {
                    session = sessionMap.get(sessionId);
                }
                if (sessionId == null || session == null) {
                    sessionId = UUID.randomUUID().toString();
                    session = new HttpSession();
                    if (maxInactiveIntervalMillis > 0) {
                        session.setDefaultMaxInactiveIntervalMillis(maxInactiveIntervalMillis);
                    }
                    session.setSessionId(sessionId);
                    sessionMap.put(sessionId, session);
                    httpResponse.getCookies().put("JSESSIONID", sessionId + "; Path=/; HttpOnly");
                }
                httpRequest.setSession(session);
                session.touch();
                httpResponse.setVersion(httpRequest.getVersion());
                String path = httpHelper.normalizePath(httpRequest.getPath());
                HttpServlet servlet;
                servlet = urlMappingRegistry.resolve(path);
                if (servlet == null) {
                    servlet = urlMappingRegistry.resolve("/404");
                }
                FilterChain filterChain = new FilterChainImpl(filters, servlet, 0);
                filterChain.doFilter(httpRequest, httpResponse);
                System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + httpRequest.getMethod() + " " + httpRequest.getPath() + "→" + httpResponse.getStatusDesc());
                if (isClosed) {
                    httpResponse.getHeaders().put("Connection", "close");
                } else {
                    httpResponse.getHeaders().put("Connection", "keep-alive");
                }
                output.write(httpResponse.toString().getBytes());
                output.write(httpResponse.getBody());
                output.flush();
                responseSent = true;
                if (isClosed) {
                    break;
                }
            }
        } catch (SocketTimeoutException socketTimeoutException) {
            System.out.println("socket timeout");
        } catch (Exception e) {
            if (!responseSent) {
                HttpResponse errorResponse = new HttpResponse();
                HttpServlet servlet = urlMappingRegistry.resolve("/error");
                HttpRequest httpRequest = new HttpRequest();
                httpRequest.setServletContext(this.servletContext);
                httpRequest.setHttpServer(this);
                servlet.service(httpRequest, errorResponse);
                output.write(errorResponse.toString().getBytes());
                output.write(errorResponse.getBody());
                output.flush();
            }
        }
    }

    public void dispatch(String path, HttpRequest request, HttpResponse response) throws IOException {
        path = httpHelper.normalizePath(path);
        HttpServlet servlet;
        servlet = urlMappingRegistry.resolve(path);
        if (servlet == null) {
            servlet = urlMappingRegistry.resolve("/404");
        }
        servlet.service(request, response);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return new RequestDispatcherImpl(this, httpHelper.normalizePath(path));
    }
}
