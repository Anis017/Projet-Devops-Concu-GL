package course.model;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

/** Utilitaires d'affichage console. */
public final class Affichage {
    private Affichage() {
    }

    /** 20.0 -> "20" ; 12.5 -> "12.5". */
    public static String km(double valeur) {
        if (Math.abs(valeur - Math.rint(valeur)) < 1e-9) {
            return String.valueOf((long) Math.rint(valeur));
        }
        return String.format(Locale.ROOT, "%.1f", valeur);
    }

    public static void infosCourse(Course c, PrintStream out) {
        out.println("------ Course " + c.getId() + " : " + c.getNom() + " ------");
        out.println("Distance totale : " + km(c.getDistanceTotale()) + " km");
        out.println("Statut          : " + c.getStatut());
        out.println("Participants (" + c.getParticipations().size() + ") :");
        for (Participation p : c.getParticipations()) {
            out.println("  - [" + p.getVoitureId() + "] " + p.getNom() + " (" + p.getMarque() + ") "
                    + km(p.getVitesse()) + " km/tour");
        }
    }

    public static void resultats(Course c, PrintStream out) {
        List<Participation> cl = c.getClassement();
        out.println("============== CLASSEMENT ==============");
        int rang = 1;
        for (Participation p : cl) {
            out.println(rang++ + ". " + p.getNom() + " - " + km(p.getDistanceParcourue()) + " km - "
                    + p.getTourArrivee() + " tours");
        }
        Participation g = c.getGagnant();
        if (g != null) {
            out.println("Gagnant : " + g.getNom());
        }
        out.println("Nombre total de tours de la simulation : " + c.getToursTotal());
        out.println("========================================");
    }
}
