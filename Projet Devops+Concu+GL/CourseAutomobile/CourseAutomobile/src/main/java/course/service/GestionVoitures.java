package course.service;

import course.model.Validation;
import course.model.ValidationException;
import course.model.Voiture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** F1 – Gestion des voitures (garage). */
public class GestionVoitures {
    private final Map<String, Voiture> voitures = new LinkedHashMap<>();
    private final Predicate<String> voitureEngagee;

    /** @param voitureEngagee indique si une voiture est inscrite à une course non terminée */
    public GestionVoitures(Predicate<String> voitureEngagee) {
        this.voitureEngagee = voitureEngagee;
    }

    public Voiture ajouter(String id, String nom, String marque, double vitesse) {
        Voiture v = new Voiture(id, nom, marque, vitesse); // valide les champs
        if (voitures.containsKey(v.getId())) {
            throw new ValidationException("L'identifiant '" + v.getId() + "' existe déjà.");
        }
        voitures.put(v.getId(), v);
        return v;
    }

    public List<Voiture> lister() {
        return new ArrayList<>(voitures.values());
    }

    public Optional<Voiture> rechercher(String id) {
        return Optional.ofNullable(id == null ? null : voitures.get(id.trim()));
    }

    public Voiture obtenir(String id) {
        return rechercher(id).orElseThrow(
                () -> new ValidationException("Aucune voiture avec l'identifiant '" + id + "'."));
    }

    /** Les paramètres null sont conservés. Validation avant toute modification (atomique). */
    public Voiture modifier(String id, String nom, String marque, Double vitesse) {
        Voiture v = obtenir(id);
        interdireSiEngagee(v, "modifiée");
        String n = nom == null ? null : Validation.texteNonVide("nom", nom);
        String m = marque == null ? null : Validation.texteNonVide("marque", marque);
        Double s = vitesse == null ? null : Validation.strictementPositif("vitesse", vitesse);
        if (n != null) {
            v.setNom(n);
        }
        if (m != null) {
            v.setMarque(m);
        }
        if (s != null) {
            v.setVitesse(s);
        }
        return v;
    }

    public void supprimer(String id) {
        Voiture v = obtenir(id);
        interdireSiEngagee(v, "supprimée");
        voitures.remove(v.getId());
    }

    private void interdireSiEngagee(Voiture v, String action) {
        if (voitureEngagee.test(v.getId())) {
            throw new ValidationException("La voiture '" + v.getId()
                    + "' est inscrite à une course non terminée : elle ne peut pas être " + action + ".");
        }
    }
}
