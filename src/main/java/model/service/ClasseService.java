package model.service;

import java.sql.Connection;
import model.table.Classe;
import model.dao.ClasseDAO;
import model.daoImpl.ClasseDAOImpl;

public class ClasseService {
	private static ClasseDAO classeDAO = new ClasseDAOImpl();

	public static Classe[] getAll(Connection con) throws Exception {
		return ClasseService.classeDAO.getAll(con);
	}

	public void insert(Connection con, Classe classe) throws Exception {
		ClasseService.classeDAO.insert(con, classe);
	}

	public void update(Connection con, Classe classe) throws Exception {
		ClasseService.classeDAO.update(con, classe);
	}

	public void delete(Connection con, Classe classe) throws Exception {
		ClasseService.classeDAO.delete(con, classe);
	}
}
