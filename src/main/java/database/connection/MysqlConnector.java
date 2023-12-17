package database.connection;

import database.util.DatabaseProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnector {
    private static final String PILOT = "com.mysql.cj.jdbc.Driver";
    private static String ip;
    private static int port;
    private static String user;
    private static String password; // Le password peut etre une chaine vide
    private static boolean staticFieldsInitialized = false;
    
    private MysqlConnector() {
        
    }
    
    private static void initializeStaticFields() throws Exception {
        if (!MysqlConnector.staticFieldsInitialized) {
            Properties properties = DatabaseProperties.getProperties();

            try {
                MysqlConnector.ip = properties.getProperty("mysql.ip");
                MysqlConnector.port = Integer.parseInt(properties.getProperty("mysql.port"));
                MysqlConnector.user = properties.getProperty("mysql.user");
                MysqlConnector.password = properties.getProperty("mysql.password");
                
                if (MysqlConnector.ip == null || MysqlConnector.ip.isEmpty() || MysqlConnector.ip.isBlank()) {
                    throw new Exception("MysqlConnection.ip est null");
                }
                if (MysqlConnector.port <= 0) {
                    throw new Exception("MysqlConnection.port est négatif");
                }
                if (MysqlConnector.user == null || MysqlConnector.user.isEmpty() || MysqlConnector.user.isBlank()) {
                    throw new Exception("MysqlConnection.user non spécifié");
                }

                MysqlConnector.staticFieldsInitialized = true;
            } catch (Exception e) {
                throw new Exception("\nException sur l'initialisation des champs statiques dans MysqlConnector.initializeStaticFields: " + e.getMessage());
            }   
        }
    }
    
    static Connection getConnection(String projet) throws Exception {
        Connection valiny = null;
        
        MysqlConnector.initializeStaticFields();
        
        try {
            Class.forName(MysqlConnector.PILOT);
            valiny = DriverManager.getConnection(MysqlConnector.getUrl(projet), MysqlConnector.user, MysqlConnector.password);
        } catch (ClassNotFoundException e) {
            e = new ClassNotFoundException("\nClassNotFoundException dans MysqlConnector.getConnection: " + e.getMessage());
            throw e;
        } catch(SQLException e) {
            e = new SQLException("\nSQLException dans MysqlConnector.getConnection: " + e.getMessage());
            throw e;
        }   
            
        return valiny;
    }
    
    private static String getUrl(String projet) {
        return "jdbc:mysql://" + MysqlConnector.ip + ":" + MysqlConnector.port + "/" + projet;
    }
}
