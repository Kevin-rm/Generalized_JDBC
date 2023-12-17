package database.util;

import database.annotation.Column;
import database.annotation.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;

public class QueryConstructor {
    private QueryConstructor() {
        
    }
    
    public static String getInsertQuery(String sgbd, Object object) throws Exception {
        String valiny = new String();
           
        Class<?> c = object.getClass();
        Table sqlTable = c.getAnnotation(Table.class);
        
        if (sqlTable == null) {
            throw new Exception("Annotation absente pour la classe " + c.getName());
        }
        
        Field[] declaredFields = c.getDeclaredFields();

        try {
            valiny = "INSERT INTO " + sqlTable.name() + "(";
            for (int i = 0; i < declaredFields.length; i++) {
                // On recupere le nom du declaredFields[i] equivalent dans la base de donnees
                Column sqlField = declaredFields[i].getAnnotation(Column.class);
                
                // Si le champ est annote et n'est pas autoIncremente
                if (sqlField != null && !sqlField.isAutoIncrement()) {
                    valiny += sqlField.name();
                    
                    if (i != declaredFields.length - 1) { // Le dernier champ ne prendra pas de virgule
                        valiny += ", ";   
                    }   
                } else if (sqlField == null) {
                    throw new Exception("Annotation absente pour l'attribut " + declaredFields[i].getName());
                }
            }
            valiny += ") VALUES (";

            for (int i = 0; i < declaredFields.length; i++) {        
                Column sqlField = declaredFields[i].getAnnotation(Column.class);
                if (!sqlField.isAutoIncrement()) { // Si le champ n'est pas autoIncrement
                    /**
                     * On recupere chaque fonction getter associee a un champ
                     * Par exemple: getId(), getName(), etc...
                     */
                    Method getter = c.getMethod("get" + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1));
                    Object declaredFieldValue = getter.invoke(object);
                    
                    if (declaredFieldValue == null) {
                        valiny += "null";
                    } else {
                        /* On ajoute des simple quotes '' si le type est un String ou une date */
                        if (declaredFields[i].getType() == String.class || declaredFields[i].getType() == LocalDate.class) {
                            if (sgbd.compareToIgnoreCase("Oracle") == 0 && declaredFields[i].getType() == LocalDate.class) {
                                valiny += "TO_DATE('" + declaredFieldValue + "', 'YYYY-MM-DD')";
                            } else {
                                valiny += "'" + declaredFieldValue  + "'";
                            }
                        /* ------------------------- */
                        } else {
                            valiny += declaredFieldValue;
                        }
                    }
                    
                    if (i != declaredFields.length - 1) {
                        valiny += ", ";
                    }
                }
            }
            valiny += ")";
        } catch (Exception e) {
            throw new Exception("\nException dans QueryConstructor.getInsertQuery: " + e.getMessage());
        }

        return valiny;
    }
    
    /*
     * -> En supposons qu'il n'y a qu'un seul primary key dans chaque table
     * UPDATE nom_de_table SET column1 = object.getField1() ... WHERE primary_key_field = primary_key_fieldValue
     */
    public static String getUpdateQuery(String sgbd, Object object) throws Exception{        
        String valiny = new String();
        
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
        
        try {
            valiny = "UPDATE " + sqlTable.name() + " SET ";
            
            for (int i = 0; i < declaredFields.length; i++) {
                Column sqlField = declaredFields[i].getAnnotation(Column.class);
                if (sqlField != null) {
                    if (!sqlField.isPrimaryKey()) { // Le field n'est pas un primaryKey
                        Method getter = c.getMethod("get" + declaredFields[i].getName().substring(0, 1).toUpperCase() + declaredFields[i].getName().substring(1));

                        valiny += sqlField.name() + " = ";

                        /* On ajoute des simple quotes '' si le type est un String ou une date */
                        if (declaredFields[i].getType() == String.class || declaredFields[i].getType() == LocalDate.class) {
                            if (sgbd.compareToIgnoreCase("Oracle") == 0 && declaredFields[i].getType() == LocalDate.class) {
                                valiny += "TO_DATE('" + getter.invoke(object) + "', 'YYYY-MM-DD')";
                            } else {
                                valiny += "'" + getter.invoke(object)  + "'";
                            }
                        /* ------------------------- */
                        } else {
                            valiny += getter.invoke(object);
                        }

                        if (i != declaredFields.length - 1) {
                            valiny += ", ";
                        }
                    }
                } else {
                    throw new Exception("Annotation absente pour l' attribut " + declaredFields[i].getName());
                }
            }
            
            Column sqlFieldCondition = fieldCondition.getAnnotation(Column.class);
            if (sqlFieldCondition == null) {
                throw new Exception("Annotation absente pour l'attribut " + fieldCondition.getName());
            }
            
            valiny += " WHERE " + sqlFieldCondition.name() + " = " + fieldCondition.get(object);
        } catch (Exception e) {
            throw new Exception("\nException dans QueryConstructor.getUpdateQuery: " + e.getMessage());
        }
        
        return valiny;
    }
    
    /*
     * -> En supposons toujours qu'il n'y a qu'un seul primary key dans chaque table
     * On recherche le primary key et ce sera la colonne de condition
     * DELETE FROM nom_de_table WHERE primary_key_field = primary_key_fieldValue
     */
    public static String getDeleteQuery(Object object) throws Exception {
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
        
        return "DELETE FROM " + sqlTable.name()+ " WHERE " + sqlFieldCondition.name() + " = " + fieldCondition.get(object);
    }
}
