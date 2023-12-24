package model.service;

import java.sql.Connection;
import model.table.Etudiant;
import model.dao.EtudiantDAO;
import model.daoImpl.EtudiantDAOImpl;

public class EtudiantService {
	private static EtudiantDAO etudiantDAO = new EtudiantDAOImpl();

	public static Etudiant[] getAll(Connection con) throws Exception {
		return EtudiantService.etudiantDAO.getAll(con);
	}

	public void insert(Connection con, Etudiant etudiant) throws Exception {
		EtudiantService.etudiantDAO.insert(con, etudiant);
	}

	public void update(Connection con, Etudiant etudiant) throws Exception {
		EtudiantService.etudiantDAO.update(con, etudiant);
	}

	public void delete(Connection con, Etudiant etudiant) throws Exception {
		EtudiantService.etudiantDAO.delete(con, etudiant);
	}
}
