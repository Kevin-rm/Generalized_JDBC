package database.connection;

import database.util.DatabaseProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class OracleConnector {
    private static final String PILOT = "oracle.jdbc.driver.OracleDriver";
    private static String ip;
    private static int port;
    private static String user;
    private static String password; // Le password peut etre une chaine vide
    private static boolean staticFieldsInitialized = false;
    
    private OracleConnector() {
        
    }
    
    private static void initializeStaticFields() throws Exception {
        if (!OracleConnector.staticFieldsInitialized) {
            Properties properties = DatabaseProperties.getProperties();

            try {
                OracleConnector.ip = properties.getProperty("oracle.ip");
                OracleConnector.port = Integer.parseInt(properties.getProperty("oracle.port"));
                OracleConnector.user = properties.getProperty("oracle.user");
                OracleConnector.password = properties.getProperty("oracle.password");
                
                if (OracleConnector.ip == null || OracleConnector.ip.isEmpty() || OracleConnector.ip.isBlank()) {
                    throw new Exception("OracleConnection.ip est null");
                }
                if (OracleConnector.port <= 0) {
                    throw new Exception("OracleConnection.port est négatif");
                }
                if (OracleConnector.user == null || OracleConnector.user.isEmpty() || OracleConnector.user.isBlank()) {
                    throw new Exception("OracleConnection.user non spécifié");
                }

                OracleConnector.staticFieldsInitialized = true;
            } catch (Exception e) {
                throw new Exception("\nException sur l'initialisation des champs statiques dans OracleConnector.initializeStaticFields: " + e.getMessage());
            }   
        }
    }
    
    static Connection getConnection(String projet) throws Exception {
        Connection valiny = null;
        
        OracleConnector.initializeStaticFields();
        
        try {
            Class.forName(OracleConnector.PILOT);
            valiny = DriverManager.getConnection(OracleConnector.getUrl(), OracleConnector.user, OracleConnector.password);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("\nClassNotFoundException dans OracleConnector.getConnection: " + e.getMessage());
        } catch(SQLException e) {
            throw new SQLException("\nSQLException dans OracleConnector.getConnection: " + e.getMessage());
        }   
            
        return valiny;
    }
    
    private static String getUrl() {
        return "jdbc:oracle:thin:@" + OracleConnector.ip + ":" + OracleConnector.port + ":orcl";
    }
}
