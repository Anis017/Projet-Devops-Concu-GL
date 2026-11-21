package course.model;

/**
 * Voiture enregistrée dans le garage. Entité persistante : elle ne porte PAS la distance
 * parcourue (état propre à une course, voir {@link Participation}).
 */
public class Voiture {
    private final String id;
    private String nom;
    private String marque;
    private double vitesse; // km par tour

    public Voiture(String id, String nom, String marque, double vitesse) {
        this.id = Validation.texteNonVide("identifiant", id);
        this.nom = Validation.texteNonVide("nom", nom);
        this.marque = Validation.texteNonVide("marque", marque);
        this.vitesse = Validation.strictementPositif("vitesse", vitesse);
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getMarque() {
        return marque;
    }

    public double getVitesse() {
        return vitesse;
    }

    public void setNom(String nom) {
        this.nom = Validation.texteNonVide("nom", nom);
    }

    public void setMarque(String marque) {
        this.marque = Validation.texteNonVide("marque", marque);
    }

    public void setVitesse(double vitesse) {
        this.vitesse = Validation.strictementPositif("vitesse", vitesse);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nom + " (" + marque + ") - " + Affichage.km(vitesse) + " km/tour";
    }
}
