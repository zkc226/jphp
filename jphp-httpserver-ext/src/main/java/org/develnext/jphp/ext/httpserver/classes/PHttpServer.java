package org.develnext.jphp.ext.httpserver.classes;

import org.develnext.jphp.ext.httpserver.HttpServerExtension;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import php.runtime.Memory;
import php.runtime.annotation.Reflection.Name;
import php.runtime.annotation.Reflection.Namespace;
import php.runtime.annotation.Reflection.Nullable;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.invoke.Invoker;
import php.runtime.lang.BaseObject;
import php.runtime.memory.*;
import php.runtime.reflection.ClassEntity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Name("HttpServer")
@Namespace(HttpServerExtension.NS)
public class PHttpServer extends BaseObject {
    private Server server;
    private HandlerList handlers = new HandlerList();
    private SessionIdManager idmanager;
    private QueuedThreadPool threadPool;

    public PHttpServer(Environment env, Server server) {
        super(env);
        this.server = server;
    }

    public PHttpServer(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public void __construct() {
        threadPool = new QueuedThreadPool();
        server = new Server(threadPool);
        initSessionManager();
    }

    @Signature
    public void __construct(int port) {
        __construct(port, null);
    }

    private void initSessionManager() {
        idmanager = new DefaultSessionIdManager(server);
        server.setSessionIdManager(idmanager);

        SessionHandler sessions = new SessionHandler();
        sessions.setSessionIdManager(idmanager);
        handlers.addHandler(sessions);
    }

    @Signature
    public void __construct(int port, String host) {
        threadPool = new QueuedThreadPool();
        server = new Server(threadPool);

        if (host == null || host.isEmpty()) {
            listen(LongMemory.valueOf(port));
        } else {
            listen(StringMemory.valueOf(host + ":" + port));
        }

        initSessionManager();
    }

    @Signature
    public void minThreads(int minThreads) {
        threadPool.setMinThreads(minThreads);
    }

    @Signature
    public int minThreads() {
        return threadPool.getMinThreads();
    }

    @Signature
    public void maxThreads(int maxThreads) {
        threadPool.setMaxThreads(maxThreads);
    }

    @Signature
    public int maxThreads() {
        return threadPool.getMaxThreads();
    }

    @Signature
    public void threadIdleTimeout(int timeout) {
        threadPool.setIdleTimeout(timeout);
    }

    @Signature
    public int threadIdleTimeout() {
        return threadPool.getIdleTimeout();
    }

    @Signature
    public boolean stopAtShutdown() {
        return server.getStopAtShutdown();
    }

    @Signature
    public void stopAtShutdown(boolean val) {
        server.setStopAtShutdown(val);
    }

    @Signature
    public void clearHandlers() {
        handlers.setHandlers(new Handler[0]);
    }

    @Signature
    public void addHandler(final Invoker invoker) {
        handlers.addHandler(new InvokeHandler(invoker));
    }

    @Signature
    public Memory handlers() {
        ArrayMemory result = ArrayMemory.createListed(handlers.getHandlers().length);

        for (Handler handler : handlers.getHandlers()) {
            if (handler instanceof InvokeHandler) {
                Invoker invoker = ((InvokeHandler) handler).getInvoker();
                result.add(invoker.getMemory().toImmutable());
            }
        }

        return result.toImmutable();
    }

    @Signature
    public PHttpRouteHandler route(Environment env, Memory methods, String path, Memory invoker) {
        PHttpRouteHandler routeHandler = new PHttpRouteHandler(env);
        routeHandler.reset(env, methods, path, invoker);

        addHandler(Invoker.create(env, ObjectMemory.valueOf(routeHandler)));
        return routeHandler;
    }

    @Signature
    public PHttpRouteHandler any(Environment env, String path, Memory invoker) {
        return route(env, StringMemory.valueOf("*"), path, invoker);
    }

    @Signature
    public PHttpRouteHandler get(Environment env, String path, Memory invoker) {
        return route(env, StringMemory.valueOf("GET"), path, invoker);
    }

    @Signature
    public PHttpRouteHandler post(Environment env, String path, Memory invoker) {
        return route(env, StringMemory.valueOf("POST"), path, invoker);
    }

    @Signature
    public PHttpRouteHandler put(Environment env, String path, Memory invoker) {
        return route(env, StringMemory.valueOf("PUT"), path, invoker);
    }

    @Signature
    public PHttpRouteHandler delete(Environment env, String path, Memory invoker) {
        return route(env, StringMemory.valueOf("DELETE"), path, invoker);
    }

    @Signature
    public PHttpRouteHandler options(Environment env, String path, Memory invoker) {
        return route(env, StringMemory.valueOf("OPTIONS"), path, invoker);
    }

    @Signature
    public void listen(Memory value) {
        listen(value, null);
    }

    @Signature
    public void listen(Memory value, ArrayMemory sslSettings) {
        ServerConnector connector;

        if (sslSettings != null) {
            SslContextFactory contextFactory = new SslContextFactory();

            // key store
            if (sslSettings.containsKey("keyStorePath"))
                contextFactory.setKeyStorePath(sslSettings.valueOfIndex("keyStorePath").toString());

            if (sslSettings.containsKey("keyStorePassword"))
                contextFactory.setKeyStoreType(sslSettings.valueOfIndex("keyStorePassword").toString());

            if (sslSettings.containsKey("keyStoreType"))
                contextFactory.setKeyStoreType(sslSettings.valueOfIndex("keyStoreType").toString());

            if (sslSettings.containsKey("keyStoreProvider"))
                contextFactory.setKeyStoreProvider(sslSettings.valueOfIndex("keyStoreProvider").toString());

            // trust store
            if (sslSettings.containsKey("trustStorePath"))
                contextFactory.setTrustStorePath(sslSettings.valueOfIndex("trustStorePath").toString());

            if (sslSettings.containsKey("trustStorePassword"))
                contextFactory.setTrustStoreType(sslSettings.valueOfIndex("trustStorePassword").toString());

            if (sslSettings.containsKey("trustStoreType"))
                contextFactory.setTrustStoreType(sslSettings.valueOfIndex("trustStoreType").toString());

            if (sslSettings.containsKey("trustStoreProvider"))
                contextFactory.setTrustStoreProvider(sslSettings.valueOfIndex("trustStoreProvider").toString());

            if (sslSettings.containsKey("trustAll"))
                contextFactory.setTrustAll(sslSettings.valueOfIndex("trustAll").toBoolean());

            if (sslSettings.containsKey("trustManagerFactoryAlgorithm"))
                contextFactory.setTrustManagerFactoryAlgorithm(sslSettings.valueOfIndex("trustManagerFactoryAlgorithm").toString());

            // key manager
            if (sslSettings.containsKey("keyManagerFactoryAlgorithm"))
                contextFactory.setKeyManagerFactoryAlgorithm(sslSettings.valueOfIndex("keyManagerFactoryAlgorithm").toString());

            if (sslSettings.containsKey("keyManagerPassword"))
                contextFactory.setKeyManagerPassword(sslSettings.valueOfIndex("keyManagerPassword").toString());

            // other
            if (sslSettings.containsKey("certAlias"))
                contextFactory.setCertAlias(sslSettings.valueOfIndex("certAlias").toString());

            if (sslSettings.containsKey("protocol"))
                contextFactory.setProtocol(sslSettings.valueOfIndex("protocol").toString());

            if (sslSettings.containsKey("provider"))
                contextFactory.setProvider(sslSettings.valueOfIndex("provider").toString());

            if (sslSettings.containsKey("validateCerts"))
                contextFactory.setValidateCerts(sslSettings.valueOfIndex("validateCerts").toBoolean());

            connector = new ServerConnector(server, contextFactory);
        } else {
            connector = new ServerConnector(server);
        }

        if (value.isNumber()) {
            connector.setName("0.0.0.0:" + value.toInteger());
            connector.setPort(value.toInteger());
        } else {
            String[] strings = value.toString().split("\\:");

            if (strings.length < 2) {
                throw new IllegalArgumentException("Invalid listen value: " + value);
            }

            connector.setHost(strings[0]);
            connector.setPort(Integer.parseInt(strings[1]));
            connector.setName(strings[0] + ":" + strings[1]);
        }

        server.addConnector(connector);
    }

    @Signature
    public boolean unlisten(Environment env, String value) {
        if (server.isRunning()) {
            env.exception("Unable to unlisten() for running server");
        }

        String host = "0.0.0.0";
        String port = "80";

        if (value.contains(":")) {
            String[] strings = value.split("\\:");
            host = strings[0];

            if (strings.length > 1) {
                port = strings[1];
            }
        }

        for (Connector connector : server.getConnectors()) {
            if (connector.getName().equals(host + ":" + port)) {
                server.removeConnector(connector);
                return true;
            }
        }

        return false;
    }

    @Signature
    public Memory connectors() {
        Connector[] connectors = server.getConnectors();
        ArrayMemory arrayMemory = ArrayMemory.createListed(connectors.length);

        for (Connector connector : connectors) {
            arrayMemory.add(connector.getName());
        }

        return arrayMemory.toImmutable();
    }

    @Signature
    public void runInBackground() throws Exception {
        server.setErrorHandler(new ErrorHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                super.handle(target, baseRequest, request, response);
            }
        });
        server.setHandler(handlers);
        server.start();
    }

    @Signature
    public void run() throws Exception {
        runInBackground();
        server.join();
    }

    @Signature
    public void shutdown() throws Exception {
        server.stop();
    }

    @Signature
    public boolean isRunning() {
        return server.isRunning();
    }

    @Signature
    public boolean isFailed() {
        return server.isFailed();
    }

    @Signature
    public boolean isStarting() {
        return server.isStarting();
    }

    @Signature
    public boolean isStopping() {
        return server.isStopping();
    }

    @Signature
    public boolean isStopped() {
        return server.isStopped();
    }

    @Signature
    public void setRequestLogHandler(Environment env, @Nullable Invoker invoker) {
        if (invoker == null) {
            server.setRequestLog(null);
        } else {
            server.setRequestLog((request, response) -> {
                invoker.callAny(new PHttpServerRequest(env, request), new PHttpServerResponse(env, response));
            });
        }
    }

    public static class InvokeHandler extends AbstractHandler {
        private final Invoker invoker;

        public InvokeHandler(Invoker invoker) {
            this.invoker = invoker;
        }

        public Invoker getInvoker() {
            return invoker;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            try {
                invoker.callAny(new PHttpServerRequest(invoker.getEnvironment(), baseRequest), new PHttpServerResponse(invoker.getEnvironment(), response));
            } catch (Throwable e) {
                Environment.catchThrowable(e, invoker.getEnvironment());
            }
        }
    }
}
