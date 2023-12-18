package database.util;

import database.annotation.Column;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CRUDManagerUtil {
    private CRUDManagerUtil() {
        
    }
    
    // Cree un objet a partir d'une resultSet
    public static Object createObject(Class<?> c, ResultSet res) throws Exception {
        Object valiny = null;

        Constructor<?> construct = c.getConstructor(); // Recupere le constructeur sans arguments(vide)
        valiny = construct.newInstance(); // Initialise l'objet avec une instance de c

        Field[] declaredFields = c.getDeclaredFields();
        
        ResultSetMetaData resultSetMetaData = res.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();

        try {
            for (int i = 0; i < columnCount; i++) {
                String columnName = resultSetMetaData.getColumnName(i + 1);
                
                for (Field declaredField : declaredFields) {
                    if (declaredField.isAnnotationPresent(Column.class)) {
                        Column sqlColumn = declaredField.getAnnotation(Column.class);

                        if (columnName.equalsIgnoreCase(sqlColumn.name())) {
                            Object declaredFieldValue = new Object();

                            if (declaredField.getType() == LocalDate.class) {
                                declaredFieldValue = res.getDate(i + 1).toLocalDate(); // i + 1 car on commence a 1 mais pas a 0
                            } else if (declaredField.getType() == LocalDateTime.class) {
                                declaredFieldValue = res.getTimestamp(i + 1).toLocalDateTime();
                            } else if (declaredField.getType() == LocalTime.class) {
                                declaredFieldValue = res.getTime(i + 1).toLocalTime();
                            } else if (declaredField.getType() == Double.TYPE) {
                                declaredFieldValue = res.getDouble(i + 1);
                            } else if (declaredField.getType() == Integer.TYPE) {
                                declaredFieldValue = res.getInt(i + 1);
                            } else if (declaredField.getType() == String.class) {
                                declaredFieldValue = res.getString(i + 1);
                            }

                            /**
                             * On peut utiliser une autre methode:
                             * Cette autre methode consiste a recuperer la fonction setter correspondant au declaredFields 
                             */
                            declaredField.setAccessible(true); // on doit rendre le champ accessible a cause de l'encapsulation
                            declaredField.set(valiny, declaredFieldValue); // on met comme valeur du declaredField la valeur recuperee dans la base de donnees

                            break;
                        }
                    } else {
                        throw new Exception("Annotation absente pour l'attribut " + declaredField);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException("\nSQLException dans CRUDManagerUtil.createObject: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("\nException dans CRUDManagerUtil.createObject: " + e.getMessage());
        }

        return valiny;
    }
    
    public static Field getPrimaryKeyField(Class<?> c) {
        for (Field declaredField : c.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Column.class)) {
                Column sqlField = declaredField.getAnnotation(Column.class);
                if (sqlField.isPrimaryKey()) {
                    return declaredField;
                }
            }
        }
        return null;
    }
}
