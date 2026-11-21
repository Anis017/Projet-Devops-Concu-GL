package course.service;

import course.model.Course;
import course.model.Participation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Règle de classement de la partie 1 : tour d'arrivée, puis temps théorique
 * (distance totale / vitesse), puis identifiant.
 */
public final class RegleClassement {
    private RegleClassement() {
    }

    public static Comparator<Participation> comparateur(double distanceTotale) {
        return Comparator
                .comparingInt(Participation::getTourArrivee)
                .thenComparingDouble((Participation p) -> p.tempsTheorique(distanceTotale))
                .thenComparing(Participation::getVoitureId);
    }

    public static List<Participation> classer(Course course) {
        List<Participation> liste = new ArrayList<>(course.getParticipations());
        liste.sort(comparateur(course.getDistanceTotale()));
        return liste;
    }
}
