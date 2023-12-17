package database.connection;

import database.util.DatabaseProperties;
import java.sql.Connection;

/**
 * C'est une classe qui permet de recuperer une connexion dans les bases suivantes:
 * Oracle, Postgres, MySQL
 */
public class DbConnector {
    private static String project;
    
    /* Initilisation de project */
    static {  
        try {
            DbConnector.project = DatabaseProperties.getProject();
        } catch (Exception e) {
            throw new RuntimeException("\nException lors de l'initialisation de project dans DbConnector", e);
        }
    }
    /* ----------------------- */
    
    private DbConnector() {
        
    }
    
    public static Connection getConnection(String sgbd) throws Exception {
        Connection valiny = null;
        
        if (sgbd.compareToIgnoreCase("Oracle") == 0) {
            valiny = OracleConnector.getConnection(DbConnector.project);
        } else if (sgbd.compareToIgnoreCase("Postgres") == 0) {
            valiny = PostgresConnector.getConnection(DbConnector.project);
        } else if (sgbd.compareToIgnoreCase("MySQL") == 0) {
            valiny = MysqlConnector.getConnection(DbConnector.project);
        } else { // Securisation au cas ou le SGBD est different des 3 bases reconnues par le programme
            throw new IllegalArgumentException("Soit faute de frappe sur le SGDB, soit celui qui a été saisi n'est pas reconnu par ce programme");
        }

        return valiny;
    }
}
