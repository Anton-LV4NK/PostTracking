import com.PostTracking.Application;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class Environment {
    private final String JDBC_DRIVER = "org.postgresql.Driver";
    private final String DB_URL = "jdbc:postgresql://%1$s:5432/";

    private final String db_username = "postgres";
    private final String db_password = "123456";
    private final String db_host = "localhost";
    public final int httpPort = 1409;


    private final Application application;

    private final Properties connectProps;
    private final String databaseName;

    public Environment() throws Exception
    {
        // create a temp database
        Class.forName(JDBC_DRIVER);
        connectProps = new Properties();
        connectProps.setProperty("user", db_username);
        connectProps.setProperty("password",db_password);
        connectProps.setProperty("ssl", "false");

        databaseName = "test_db_post_tracking";

        try (Connection conn = DriverManager.getConnection(String.format(DB_URL, db_host), connectProps))
        {
            try (PreparedStatement ps = conn.prepareStatement(String.format("CREATE DATABASE %s", databaseName)))
            {
                ps.execute();
            }
        }
        // start application
        application = new Application();
        String[] args = new String[] {
                "./src/test/java/test_settings.yaml",
        };
        application.start(args);

    }

    public void close() throws Exception
    {
        application.close();

        // drop database
        Connection conn = DriverManager.getConnection(String.format(DB_URL, db_host), connectProps);

        int counter = 0;
        while (true)
        {
            try (PreparedStatement ps = conn.prepareStatement(String.format("DROP DATABASE %s", databaseName)))
            {
                ps.execute();
                break;
            }
            catch (PSQLException ex)
            {
                Thread.sleep(100);
                if (counter++ == 20)
                {
                    break;
                }
            }
        }
    }
}

