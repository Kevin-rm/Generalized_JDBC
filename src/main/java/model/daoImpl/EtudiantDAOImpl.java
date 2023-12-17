package model.daoImpl;

import database.annotation.Table;
import database.dataAccess.CRUDManager;
import java.sql.Connection;
import java.util.Arrays;
import model.table.Etudiant;
import model.dao.EtudiantDAO;

public class EtudiantDAOImpl implements EtudiantDAO {
	private static final String SGBD = "postgres";

	/*
	 * Ci-dessous sont des codes auto-generated, ils offrent une overview de l'utilisation de la classe CRUDManager
	 * Vous pouvez les modifier selon vos guises et selon votre propre logique
	 * Verifier juste que vos requetes SELECT ne contiennent pas d'erreurs
	 */

	@Override
	public Etudiant[] getAllEtudiants(Connection con) throws Exception {
		Table sqlTable = Etudiant.class.getAnnotation(Table.class);
		if (sqlTable == null) {
			throw new Exception("Annotation absente pour la classe " + Etudiant.class.getName());
		}

		Object[] objects = CRUDManager.select(EtudiantDAOImpl.SGBD, con, Etudiant.class, "SELECT * FROM " + sqlTable.name());
		return Arrays.copyOf(objects, objects.length, Etudiant[].class);
	}

	@Override
	public void insert(Connection con, Etudiant etudiant) throws Exception {
		CRUDManager.insert(EtudiantDAOImpl.SGBD, con, etudiant);
	}

	@Override
	public void update(Connection con, Etudiant etudiant) throws Exception {
		CRUDManager.update(EtudiantDAOImpl.SGBD, con, etudiant);
	}

	@Override
	public void delete(Connection con, Etudiant etudiant) throws Exception {
		CRUDManager.delete(EtudiantDAOImpl.SGBD, con, etudiant);
	}
}
