package model.dao;

import java.sql.Connection;
import model.table.Etudiant;

public interface EtudiantDAO extends DAO<Etudiant> {
	@Override
	Etudiant[] getAll(Connection con) throws Exception;

	@Override
	void insert(Connection con, Etudiant etudiant) throws Exception;

	@Override
	void update(Connection con, Etudiant etudiant) throws Exception;

	@Override
	void delete(Connection con, Etudiant etudiant) throws Exception;
}
