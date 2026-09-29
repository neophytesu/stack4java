package mybatis.interceptor;

import mybatis.session.SqlSession;

import java.util.List;

public class InterceptorChain {
    private final List<Interceptor> interceptors;

    public InterceptorChain(List<Interceptor> interceptors) {
        this.interceptors = List.copyOf(interceptors);
    }

    public Object invoke(SqlSession target, String name, Object[] args) throws Exception {
        return new Invocation(target, name, args, interceptors, 0).proceed();
    }
}
