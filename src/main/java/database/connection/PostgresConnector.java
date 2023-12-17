package database.connection;

import database.util.DatabaseProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresConnector {
    private static final String PILOT = "org.postgresql.Driver";
    private static String ip;
    private static int port;
    private static String user;
    private static String password; // Le password peut etre une chaine vide
    private static boolean staticFieldsInitialized = false;
    
    private PostgresConnector() {
        
    }
    
    private static void initializeStaticFields() throws Exception {
        if (!PostgresConnector.staticFieldsInitialized) {
            Properties properties = DatabaseProperties.getProperties();

            try {
                PostgresConnector.ip = properties.getProperty("postgres.ip");
                PostgresConnector.port = Integer.parseInt(properties.getProperty("postgres.port"));
                PostgresConnector.user = properties.getProperty("postgres.user");
                PostgresConnector.password = properties.getProperty("postgres.password");
                
                if (PostgresConnector.ip == null || PostgresConnector.ip.isEmpty() || PostgresConnector.ip.isBlank()) {
                    throw new Exception("PostgresConnection.ip est null");
                }
                if (PostgresConnector.port <= 0) {
                    throw new Exception("PostgresConnection.port est négatif");
                }
                if (PostgresConnector.user == null || PostgresConnector.user.isEmpty() || PostgresConnector.user.isBlank()) {
                    throw new Exception("PostgresConnection.user non spécifié");
                }

                PostgresConnector.staticFieldsInitialized = true;
            } catch (Exception e) {
                throw new Exception("\nException sur l'initialisation des champs statiques dans PostgresConnector.initializeStaticFields: " + e.getMessage());
            }   
        }
    }
    
    static Connection getConnection(String projet) throws Exception {
        Connection valiny = null;
        
        PostgresConnector.initializeStaticFields();
        
        try {
            Class.forName(PostgresConnector.PILOT);
            valiny = DriverManager.getConnection(PostgresConnector.getUrl(projet), PostgresConnector.user, PostgresConnector.password);
        } catch (ClassNotFoundException e) {
            e = new ClassNotFoundException("\nClassNotFoundException dans PostgresConnector.getConnection: " + e.getMessage());
            throw e;
        } catch(SQLException e) {
            e = new SQLException("\nSQLException dans PostgresConnector.getConnection: " + e.getMessage());
            throw e;
        }   
            
        return valiny;
    }
    
    private static String getUrl(String projet) {
        return "jdbc:postgresql://" + PostgresConnector.ip + ":" + PostgresConnector.port + "/" + projet;
    }
}
