package model.dao;

import java.sql.Connection;
import model.table.Classe;

public interface ClasseDAO extends DAO<Classe> {
	@Override
	Classe[] getAll(Connection con) throws Exception;

	@Override
	void insert(Connection con, Classe classe) throws Exception;

	@Override
	void update(Connection con, Classe classe) throws Exception;

	@Override
	void delete(Connection con, Classe classe) throws Exception;
}
