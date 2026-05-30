package com.hico.assetsmanager.model;

public class Bien {
    private int id;
    private String nom;
    private String type;
    private String description;
    private double valeur;
    private String dateCreation;
    private String dateModification;
    private String dateConsultation;
    private String dateSuppression;

    public Bien() {
    }

    public Bien(int id, String nom, String type, String description, double valeur,
                String dateCreation, String dateModification, String dateConsultation,
                String dateSuppression) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.description = description;
        this.valeur = valeur;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.dateConsultation = dateConsultation;
        this.dateSuppression = dateSuppression;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getValeur() { return valeur; }
    public void setValeur(double valeur) { this.valeur = valeur; }
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
    public String getDateModification() { return dateModification; }
    public void setDateModification(String dateModification) { this.dateModification = dateModification; }
    public String getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(String dateConsultation) { this.dateConsultation = dateConsultation; }
    public String getDateSuppression() { return dateSuppression; }
    public void setDateSuppression(String dateSuppression) { this.dateSuppression = dateSuppression; }
}
