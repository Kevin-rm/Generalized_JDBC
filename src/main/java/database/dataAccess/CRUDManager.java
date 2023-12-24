package database.dataAccess;

import database.connection.DbConnector;
import database.util.CRUDManagerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * Meme fonctionnalite que l'ancienne classe BddObject mais celle-ci est moins couteuse car elle utilise des methodes statiques
 * Pas besoin d'heritage, c'est une classe totalement independante
 * Probleme:
 *  -> Elle est moins securisee aux erreurs, car il faut preciser a chaque fois la classe dont on veut recuperer les donnees 
 */
public class CRUDManager {
    private CRUDManager() {
        
    }
    
    public static Object[] select(String sgbd, Connection con , Class<?> c, String query) throws Exception {
        if (sgbd.compareToIgnoreCase("Oracle") != 0 && sgbd.compareToIgnoreCase("Postgres") != 0 && sgbd.compareToIgnoreCase("Mysql") != 0) {
            throw new Exception("Soit faute de frappe sur le SGDB, soit celui qui a été saisi n'est pas reconnu par ce programme");
        }
        
        boolean conWasNull = false;
        if (con == null) {
            conWasNull = true;

            con = DbConnector.getConnection(sgbd); // Genere une exception
        }

        Object[] valiny = null;
        Vector<Object> v = new Vector<>();

        Statement stmt = null;
        ResultSet res = null;

        try {
            stmt = con.createStatement();
            res = stmt.executeQuery(query);

            while (res.next()) {
                Object iray = CRUDManagerUtil.createObject(c, res);
                v.add(iray);
            }

            valiny = v.toArray();

            res.close();
            stmt.close();
            if (conWasNull) {
                con.close();
            }
        } catch (SQLException e) {
            throw new SQLException("\nSQLException dans CRUDManager.select: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("\nException dans CRUDManager.select: " + e.getMessage());
        }

        return valiny;
    }

    public static void insert(String sgbd, Connection con, Object object) throws Exception {
        if (sgbd.compareToIgnoreCase("Oracle") != 0 && sgbd.compareToIgnoreCase("Postgres") != 0 && sgbd.compareToIgnoreCase("Mysql") != 0) {
            throw new Exception("Soit faute de frappe sur le SGDB, soit celui qui a été saisi n'est pas reconnu par ce programme");
        }
        
        boolean conWasNull = false;
        if (con == null) {
            conWasNull = true;

            con = DbConnector.getConnection(sgbd); // Genere une exception
        }

        try {
            PreparedStatement preparedStatement = CRUDManagerUtil.getInsertPreparedStatement(con, object);
            preparedStatement.executeUpdate();

            con.commit();

            preparedStatement.close();
            if (conWasNull) {
                con.close();
            }
        } catch (SQLException e) {
            con.rollback();
            
            throw new SQLException("\nSQLException dans CRUDManager.insert: " + e.getMessage());
        }
    }

    /*
     * Fonction qui va updater des tables
     * A besoin que l'objet aie un primary key et ce-dernier ne doit pas etre une composite primary key
     */
    public static void update(String sgbd, Connection con, Object object) throws Exception {
        if (sgbd.compareToIgnoreCase("Oracle") != 0 && sgbd.compareToIgnoreCase("Postgres") != 0 && sgbd.compareToIgnoreCase("Mysql") != 0) {
            throw new Exception("Soit faute de frappe sur le SGDB, soit celui qui a été saisi n'est pas reconnu par ce programme");
        }
        
        boolean conWasNull = false;
        if (con == null) {
            conWasNull = true;

            con = DbConnector.getConnection(sgbd); // Genere une exception
        }

        try {
            PreparedStatement preparedStatement = CRUDManagerUtil.getUpdatePreparedStatement(con, object);
            preparedStatement.executeUpdate();

            con.commit();

            preparedStatement.close();
            if (conWasNull) {
                con.close();
            }
        } catch (SQLException e) {
            con.rollback();
            
            throw new SQLException("\nSQLException dans CRUDManager.update: " + e.getMessage());
        }
    }

    /*
     * La fonction delete est rarement utilisee dans un projet
     * A besoin que l'objet aie un primary key et ce-dernier ne doit pas etre une composite primary key
     */
    public static void delete(String sgbd, Connection con, Object object) throws Exception {
        if (sgbd.compareToIgnoreCase("Oracle") != 0 && sgbd.compareToIgnoreCase("Postgres") != 0 && sgbd.compareToIgnoreCase("Mysql") != 0) {
            throw new Exception("Soit faute de frappe sur le SGDB, soit celui qui a été saisi n'est pas reconnu par ce programme");
        }
        
        boolean conWasNull = false;
        if (con == null) {
            conWasNull = true;

            con = DbConnector.getConnection(sgbd); // Genere une exception
        }

        try {
            PreparedStatement preparedStatement = CRUDManagerUtil.getDeletePreparedStatement(con, object);
            preparedStatement.executeUpdate();

            con.commit();

            preparedStatement.close();
            if (conWasNull) {
                con.close();
            }
        } catch (SQLException e) {
            con.rollback();
            
            throw new SQLException("\nSQLException dans CRUDManager.delete: " + e.getMessage());
        }
    }
}
