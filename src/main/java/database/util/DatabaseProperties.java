package database.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Ceci une classe qu'on appelle Singleton
 * Mais c'est quoi un Singleton ?
 * -> Singleton est un modele de conception (design pattern) qui garantit qu'une classe n'a qu'une seule instance et fournit un point d'acces global a cette instance unique
 * A quoi donc sert-elle ici ?
 * -> Charger les properties une seule fois et les reutiliser pour toutes les connexions ulterieures tout au long du programme
 */
public class DatabaseProperties {
    private static final String PROPERTIES_FILE = "configurations/database.properties";
    private static Properties properties;
    private static boolean propertiesAreInitialized = false;

    private DatabaseProperties() {
        
    }

    private static void initProperties() throws IOException {
        if (!DatabaseProperties.propertiesAreInitialized) {
            DatabaseProperties.properties = new Properties();
            
            try (FileInputStream fileInputStream = new FileInputStream(DatabaseProperties.PROPERTIES_FILE)) {
                DatabaseProperties.properties.load(fileInputStream);
                
                DatabaseProperties.propertiesAreInitialized = true;
            } catch (IOException e) {
                throw new IOException("\nIOException lors de l'initialisation des propriétés dans DatabaseProperties.init: " + e.getMessage());
            }
        }
    }

    public static Properties getProperties() throws IOException {
        DatabaseProperties.initProperties();
        
        return DatabaseProperties.properties;
    }
    
    public static String getProject() throws Exception {
        String valiny = DatabaseProperties.getProperties().getProperty("project");
        if (valiny == null || valiny.isEmpty() || valiny.isBlank()) {
            throw new Exception("Project non défini");
        }
        
        return valiny;
    }
}
