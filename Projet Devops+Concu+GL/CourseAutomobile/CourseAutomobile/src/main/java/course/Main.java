package course;

import course.concurrent.SimulateurConcurrent;
import course.model.Course;
import course.service.GestionCourse;
import course.service.GestionVoitures;
import course.service.SimulateurSequentiel;
import course.ui.Menu;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Point d'entrée.
 * <pre>
 *   java course.Main                          menu interactif
 *   java course.Main --auto                   simulation automatique séquentielle (partie 1)
 *   java course.Main --auto --concurrent      idem avec un thread par voiture (partie 2)
 *   java course.Main --auto --pool            idem avec un pool fixe de 3 threads
 *   option : --delay=MS                       délai entre deux déplacements (défaut 500)
 * </pre>
 * Code de sortie : 0 si la simulation se termine normalement, 1 sinon.
 */
public class Main {

    public static void main(String[] args) throws UnsupportedEncodingException {
        // Sortie en UTF-8 quel que soit l'environnement (conteneur Docker sans locale, par exemple)
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, "UTF-8"));
        boolean auto = false;
        boolean threads = false;
        boolean pool = false;
        long delai = SimulateurConcurrent.DELAI_PAR_DEFAUT_MS;
        for (String a : args) {
            if (a.equals("--auto")) {
                auto = true;
            } else if (a.equals("--concurrent")) {
                threads = true;
            } else if (a.equals("--pool")) {
                pool = true;
            } else if (a.startsWith("--delay=")) {
                delai = Long.parseLong(a.substring("--delay=".length()));
            }
        }
        if (!auto) {
            new Menu(System.in, System.out, delai).run();
            return;
        }
        try {
            lancerAuto(threads, pool, delai);
        } catch (RuntimeException e) {
            System.err.println("Échec de la simulation : " + e.getMessage());
            System.exit(1);
        }
    }

    /** Jeu de données prédéfini : Ferrari 20, BMW 15, Renault 10 km/tour sur 100 km. */
    static void lancerAuto(boolean threads, boolean pool, long delai) {
        GestionCourse gc = new GestionCourse();
        GestionVoitures garage = new GestionVoitures(gc::voitureEngagee);
        garage.ajouter("V1", "Ferrari", "Ferrari", 20);
        garage.ajouter("V2", "BMW", "BMW", 15);
        garage.ajouter("V3", "Renault", "Renault", 10);
        Course c = gc.creer("C1", "Grand Prix automatique", 100);
        for (course.model.Voiture v : garage.lister()) {
            gc.inscrire(v);
        }
        if (pool) {
            new SimulateurConcurrent(delai).executerAvecPool(c, 3, System.out);
        } else if (threads) {
            new SimulateurConcurrent(delai).executerAvecThreads(c, System.out);
        } else {
            new SimulateurSequentiel().simuler(c, System.out);
        }
    }
}
