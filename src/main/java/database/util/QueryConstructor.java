package database.util;

import database.annotation.Column;
import database.annotation.Table;
import java.lang.reflect.Field;

public class QueryConstructor {
    private QueryConstructor() {
        
    }
    
    public static String getInsertQuery(Table sqlTable, Field[] declaredFields) throws Exception {
        try {
            StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + sqlTable.name() + " (");
            StringBuilder valuesBuilder = new StringBuilder(") VALUES (");
            
            for (int i = 0; i < declaredFields.length; i++) {
                Column sqlField = declaredFields[i].getAnnotation(Column.class);

                if (sqlField != null) {
                    if (!sqlField.isAutoIncrement()) {
                        queryBuilder.append(sqlField.name());

                        valuesBuilder.append("?");

                        if (i != declaredFields.length - 1) {
                            queryBuilder.append(", ");
                            valuesBuilder.append(", ");
                        }
                    }
                } else {
                    throw new Exception("Annotation absente pour l'attribut " + declaredFields[i].getName());
                }
            }

            queryBuilder.append(valuesBuilder).append(")");
            
            return queryBuilder.toString();
        } catch (Exception e) {
            throw new Exception("\nException dans QueryConstructor.getInsertQuery: " + e.getMessage());
        }
    }
    
    /*
     * -> En supposons qu'il n'y a qu'un seul primary key dans la table ou on veut faire update
     * UPDATE nom_de_table SET column1 = ? ... WHERE primary_key_field = ?
     */
    public static String getUpdateQuery(Table sqlTable, Field[] declaredFields, Field fieldCondition) throws Exception {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("UPDATE ").append(sqlTable.name()).append(" SET ");

            for (int i = 0; i < declaredFields.length; i++) {
                Column sqlField = declaredFields[i].getAnnotation(Column.class);
                if (sqlField != null) {
                    if (!sqlField.isPrimaryKey()) {
                        queryBuilder.append(sqlField.name()).append(" = ?");

                        if (i != declaredFields.length - 1) {
                            queryBuilder.append(", ");
                        }
                    }
                } else {
                    throw new Exception("Annotation absente pour l'attribut " + declaredFields[i].getName());
                }
            }

            Column sqlFieldCondition = fieldCondition.getAnnotation(Column.class);
            if (sqlFieldCondition == null) {
                throw new Exception("Annotation absente pour l'attribut " + fieldCondition.getName());
            }

            queryBuilder.append(" WHERE ").append(sqlFieldCondition.name()).append(" = ?");

            return queryBuilder.toString();
        } catch (Exception e) {
            throw new Exception("\nException dans QueryConstructor.getUpdateQuery: " + e.getMessage());
        }
    }
    
    /*
     * -> En supposons toujours qu'il n'y a qu'un seul primary key dans la table ou on veut faire delete
     * On recherche le primary key et ce sera la colonne de condition
     * DELETE FROM nom_de_table WHERE primary_key_field = ?
     */
    public static String getDeleteQuery(Table sqlTable, Column sqlFieldCondition) throws Exception {
        return "DELETE FROM " + sqlTable.name() + " WHERE " + sqlFieldCondition.name() + " = ?";
    }
}
