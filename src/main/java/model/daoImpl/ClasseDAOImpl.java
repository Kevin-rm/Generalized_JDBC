package model.daoImpl;

import database.annotation.Table;
import database.dataAccess.CRUDManager;
import java.sql.Connection;
import java.util.Arrays;
import model.table.Classe;
import model.dao.ClasseDAO;

public class ClasseDAOImpl implements ClasseDAO {
	private static final String SGBD = "postgres";

	/*
	 * Ci-dessous sont des codes auto-generated, ils offrent une overview de l'utilisation de la classe CRUDManager
	 * Vous pouvez les modifier selon vos guises et selon votre propre logique
	 * Verifier juste que vos requetes SELECT ne contiennent pas d'erreurs
	 */

	@Override
	public Classe[] getAllClasses(Connection con) throws Exception {
		Table sqlTable = Classe.class.getAnnotation(Table.class);
		if (sqlTable == null) {
			throw new Exception("Annotation absente pour la classe " + Classe.class.getName());
		}

		Object[] objects = CRUDManager.select(ClasseDAOImpl.SGBD, con, Classe.class, "SELECT * FROM " + sqlTable.name());
		return Arrays.copyOf(objects, objects.length, Classe[].class);
	}

	@Override
	public void insert(Connection con, Classe classe) throws Exception {
		CRUDManager.insert(ClasseDAOImpl.SGBD, con, classe);
	}

	@Override
	public void update(Connection con, Classe classe) throws Exception {
		CRUDManager.update(ClasseDAOImpl.SGBD, con, classe);
	}

	@Override
	public void delete(Connection con, Classe classe) throws Exception {
		CRUDManager.delete(ClasseDAOImpl.SGBD, con, classe);
	}
}
