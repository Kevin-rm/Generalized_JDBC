package model.dao;

import java.sql.Connection;

public interface DAO<T> {
	/* Ci-dessous sont des methodes basiques de CRUD */

	T[] getAll(Connection con) throws Exception;

	void insert(Connection con, T t) throws Exception;

	void update(Connection con, T t) throws Exception;

	void delete(Connection con, T t) throws Exception;
}
