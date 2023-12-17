package model.table;

import database.annotation.Column;
import database.annotation.Table;

@Table(name = "classe")
public class Classe {
	@Column(name = "id", isPrimaryKey = true, isAutoIncrement = true)
	private int id;
	@Column(name = "niveau_classe")
	private int niveauClasse;
	@Column(name = "num_classe")
	private int numClasse;

	public Classe() {

	}

	// getters
	public int getId() {
		return this.id;
	}
	public int getNiveauClasse() {
		return this.niveauClasse;
	}
	public int getNumClasse() {
		return this.numClasse;
	}

	// setters
	public void setId(int id) {
		this.id = id;
	}
	public void setNiveauClasse(int niveauClasse) {
		this.niveauClasse = niveauClasse;
	}
	public void setNumClasse(int numClasse) {
		this.numClasse = numClasse;
	}
}
