package course.concurrent;

import course.model.Affichage;
import course.model.Course;
import course.model.Participation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Partie 2 – Simulation concurrente : un thread par voiture, ou pool fixe de threads. */
public class SimulateurConcurrent {
    public static final long DELAI_PAR_DEFAUT_MS = 500;

    private final long delaiMs;

    public SimulateurConcurrent(long delaiMs) {
        this.delaiMs = delaiMs;
    }

    /** Stratégie 1 : un Thread par voiture, attente avec join(). */
    public void executerAvecThreads(Course course, PrintStream out) {
        ClassementPartage classement = preparer(course, out);
        List<Thread> threads = new ArrayList<>();
        for (Participation p : course.getParticipations()) {
            Thread t = new Thread(new VoitureRunnable(p, course.getDistanceTotale(), delaiMs, classement, out),
                    "voiture-" + p.getVoitureId());
            threads.add(t);
            t.start();
        }
        try {
            for (Thread t : threads) {
                t.join(); // le classement final n'est affiché qu'après la fin de tous les threads
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            for (Thread t : threads) {
                t.interrupt();
            }
            throw new IllegalStateException("Course interrompue avant la fin.", e);
        }
        terminer(course, classement, out);
    }

    /** Stratégie 2 : pool fixe de threads (ExecutorService). */
    public void executerAvecPool(Course course, int taillePool, PrintStream out) {
        ClassementPartage classement = preparer(course, out);
        ExecutorService pool = Executors.newFixedThreadPool(taillePool);
        try {
            for (Participation p : course.getParticipations()) {
                pool.submit(new VoitureRunnable(p, course.getDistanceTotale(), delaiMs, classement, out));
            }
            pool.shutdown(); // plus de nouvelles tâches ; les tâches soumises vont jusqu'au bout
            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                pool.shutdownNow();
                throw new IllegalStateException("Délai dépassé : course arrêtée.");
            }
        } catch (InterruptedException e) {
            pool.shutdownNow(); // interruption immédiate des tâches en cours
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Course interrompue avant la fin.", e);
        }
        terminer(course, classement, out);
    }

    private ClassementPartage preparer(Course course, PrintStream out) {
        course.demarrer();
        out.println("========== COURSE CONCURRENTE ==========");
        out.println("Distance : " + Affichage.km(course.getDistanceTotale()) + " km");
        StringBuilder sb = new StringBuilder();
        for (Participation p : course.getParticipations()) {
            sb.append(sb.length() == 0 ? "" : ", ").append(p.getNom());
        }
        out.println("Participants : " + sb);
        return new ClassementPartage();
    }

    private void terminer(Course course, ClassementPartage classement, PrintStream out) {
        course.terminer(classement.getOrdre());
        Affichage.resultats(course, out);
    }
}
