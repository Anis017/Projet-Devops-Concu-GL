package course.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Une course : configuration, participants, état et résultats. */
public class Course {
    public enum Statut { CONFIGUREE, EN_COURS, TERMINEE }

    private final String id;
    private final String nom;
    private final double distanceTotale;
    private final List<Participation> participations = new ArrayList<>();
    private List<Participation> classement = new ArrayList<>();
    private int toursTotal;
    private Statut statut = Statut.CONFIGUREE;

    public Course(String id, String nom, double distanceTotale) {
        this.id = Validation.texteNonVide("identifiant de course", id);
        this.nom = Validation.texteNonVide("nom de course", nom);
        this.distanceTotale = Validation.strictementPositif("distance de la course", distanceTotale);
    }

    public void ajouterParticipant(Voiture voiture) {
        if (statut != Statut.CONFIGUREE) {
            throw new ValidationException("Impossible d'inscrire une voiture : la course n'est plus en configuration.");
        }
        if (contientVoiture(voiture.getId())) {
            throw new ValidationException("La voiture '" + voiture.getId() + "' est déjà inscrite à cette course.");
        }
        participations.add(new Participation(voiture));
    }

    public boolean contientVoiture(String voitureId) {
        for (Participation p : participations) {
            if (p.getVoitureId().equals(voitureId)) {
                return true;
            }
        }
        return false;
    }

    /** Valide la configuration (au moins deux voitures, course non démarrée). */
    public void valider() {
        if (statut != Statut.CONFIGUREE) {
            throw new ValidationException("Cette course a déjà été lancée : créez une nouvelle course.");
        }
        if (participations.size() < 2) {
            throw new ValidationException("Une course nécessite au moins 2 voitures (actuellement : "
                    + participations.size() + ").");
        }
    }

    public void demarrer() {
        valider();
        for (Participation p : participations) {
            p.reinitialiser();
        }
        statut = Statut.EN_COURS;
    }

    public boolean toutesArrivees() {
        for (Participation p : participations) {
            if (!p.isArrivee()) {
                return false;
            }
        }
        return true;
    }

    /** Enregistre le classement final et clôt la course. */
    public void terminer(List<Participation> classementFinal) {
        this.classement = new ArrayList<>(classementFinal);
        int max = 0;
        for (Participation p : participations) {
            max = Math.max(max, p.getTourArrivee());
        }
        this.toursTotal = max;
        this.statut = Statut.TERMINEE;
    }

    /** Une voiture inscrite à une course non terminée est « engagée ». */
    public boolean engage(String voitureId) {
        return statut != Statut.TERMINEE && contientVoiture(voitureId);
    }

    public Participation getGagnant() {
        return classement.isEmpty() ? null : classement.get(0);
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public double getDistanceTotale() {
        return distanceTotale;
    }

    public Statut getStatut() {
        return statut;
    }

    public int getToursTotal() {
        return toursTotal;
    }

    public List<Participation> getParticipations() {
        return Collections.unmodifiableList(participations);
    }

    public List<Participation> getClassement() {
        return Collections.unmodifiableList(classement);
    }
}
