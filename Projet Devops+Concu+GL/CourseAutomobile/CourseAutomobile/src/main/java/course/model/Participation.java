package course.model;

/**
 * Participation d'une voiture à une course : conserve la distance parcourue et le tour d'arrivée.
 * Les caractéristiques de la voiture sont copiées à l'inscription (les résultats restent
 * donc stables même si la voiture est modifiée après la course).
 */
public class Participation {
    private static final double EPSILON = 1e-9;

    private final String voitureId;
    private final String nom;
    private final String marque;
    private final double vitesse;
    private double distanceParcourue;
    private int toursEffectues;
    private int tourArrivee; // 0 tant que la ligne n'est pas franchie
    private boolean arrivee;

    public Participation(Voiture voiture) {
        this.voitureId = voiture.getId();
        this.nom = voiture.getNom();
        this.marque = voiture.getMarque();
        this.vitesse = voiture.getVitesse();
    }

    /** Remet la distance à zéro au début de chaque course. */
    public void reinitialiser() {
        distanceParcourue = 0;
        toursEffectues = 0;
        tourArrivee = 0;
        arrivee = false;
    }

    /** Fait avancer d'un tour ; la distance est plafonnée à la distance totale. */
    public void avancer(double distanceTotale) {
        if (arrivee) {
            return;
        }
        toursEffectues++;
        distanceParcourue = Math.min(distanceTotale, distanceParcourue + vitesse);
        if (distanceParcourue >= distanceTotale - EPSILON) {
            distanceParcourue = distanceTotale;
            arrivee = true;
            tourArrivee = toursEffectues;
        }
    }

    /** Temps d'arrivée théorique en tours : distance totale / vitesse. */
    public double tempsTheorique(double distanceTotale) {
        return distanceTotale / vitesse;
    }

    public String getVoitureId() {
        return voitureId;
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

    public double getDistanceParcourue() {
        return distanceParcourue;
    }

    public int getTourArrivee() {
        return tourArrivee;
    }

    public boolean isArrivee() {
        return arrivee;
    }
}
