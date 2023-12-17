package model.dao;

import java.sql.Connection;
import model.table.Etudiant;

public interface EtudiantDAO {
	/* Ci-dessous sont des methodes basiques de CRUD */

	Etudiant[] getAllEtudiants(Connection con) throws Exception;

	void insert(Connection con,Etudiant etudiant) throws Exception;

	void update(Connection con, Etudiant etudiant) throws Exception;

	void delete(Connection con, Etudiant etudiant) throws Exception;
}
