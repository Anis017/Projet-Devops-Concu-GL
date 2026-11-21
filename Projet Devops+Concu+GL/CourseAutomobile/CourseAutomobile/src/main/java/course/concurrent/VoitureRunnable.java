package course.concurrent;

import course.model.Affichage;
import course.model.Participation;

import java.io.PrintStream;

/** Tâche indépendante simulant la progression d'une voiture. */
public class VoitureRunnable implements Runnable {
    private final Participation participation;
    private final double distanceTotale;
    private final long delaiMs;
    private final ClassementPartage classement;
    private final PrintStream out;

    public VoitureRunnable(Participation participation, double distanceTotale, long delaiMs,
                           ClassementPartage classement, PrintStream out) {
        this.participation = participation;
        this.distanceTotale = distanceTotale;
        this.delaiMs = delaiMs;
        this.classement = classement;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            while (!participation.isArrivee()) {
                participation.avancer(distanceTotale);
                out.println("[" + participation.getNom() + "] "
                        + Affichage.km(participation.getDistanceParcourue()) + " / "
                        + Affichage.km(distanceTotale) + " km");
                if (!participation.isArrivee()) {
                    Thread.sleep(delaiMs); // délai identique pour toutes les voitures
                }
            }
            int position = classement.enregistrer(participation);
            if (position > 0) {
                out.println(participation.getNom() + " franchit la ligne d'arrivée ! (position " + position + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // on restaure le statut d'interruption
            out.println("[" + participation.getNom() + "] interrompue.");
        }
    }
}
