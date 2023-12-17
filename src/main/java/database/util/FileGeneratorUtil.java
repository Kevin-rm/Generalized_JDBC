package database.util;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Vector;
import java.io.File;
import java.sql.SQLException;

/**
 * Description:
 * Cette classe contient toutes les fonctions dont a besoin la classe FileGenerator
 */
public class FileGeneratorUtil {
    private FileGeneratorUtil() {
        
    }
    
    /*
     * Pourquoi les methodes getCatalog et getSchemaPattern ?
     *  -> Si on met catalog = null (ou pour Oracle schemaPattern = null), le ResultSet recupere tout le contenu du database 
     *     Par consequent s'il y a des tables de meme nom dans d'autres databases, alors cela peut causer des erreurs
     *
     * catalog = project, si la base est Postgres ou MySQL
     * catalog = null pour Oracle
    */
    public static String getCatalog(String sgbd, String project) {
        String valiny = null;

        if (sgbd.compareToIgnoreCase("Postgres") == 0 || sgbd.compareToIgnoreCase("Mysql") == 0) {
            valiny = project;
        }

        return valiny;
    }
    
    /*
     * Pour Oracle schemaPattern = project et en MAJUSCULE 
     * Pour Postgres et MySQL, schemaPattern = null
     */
    public static String getSchemaPattern(String sgbd, String project) {
        String valiny = null;

        if (sgbd.compareToIgnoreCase("Oracle") == 0) {
            valiny = project.toUpperCase();
        } 

        return valiny;
    }

    public static String[] getAllTables(String sgbd, String project, DatabaseMetaData databaseMetaData) throws SQLException {
        String[] valiny = null;
        Vector<String> v = new Vector<>();
        
        try (ResultSet tables = databaseMetaData.getTables(FileGeneratorUtil.getCatalog(sgbd, project), FileGeneratorUtil.getSchemaPattern(sgbd, project), "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                v.add(tables.getString("TABLE_NAME"));
            }
        }

        valiny = v.toArray(new String[v.size()]);
        return valiny;
    }
    
    public static boolean tableExists(String tableName, String sgbd, String project, DatabaseMetaData databaseMetaData) throws SQLException {
        for (String table : FileGeneratorUtil.getAllTables(sgbd, project, databaseMetaData)) {
            if (table.compareToIgnoreCase(tableName) == 0) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isPrimaryKey(DatabaseMetaData databaseMetaData, String tableName, String columnName) throws Exception {
        ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null, null, tableName);
        
        while (primaryKeys.next()) {
            String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");
            if (columnName.equalsIgnoreCase(primaryKeyColumnName)) {
                return true;
            }
        }

        return false;
    } 

    /*
     * Ajuste le nom de la table en nom de classe suivant la norme de Java
     * Par exemple:
     * rel_act_bouq -> RelActBouq
     */
    public static String convertToCamelCase(String input) {
        String valiny = new String();
        
        String[] splittedInput = input.toLowerCase().toLowerCase().split("_");
        
        valiny += splittedInput[0];
        for (int i = 1; i < splittedInput.length; i++) {
            valiny += splittedInput[i].substring(0, 1).toUpperCase() + splittedInput[i].substring(1);
        }            

        return valiny;
    }
    
    public static String convertToPascalCase(String input) {
        return FileGeneratorUtil.convertToCamelCase(input).substring(0, 1).toUpperCase() + FileGeneratorUtil.convertToCamelCase(input).substring(1);
    }

    public static String getAdjustedType(String type) {
        String valiny = new String();

        type = type.toLowerCase();

        if (type.startsWith("int") || type.equals("serial")) {
            valiny = "int";
        } else if (type.equals("decimal") || type.equals("numeric") || type.compareToIgnoreCase("number") == 0) {
            valiny = "double";
        } else if (type.startsWith("varchar")) {
            valiny = "String";
        } else if (type.equals("date")) {
            valiny = "LocalDate";
        }

        return valiny;
    }
    
    /*
     * Chemin du dossier ou se situe tous les package et fichier .java,
     * Par defaut, le dossier est src/main/java (structure de projet Java)
     * On doit changer le chemin du sourcecode folder selon l'environnement de developpement
     */
    public static String getSourceCodeFolderPath() {
        return "src" + File.separator + "main" + File.separator + "java";
    }
    
    public static void createDirectories(String packageName) {
        String[] splittedPackageName = packageName.split("\\.");
        
        String currentPath = FileGeneratorUtil.getSourceCodeFolderPath();

        for (String folder : splittedPackageName) {
            currentPath = currentPath + File.separator + folder; // Construire le chemin complet

            File dir = new File(currentPath);
            dir.mkdir();
        }
    }
    
    public static String getPackagePath(String packageName) {
        return FileGeneratorUtil.getSourceCodeFolderPath() + File.separator + packageName.replaceAll("\\.", "/");
    }
}
