package model.table;

import database.annotation.Column;
import database.annotation.Table;

@Table(name = "etudiant")
public class Etudiant {
	@Column(name = "id", isPrimaryKey = true, isAutoIncrement = true)
	private int id;
	@Column(name = "nom")
	private String nom;
	@Column(name = "prenoms")
	private String prenoms;
	@Column(name = "idclasse")
	private int idclasse;

	public Etudiant() {

	}

	// getters
	public int getId() {
		return this.id;
	}
	public String getNom() {
		return this.nom;
	}
	public String getPrenoms() {
		return this.prenoms;
	}
	public int getIdclasse() {
		return this.idclasse;
	}

	// setters
	public void setId(int id) {
		this.id = id;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public void setPrenoms(String prenoms) {
		this.prenoms = prenoms;
	}
	public void setIdclasse(int idclasse) {
		this.idclasse = idclasse;
	}
}
