package com.PostTracking;

import com.PostTracking.Web.WebServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;

public class Application {
    private WebServer m_webServer;
    private Settings m_settings;
    private final Logger m_logger = LogManager.getLogger(Application.class);
    public void start(String[] args)
    {
        try {
            try (final FileInputStream configFile = new FileInputStream(args[0])) {
                m_settings = new Yaml().loadAs(configFile, Settings.class);
            } catch (Exception e) {
                m_logger.error("settings.yaml problem", e);
                System.exit(-1);
            }
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml");
            overrideHibernateSettings(builder);
            final SessionFactory sessionFactory = new MetadataSources(builder.build()).buildMetadata().buildSessionFactory();
            m_webServer = new WebServer(m_settings, sessionFactory);
            m_webServer.start();
        } catch (Exception e) {
            m_logger.error("Failed to start", e);
            System.exit(-1);
        }
    }
    private void overrideHibernateSettings(StandardServiceRegistryBuilder builder) {
        if (m_settings.connectionUrl != null) {
            builder.applySetting("hibernate.connection.url", m_settings.connectionUrl);
        }

        if (m_settings.username != null) {
            builder.applySetting("hibernate.connection.username", m_settings.username);
        }

        if (m_settings.password != null) {
            builder.applySetting("hibernate.connection.password", m_settings.password);
        }

        if (m_settings.showSql != null) {
            builder.applySetting("hibernate.show_sql", m_settings.showSql);
        }
        if (m_settings.useQueryCache != null) {
            builder.applySetting("hibernate.cache.use_query_cache", m_settings.useQueryCache);
        }
        if (m_settings.updateScheme != null) {
            builder.applySetting("hibernate.hbm2ddl.auto", m_settings.updateScheme);
        }
    }

    public void close() throws Exception
    {
        m_webServer.stop();
    }
}
