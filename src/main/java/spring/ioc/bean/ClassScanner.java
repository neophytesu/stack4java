package spring.ioc.bean;

import spring.ioc.annotation.Bean;

import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ClassScanner {
    List<Class<?>> scanPackage(String basePackage) throws Exception {
        String basePath = basePackage.replace(".", "/");
        ClassLoader cl = this.getClass().getClassLoader();
        URL url = cl.getResource(basePath);
        List<Class<?>> classes = new ArrayList<>();
        if (url == null) {
            return classes;
        }
        if (Objects.equals(url.getProtocol(), "file")) {
            Path packagePath = Paths.get(url.toURI());
            try (Stream<Path> paths = Files.walk(packagePath)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".class"))
                        .forEach(path -> {
                            String className = packagePath.relativize(path).toString().replace("\\", ".").replace("/", ".").replace(".class", "");
                            try {
                                Class<?> clazz = Class.forName(basePackage + "." + className, false, cl);
                                if (isBean(clazz)) {
                                    classes.add(clazz);
                                }
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        } else if (Objects.equals(url.getProtocol(), "jar")) {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            JarFile jarFile = conn.getJarFile();
            String prefix = basePackage.replace(".", "/") + "/";
            jarFile.stream().filter(jarEntry -> {
                        String name = jarEntry.getName();
                        return !(jarEntry.isDirectory()) && name.startsWith(prefix) && name.endsWith(".class") && !name.equals("/module-info.class");
                    })
                    .forEach(jarEntry -> {
                        String className = jarEntry.getName().replace(".class", "").replace("/", ".");
                        try {
                            Class<?> clazz = Class.forName(className, false, cl);
                            if (isBean(clazz)) {
                                classes.add(clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                    });
        }
        return classes;
    }

    private boolean isBean(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Bean.class)) {
            return true;
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Bean.class)) {
                return true;
            }
        }
        return false;
    }
}
