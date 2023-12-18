package database.fileGenerator;

import database.connection.DbConnector;
import database.util.DatabaseProperties;
import database.util.FileGeneratorUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/** 
 * Une classe qui sert a generer automatiquement des fichiers java a partir des tables de la base de donnee
 * Elle n'est utilisee qu'une seule fois dans un programme donc on peut se permettre de l'instancier
 * Cependant la methode statique a ete preferee sur ce programme
 */
public class FileGenerator {
    private static String project;
    
    /* Initilisation de project */
    static {  
        try {
            FileGenerator.project = DatabaseProperties.getProject();
        } catch (Exception e) {
            throw new RuntimeException("\nException lors de l'initialisation de project dans FileGenerator", e);
        }
    }
    /* ----------------------- */
    
    private FileGenerator() {
        
    }

    public static void SQLTableToJava(String sgbd, Connection con, DatabaseMetaData databaseMetaData, String packageName, String tableName) throws Exception {
        if (!FileGeneratorUtil.tableExists(tableName, sgbd, FileGenerator.project, databaseMetaData)) {
            throw new Exception("La table " + tableName + " n'existe pas dans la database " + FileGenerator.project + " du sgbd " + sgbd);
        }
        
        boolean conWasNull = false;

        try {
            String filePath = null;
        
            String fileContent = new String();

            if (con == null || databaseMetaData == null) {  
                conWasNull = true;

                con = DbConnector.getConnection(sgbd);
                databaseMetaData = con.getMetaData();
            }

            ResultSet res = databaseMetaData.getColumns(FileGeneratorUtil.getCatalog(sgbd, FileGenerator.project), FileGeneratorUtil.getSchemaPattern(sgbd, FileGenerator.project), tableName, null);
            
            String className = FileGeneratorUtil.convertToPascalCase(tableName);

            // Package de la classe
            if (packageName != null && !packageName.isEmpty() && !packageName.isBlank()) {
                fileContent = "package " + packageName + ";\n\n";
                
                FileGeneratorUtil.createDirectories(packageName);
                filePath = FileGeneratorUtil.getPackagePath(packageName) + File.separator + className + ".java";
            } else {
                filePath = FileGeneratorUtil.getSourceCodeFolderPath() + File.separator + className + ".java";
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));

            ArrayList<String> columns = new ArrayList<>();
            ArrayList<String> types = new ArrayList<>();
            
            String fieldSection = new String();

            while (res.next()) {
                String column = res.getString("COLUMN_NAME");
                String type = FileGeneratorUtil.getAdjustedType(res.getString("TYPE_NAME"));
                
                fieldSection += "\t@Column(name = \"" + column + "\"";
                if (FileGeneratorUtil.isPrimaryKey(databaseMetaData, tableName, column)) {
                    fieldSection += ", isPrimaryKey = true";
                }
                
                if (sgbd.compareToIgnoreCase("Oracle") != 0) { // Mysql et Postgres, Oracle n'a pas de auto_increment
                    boolean isAutoIncrement = res.getString("IS_AUTOINCREMENT").equals("YES");
    
                    if (isAutoIncrement) {
                       fieldSection += ", isAutoIncrement = true";
                    }
                }
                fieldSection += ")\n";
                
                column = FileGeneratorUtil.convertToCamelCase(column);

                // Generer les attributs de la classe
                fieldSection += "\tprivate " + type + " " + column + ";\n";

                columns.add(column);
                types.add(type);
            }
            
            // Imports a faire
            fileContent += "import database.annotation.Column;\n";
            fileContent += "import database.annotation.Table;\n";
            
            // Si un champ est de type LocalDate alors on doit importer cette classe
            if (types.contains("LocalDate")) {
                fileContent += "import java.time.LocalDate;\n";
            }
            if (types.contains("LocalDateTime")) {
                fileContent += "import java.time.LocalDateTime;\n";
            }
            if (types.contains("LocalTime")) {
                fileContent += "import java.time.LocalTime;\n";
            }
            
            fileContent += "\n";

            fileContent += "@Table(name = \"" + tableName + "\")\n";
            fileContent += "public class " + className;
            fileContent += " {\n";
            
            fileContent += fieldSection;

            // Constructeur vide (par defaut)
            fileContent += "\n\tpublic " + className + "() {\n";
            fileContent += "\n\t}\n";

            /* Generer les getters et les setters par defaut */
            /* Getters */
            fileContent += "\n\t// getters";
            for (int i = 0; i < columns.size(); i++) {
                fileContent += "\n\tpublic " + types.get(i) + " get" + columns.get(i).substring(0, 1).toUpperCase() + columns.get(i).substring(1) + "() {\n";
                fileContent += "\t\treturn this." + columns.get(i) + ";\n";
                fileContent += "\t}";
            }
            /* ----------- */

            /* Setters */
            fileContent += "\n\n\t// setters";
            for (int i = 0; i < columns.size(); i++) {
                fileContent += "\n\tpublic";
                fileContent += " void set" + columns.get(i).substring(0, 1).toUpperCase() + columns.get(i).substring(1) + "(" + types.get(i) + " " + columns.get(i) + ") {\n";
                fileContent += "\t\tthis." + columns.get(i) + " = " + columns.get(i) + ";\n";
                fileContent += "\t}";

                if (i == columns.size() - 1) { 
                    fileContent += "\n"; // Lors du dernier setter on saute une ligne
                }
            }
            /* ----------- */

            fileContent += "}\n";

            bufferedWriter.write(fileContent);

            bufferedWriter.close();
            res.close();

            if (conWasNull) {
                con.close();
            }
    
            System.out.println("Fichier " + className + ".Java généré avec succès");
        } catch (IOException e) {
            throw new IOException("\nIOException dans FileGenerator.SQLTableToJava: " + e.getMessage());
        } catch (SQLException e) {
            throw new SQLException("\nSQLException dans FileGenerator.SQLTableToJava: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("\nException dans FileGenerator.SQLTableToJava: " + e.getMessage());
        }
    }
    
    // Genere une interface DAO pour une table donnee
    public static void generateDAOInterface(String tableName) throws Exception {
        String className = FileGeneratorUtil.convertToPascalCase(tableName);
        String interfaceName = className + "DAO";

        String interfaceContent = new String();
        
        interfaceContent += "package model.dao;\n\n";
        
        interfaceContent += "import java.sql.Connection;\n";
        interfaceContent += "import model.table." + className + ";\n\n";
        
        interfaceContent += "public interface " + interfaceName + " {\n";
        
        interfaceContent += "\t/* Ci-dessous sont des methodes basiques de CRUD */\n\n";
        interfaceContent += "\t" + className + "[] getAll" + className + "s(Connection con) throws Exception;\n\n";
        interfaceContent += "\tvoid insert(Connection con," + className + " " + FileGeneratorUtil.convertToCamelCase(tableName) + ") throws Exception;\n\n";
        interfaceContent += "\tvoid update(Connection con, " + className + " " + FileGeneratorUtil.convertToCamelCase(tableName) + ") throws Exception;\n\n";
        interfaceContent += "\tvoid delete(Connection con, " + className + " " + FileGeneratorUtil.convertToCamelCase(tableName) + ") throws Exception;\n";
        
        interfaceContent += "}\n";
        
        String packageName = "model.dao";
        FileGeneratorUtil.createDirectories(packageName);
               
        String filePath = FileGeneratorUtil.getPackagePath(packageName) + File.separator + interfaceName + ".java";

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            bufferedWriter.write(interfaceContent);
            
            bufferedWriter.close();
            
            System.out.println("Interface " + interfaceName + " générée avec succès");
        } catch (IOException e) {
            throw new IOException("\nIOException dans FileGenerator.generateDAOInterface: " + e.getMessage());
        }
    }
    
    // Genere une classe qui va implementer son interface DAO respectif pour une table donnee
    public static void generateDAOImplementationClass(String sgbd, String tableName) throws Exception {
        String className = FileGeneratorUtil.convertToPascalCase(tableName);
        String interfaceName = className + "DAO";
        String interfaceImplClassName = interfaceName + "Impl";

        String fileContent = new String();
        
        fileContent += "package model.daoImpl;\n\n";
        
        fileContent += "import database.annotation.Table;\n";
        fileContent += "import database.dataAccess.CRUDManager;\n";
        fileContent += "import java.sql.Connection;\n";
        fileContent += "import java.util.Arrays;\n";
        fileContent += "import model.table." + className + ";\n";
        fileContent += "import model.dao." + interfaceName + ";\n\n";
        
        fileContent += "public class " + interfaceImplClassName + " implements " + interfaceName + " {\n";
        
        fileContent += "\tprivate static final String SGBD = \"" + sgbd + "\";\n\n";
        
        /* Section commentaire */
        fileContent += "\t/*\n";
        fileContent += "\t * Ci-dessous sont des codes auto-generated, ils offrent une overview de l'utilisation de la classe CRUDManager\n";
        fileContent += "\t * Vous pouvez les modifier selon vos guises et selon votre propre logique\n";
        fileContent += "\t * Verifier juste que vos requetes SELECT ne contiennent pas d'erreurs\n";
        fileContent += "\t */\n\n";
        /* ------------------- */
        
        fileContent += "\t@Override\n";
        fileContent += "\tpublic " + className + "[] getAll" + className + "s(Connection con) throws Exception {\n";
        /* Code auto-generated pour le select */
        fileContent += "\t\tTable sqlTable = " + className + ".class.getAnnotation(Table.class);\n";
        fileContent += "\t\tif (sqlTable == null) {\n";
        fileContent += "\t\t\tthrow new Exception(\"Annotation absente pour la classe \" + " + className +".class.getName());\n";
        fileContent += "\t\t}\n\n";
        fileContent += "\t\tObject[] objects = CRUDManager.select(" + interfaceImplClassName + ".SGBD, con, " + className + ".class, \"SELECT * FROM \" + sqlTable.name());\n";
        fileContent += "\t\treturn Arrays.copyOf(objects, objects.length, " + className + "[].class);\n";
        /* ------------------ */
        fileContent += "\t}\n\n";
        
        fileContent += "\t@Override\n";
        fileContent += "\tpublic void insert(Connection con, " + className + " " + FileGeneratorUtil.convertToCamelCase(tableName) + ") throws Exception {\n";
        /* Code auto-generated insert */
        fileContent += "\t\tCRUDManager.insert(" + interfaceImplClassName + ".SGBD, con, " + FileGeneratorUtil.convertToCamelCase(tableName) + ");\n";
        /* ------------- */
        fileContent += "\t}\n\n";
        
        fileContent += "\t@Override\n";
        fileContent += "\tpublic void update(Connection con, " + className + " " + FileGeneratorUtil.convertToCamelCase(tableName) + ") throws Exception {\n";
        /* Code auto-generated update */
        fileContent += "\t\tCRUDManager.update(" + interfaceImplClassName + ".SGBD, con, " + FileGeneratorUtil.convertToCamelCase(tableName) + ");\n";
        /* ------------- */
        fileContent += "\t}\n\n";
        
        fileContent += "\t@Override\n";
        fileContent += "\tpublic void delete(Connection con, " + className + " " + FileGeneratorUtil.convertToCamelCase(tableName) + ") throws Exception {\n";
        /* Code auto-generated delete */
        fileContent += "\t\tCRUDManager.delete(" + interfaceImplClassName + ".SGBD, con, " + FileGeneratorUtil.convertToCamelCase(tableName) + ");\n";
        /* ------------- */
        fileContent += "\t}\n";
        
        fileContent += "}\n";
        
        String packageName = "model.daoImpl";
        FileGeneratorUtil.createDirectories(packageName);
               
        String filePath = FileGeneratorUtil.getPackagePath(packageName) + File.separator + interfaceImplClassName + ".java";

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            bufferedWriter.write(fileContent);
            
            bufferedWriter.close();
            
            System.out.println("Fichier " + interfaceImplClassName + ".java générée avec succès");
        } catch (IOException e) {
            throw new IOException("\nIOException dans FileGenerator.generateDAOImplemenatation: " + e.getMessage());
        }
    }

    /*
     * Fonctionnalites:
     * - Transforme toutes les tables presentes dans la base de donnees en fichier .java
     * - Genere les DAO specifiques pour chaque classe generee
     * - Implement ces interfaces DAO
     */
    public static void generateModelClassesFromDbTables(String sgbd) throws Exception {
        Connection con = DbConnector.getConnection(sgbd);
        DatabaseMetaData databaseMetaData = con.getMetaData();
        
        String[] allTables = FileGeneratorUtil.getAllTables(sgbd, FileGenerator.project, databaseMetaData);
        for (String table : allTables) {
            // Par defaut, toutes les classes generees ont comme package: "model.table"
            FileGenerator.SQLTableToJava(sgbd, con, databaseMetaData, "model.table", table);
            // Les DAO sont dans le package "model.dao"
            FileGenerator.generateDAOInterface(table);
            // Les implementations des DAO quant a eux sont dans "model.daoImpl"
            FileGenerator.generateDAOImplementationClass(sgbd, table);
        }

        con.close();
    }
}
