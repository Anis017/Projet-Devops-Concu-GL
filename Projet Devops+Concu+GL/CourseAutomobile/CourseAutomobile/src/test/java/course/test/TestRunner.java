package course.test;

import course.concurrent.ClassementPartage;
import course.concurrent.SimulateurConcurrent;
import course.model.Course;
import course.model.Participation;
import course.model.ValidationException;
import course.model.Voiture;
import course.service.GestionCourse;
import course.service.GestionVoitures;
import course.service.SimulateurSequentiel;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests automatisés sans dépendance externe (JUnit non requis).
 * Code de sortie non nul si au moins un test échoue : l'étape Test de Jenkins échoue alors.
 * Les tests de la version séquentielle sont déterministes ; les tests concurrents ne vérifient
 * jamais l'ordre d'ordonnancement des threads.
 */
public class TestRunner {
    private static final PrintStream SILENCE = new PrintStream(OutputStream.nullOutputStream());
    private static int total = 0;
    private static int echecs = 0;

    private interface Test {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        test("T1 creation d'une voiture", TestRunner::creationVoiture);
        test("T2 validation des donnees voiture (vitesse <= 0, champs vides)", TestRunner::validationVoiture);
        test("T3 identifiants en double refuses", TestRunner::identifiantDouble);
        test("T4 progression et plafonnement de la distance", TestRunner::progression);
        test("T5 detection de l'arrivee et fin de simulation", TestRunner::detectionArrivee);
        test("T6 validation de la course (distance, >= 2 voitures, double inscription)", TestRunner::validationCourse);
        test("T7 coherence du classement (vitesses differentes)", TestRunner::classementCoherent);
        test("T8 regle de departage (meme tour, temps theorique, identifiant)", TestRunner::departage);
        test("T9 voiture engagee non modifiable ni supprimable", TestRunner::voitureEngagee);
        test("T10 classement partage thread-safe (sans doublon)", TestRunner::classementPartage);
        test("T11 course concurrente : threads et pool terminent avec tous les participants", TestRunner::concurrent);

        System.out.println();
        System.out.println(total + " tests executes, " + echecs + " echec(s).");
        System.exit(echecs == 0 ? 0 : 1);
    }

    private static void test(String nom, Test t) {
        total++;
        try {
            t.run();
            System.out.println("[OK]    " + nom);
        } catch (Throwable e) {
            echecs++;
            System.out.println("[ECHEC] " + nom + " -> " + e);
        }
    }

    // ---- utilitaires ----
    private static void verifier(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void egal(Object attendu, Object obtenu, String message) {
        verifier(attendu.equals(obtenu), message + " (attendu : " + attendu + ", obtenu : " + obtenu + ")");
    }

    private static void refuse(Runnable action, String message) {
        try {
            action.run();
        } catch (ValidationException e) {
            verifier(e.getMessage() != null && !e.getMessage().isEmpty(), "message d'erreur explicite attendu");
            return;
        }
        throw new AssertionError("ValidationException attendue : " + message);
    }

    private static Course course(double distance, Voiture... voitures) {
        Course c = new Course("C", "Test", distance);
        for (Voiture v : voitures) {
            c.ajouterParticipant(v);
        }
        return c;
    }

    private static List<String> noms(List<Participation> l) {
        List<String> r = new ArrayList<>();
        for (Participation p : l) {
            r.add(p.getNom());
        }
        return r;
    }

    // ---- tests ----
    private static void creationVoiture() {
        Voiture v = new Voiture("V1", "Ferrari", "Ferrari", 20);
        egal("V1", v.getId(), "id");
        egal("Ferrari", v.getNom(), "nom");
        egal(20.0, v.getVitesse(), "vitesse");
        GestionVoitures g = new GestionVoitures(id -> false);
        g.ajouter("V2", "BMW", "BMW", 15);
        egal(1, g.lister().size(), "taille du garage");
        verifier(g.rechercher("V2").isPresent(), "recherche par identifiant");
        verifier(!g.rechercher("XX").isPresent(), "recherche d'un identifiant inconnu");
    }

    private static void validationVoiture() {
        refuse(() -> new Voiture("V", "n", "m", 0), "vitesse = 0");
        refuse(() -> new Voiture("V", "n", "m", -5), "vitesse negative");
        refuse(() -> new Voiture("V", "n", "m", Double.NaN), "vitesse NaN");
        refuse(() -> new Voiture("", "n", "m", 10), "id vide");
        refuse(() -> new Voiture("V", "  ", "m", 10), "nom vide");
    }

    private static void identifiantDouble() {
        GestionVoitures g = new GestionVoitures(id -> false);
        g.ajouter("V1", "A", "A", 10);
        refuse(() -> g.ajouter("V1", "B", "B", 12), "identifiant en double");
        egal(1, g.lister().size(), "le doublon ne doit pas etre ajoute");
    }

    private static void progression() {
        Participation p = new Participation(new Voiture("V1", "A", "A", 30));
        p.avancer(100);
        egal(30.0, p.getDistanceParcourue(), "apres 1 tour");
        p.avancer(100);
        p.avancer(100);
        egal(90.0, p.getDistanceParcourue(), "apres 3 tours");
        verifier(!p.isArrivee(), "pas encore arrivee a 90 km");
        p.avancer(100);
        egal(100.0, p.getDistanceParcourue(), "distance plafonnee a la distance totale");
        egal(4, p.getTourArrivee(), "tour d'arrivee");
        p.avancer(100);
        egal(100.0, p.getDistanceParcourue(), "une voiture arrivee ne progresse plus");
        egal(4, p.getTourArrivee(), "le tour d'arrivee ne change plus");
    }

    private static void detectionArrivee() {
        Course c = course(100, new Voiture("V1", "Ferrari", "F", 20), new Voiture("V2", "Renault", "R", 10));
        new SimulateurSequentiel().simuler(c, SILENCE);
        egal(Course.Statut.TERMINEE, c.getStatut(), "statut final");
        verifier(c.toutesArrivees(), "toutes les voitures sont arrivees");
        egal(10, c.getToursTotal(), "nombre total de tours");
        for (Participation p : c.getParticipations()) {
            egal(100.0, p.getDistanceParcourue(), "distance finale de " + p.getNom());
        }
    }

    private static void validationCourse() {
        refuse(() -> new Course("C", "n", 0), "distance = 0");
        refuse(() -> new Course("C", "n", -10), "distance negative");
        Course c = course(100, new Voiture("V1", "A", "A", 10));
        refuse(c::valider, "une seule voiture");
        refuse(() -> new SimulateurSequentiel().simuler(c, SILENCE), "demarrage avec une seule voiture");
        Voiture v = new Voiture("V1", "A", "A", 10);
        refuse(() -> c.ajouterParticipant(v), "inscription multiple de la meme voiture");
        c.ajouterParticipant(new Voiture("V2", "B", "B", 10));
        c.valider(); // ne doit plus lever d'exception
    }

    private static void classementCoherent() {
        Course c = course(100,
                new Voiture("V3", "Renault", "R", 10),
                new Voiture("V1", "Ferrari", "F", 20),
                new Voiture("V2", "BMW", "B", 15));
        new SimulateurSequentiel().simuler(c, SILENCE);
        egal(List.of("Ferrari", "BMW", "Renault"), noms(c.getClassement()), "ordre du classement");
        egal("Ferrari", c.getGagnant().getNom(), "gagnant");
        egal(5, c.getClassement().get(0).getTourArrivee(), "tours Ferrari");
        egal(7, c.getClassement().get(1).getTourArrivee(), "tours BMW");
        egal(10, c.getClassement().get(2).getTourArrivee(), "tours Renault");
    }

    private static void departage() {
        // 30 km/tour et 25 km/tour arrivent tous deux au tour 4 sur 100 km : le plus rapide gagne,
        // meme si son identifiant est le plus grand.
        Course c = course(100, new Voiture("A", "Lent", "L", 25), new Voiture("Z", "Rapide", "R", 30));
        new SimulateurSequentiel().simuler(c, SILENCE);
        egal(List.of("Rapide", "Lent"), noms(c.getClassement()), "temps theorique");
        // vitesses egales : departage par identifiant
        Course c2 = course(100, new Voiture("B", "Deux", "d", 20), new Voiture("A", "Un", "u", 20));
        new SimulateurSequentiel().simuler(c2, SILENCE);
        egal(List.of("Un", "Deux"), noms(c2.getClassement()), "departage par identifiant");
    }

    private static void voitureEngagee() {
        GestionCourse gc = new GestionCourse();
        GestionVoitures g = new GestionVoitures(gc::voitureEngagee);
        g.ajouter("V1", "A", "A", 10);
        g.ajouter("V2", "B", "B", 12);
        g.ajouter("V3", "C", "C", 14);
        gc.creer("C1", "Course", 50);
        gc.inscrire(g.obtenir("V1"));
        gc.inscrire(g.obtenir("V2"));
        refuse(() -> g.modifier("V1", "X", null, null), "modification d'une voiture engagee");
        refuse(() -> g.supprimer("V2"), "suppression d'une voiture engagee");
        g.modifier("V3", "Nouveau", null, 20.0); // non engagee : autorise
        egal("Nouveau", g.obtenir("V3").getNom(), "modification autorisee");
        new SimulateurSequentiel().simuler(gc.getCourse().get(), SILENCE);
        g.modifier("V1", "Libre", null, null); // course terminee : autorise
        g.supprimer("V2");
        egal(2, g.lister().size(), "suppression apres la fin de la course");
    }

    private static void classementPartage() throws Exception {
        final int n = 50;
        final ClassementPartage classement = new ClassementPartage();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            final Participation p = new Participation(new Voiture("V" + i, "N" + i, "M", 10));
            for (int k = 0; k < 2; k++) { // chaque voiture tente de s'enregistrer deux fois
                Thread t = new Thread(() -> classement.enregistrer(p));
                threads.add(t);
            }
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        egal(n, classement.taille(), "aucun doublon, aucune perte");
        Set<String> ids = new HashSet<>();
        for (Participation p : classement.getOrdre()) {
            ids.add(p.getVoitureId());
        }
        egal(n, ids.size(), "identifiants distincts");
    }

    private static void concurrent() {
        for (int mode = 0; mode < 2; mode++) {
            Course c = new Course("C", "Concurrent", 40);
            for (int i = 1; i <= 10; i++) {
                c.ajouterParticipant(new Voiture("V" + i, "Voiture" + i, "M", 10 + i));
            }
            SimulateurConcurrent sim = new SimulateurConcurrent(1);
            if (mode == 0) {
                sim.executerAvecThreads(c, SILENCE);
            } else {
                sim.executerAvecPool(c, 3, SILENCE);
            }
            egal(Course.Statut.TERMINEE, c.getStatut(), "statut final (mode " + mode + ")");
            egal(10, c.getClassement().size(), "tous les participants classes (mode " + mode + ")");
            Set<String> ids = new HashSet<>();
            for (Participation p : c.getClassement()) {
                ids.add(p.getVoitureId());
                verifier(p.isArrivee(), "chaque voiture a franchi la ligne");
            }
            egal(10, ids.size(), "aucun doublon dans le classement (mode " + mode + ")");
        }
    }
}
