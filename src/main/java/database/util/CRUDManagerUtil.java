package database.util;

import database.annotation.Column;
import database.annotation.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
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
    
    private static Field getPrimaryKeyField(Class<?> c) {
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
    
    public static PreparedStatement getInsertPreparedStatement(Connection con, Object object) throws Exception {
        Class<?> c = object.getClass();
        Table sqlTable = c.getAnnotation(Table.class);
        if (sqlTable == null) {
            throw new Exception("Annotation absente pour la classe " + c.getName());
        }
        
        Field[] declaredFields = c.getDeclaredFields();
        
        PreparedStatement valiny = con.prepareStatement(QueryConstructor.getInsertQuery(sqlTable, declaredFields));

        int parameterIndex = 1;
        for (Field declaredField : declaredFields) {
            Column sqlField = declaredField.getAnnotation(Column.class);

            if (!sqlField.isAutoIncrement()) {
                Method getter = c.getMethod("get" + declaredField.getName().substring(0, 1).toUpperCase() + declaredField.getName().substring(1));
                Object declaredFieldValue = getter.invoke(object);

                if (declaredFieldValue == null) {
                    valiny.setNull(parameterIndex++, Types.NULL);
                } else {
                    if (declaredField.getType() == LocalDate.class) {
                        valiny.setDate(parameterIndex++, Date.valueOf((LocalDate) declaredFieldValue));
                    } else if (declaredField.getType() == LocalDateTime.class) {
                        valiny.setTimestamp(parameterIndex++, Timestamp.valueOf((LocalDateTime) declaredFieldValue));
                    } else if (declaredField.getType() == LocalTime.class) {
                        valiny.setTime(parameterIndex++, Time.valueOf((LocalTime) declaredFieldValue));
                    } else {
                        valiny.setObject(parameterIndex++, declaredFieldValue);
                    }
                }
            }
        }
        
        return valiny;
    }
    
    public static PreparedStatement getUpdatePreparedStatement(Connection con, Object object) throws Exception {
        Class<?> c = object.getClass();
        Table sqlTable = c.getAnnotation(Table.class);
        if (sqlTable == null) {
            throw new Exception("Annotation absente pour la classe " + c.getName());
        }
        
        Field[] declaredFields = c.getDeclaredFields();
        Field fieldCondition = CRUDManagerUtil.getPrimaryKeyField(c);
        if (fieldCondition == null) {
            throw new Exception("On ne peut pas updater l'objet de type " + c.getName() + " car il n'a pas de primary key");
        }
        fieldCondition.setAccessible(true);
        
        PreparedStatement valiny = con.prepareStatement(QueryConstructor.getUpdateQuery(sqlTable, declaredFields, fieldCondition));

        int parameterIndex = 1;
        for (Field declaredField : declaredFields) {
            Column sqlField = declaredField.getAnnotation(Column.class);
            
            if (!sqlField.isPrimaryKey()) {
                Method getter = c.getMethod("get" + declaredField.getName().substring(0, 1).toUpperCase() + declaredField.getName().substring(1));
                Object declaredFieldValue = getter.invoke(object);

                if (declaredField.getType() == LocalDate.class) {
                    valiny.setDate(parameterIndex++, Date.valueOf((LocalDate) declaredFieldValue));
                } else if (declaredField.getType() == LocalDateTime.class) {
                    valiny.setTimestamp(parameterIndex++, Timestamp.valueOf((LocalDateTime) declaredFieldValue));
                } else if (declaredField.getType() == LocalTime.class) {
                    valiny.setTime(parameterIndex++, Time.valueOf((LocalTime) declaredFieldValue));
                } else {
                    valiny.setObject(parameterIndex++, declaredFieldValue);
                }
            }
        }

        Object conditionValue = fieldCondition.get(object);
        valiny.setObject(parameterIndex, conditionValue);

        return valiny;
    }
    
    public static PreparedStatement getDeletePreparedStatement(Connection con, Object object) throws Exception {
        Class<?> c = object.getClass();
        
        Table sqlTable = c.getAnnotation(Table.class);
        if (sqlTable == null) {
            throw new Exception("Annotation absente pour la classe " + c.getName());
        }
        
        Field fieldCondition = CRUDManagerUtil.getPrimaryKeyField(c);
        if (fieldCondition == null) {
            throw new Exception("On ne peut pas supprimer l'objet de type " + c.getName() + " car il n'a pas de primary key");
        }
        fieldCondition.setAccessible(true);
        
        Column sqlFieldCondition = fieldCondition.getAnnotation(Column.class);
        if (sqlFieldCondition == null) {
            throw new Exception("Annotation absente pour l'attribut " + fieldCondition.getName());
        }
        
        PreparedStatement valiny = con.prepareStatement(QueryConstructor.getDeleteQuery(sqlTable, sqlFieldCondition));

        Object primaryKeyValue = fieldCondition.get(object);
        valiny.setObject(1, primaryKeyValue);

        return valiny;
    }
}
