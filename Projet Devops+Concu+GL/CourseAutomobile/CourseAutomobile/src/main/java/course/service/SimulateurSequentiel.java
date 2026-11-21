package course.service;

import course.model.Affichage;
import course.model.Course;
import course.model.Participation;

import java.io.PrintStream;

/** F3/F4 – Simulation séquentielle tour par tour (sans threads). */
public class SimulateurSequentiel {

    public void simuler(Course course, PrintStream out) {
        course.demarrer();
        double total = course.getDistanceTotale();
        out.println("========== COURSE " + course.getNom() + " (" + Affichage.km(total) + " km) ==========");
        int tour = 0;
        while (!course.toutesArrivees()) {
            tour++;
            out.println("--- Tour " + tour + " ---");
            for (Participation p : course.getParticipations()) {
                if (p.isArrivee()) {
                    continue; // une voiture arrivée ne progresse plus
                }
                p.avancer(total);
                out.println("[" + p.getNom() + "] " + Affichage.km(p.getDistanceParcourue()) + " / "
                        + Affichage.km(total) + " km");
                if (p.isArrivee()) {
                    out.println(p.getNom() + " franchit la ligne d'arrivée !");
                }
            }
        }
        course.terminer(RegleClassement.classer(course));
        Affichage.resultats(course, out);
    }
}
