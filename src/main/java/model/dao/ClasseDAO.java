package model.dao;

import java.sql.Connection;
import model.table.Classe;

public interface ClasseDAO {
	/* Ci-dessous sont des methodes basiques de CRUD */

	Classe[] getAllClasses(Connection con) throws Exception;

	void insert(Connection con,Classe classe) throws Exception;

	void update(Connection con, Classe classe) throws Exception;

	void delete(Connection con, Classe classe) throws Exception;
}
