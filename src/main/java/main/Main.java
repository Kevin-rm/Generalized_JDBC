package main;

import database.fileGenerator.FileGenerator; 

public class Main {
    public static void main(String[] args) throws Exception {
        FileGenerator.generateModelClassesFromDbTables("postgres");
    }
}
