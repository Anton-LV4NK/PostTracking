package com.PostTracking.Web;

import com.PostTracking.Settings;
import org.apache.http.ExceptionLogger;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import java.net.BindException;


public class WebServer {
    private final Logger m_logger;
    private final static int timeOutRequestExecution = 15000;
    private HttpServer m_httpServer;
    private final Settings m_settings;
    private final SessionFactory m_sessionFactory;

    public WebServer(Settings settings, SessionFactory sessionFactory)
    {
        m_logger = LogManager.getLogger(WebServer.class);
        m_settings = settings;
        m_sessionFactory = sessionFactory;
    }

    public void start()  throws Exception {
        IOReactorConfig config = IOReactorConfig.custom()
                .setSoTimeout(timeOutRequestExecution)
                .setSoReuseAddress(true)
                .setTcpNoDelay(true)
                .build();
        m_httpServer = ServerBootstrap.bootstrap()
                .setListenerPort(m_settings.httpPort)
                .setServerInfo("PostTracking/1.0")
                .setIOReactorConfig(config)
                .setExceptionLogger(new WebExceptionLogger())
                .registerHandler("/items/*", new ItemsTrackingHandler(m_sessionFactory))
                .registerHandler("/post/*", new PostHandler(m_sessionFactory))
                .create();
        m_httpServer.start();
        m_logger.info("Started");
    }

    private class WebExceptionLogger implements ExceptionLogger
    {
        @Override
        public void log(Exception e)
        {
            ExceptionLogger.STD_ERR.log(e);
            if (e instanceof BindException)
            {
                System.exit(-1);
            }
        }
    }

    public void stop(){
        m_sessionFactory.close();
    }
}
