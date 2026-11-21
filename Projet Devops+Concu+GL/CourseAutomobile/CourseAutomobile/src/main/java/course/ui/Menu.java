package course.ui;

import course.concurrent.SimulateurConcurrent;
import course.model.Affichage;
import course.model.Course;
import course.model.ValidationException;
import course.model.Voiture;
import course.service.GestionCourse;
import course.service.GestionVoitures;
import course.service.SimulateurSequentiel;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Interface console (menu principal + saisies validées). */
public class Menu {
    private static final int POOL = 3;

    private final Scanner in;
    private final PrintStream out;
    private final GestionCourse gestionCourse = new GestionCourse();
    private final GestionVoitures garage = new GestionVoitures(gestionCourse::voitureEngagee);
    private final SimulateurSequentiel sequentiel = new SimulateurSequentiel();
    private final SimulateurConcurrent concurrent;

    /** Signale la fin du flux d'entrée (Ctrl+D ou fichier redirigé épuisé). */
    private static class FinEntree extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public Menu(InputStream entree, PrintStream sortie, long delaiConcurrentMs) {
        this.in = new Scanner(entree, "UTF-8");
        this.out = sortie;
        this.concurrent = new SimulateurConcurrent(delaiConcurrentMs);
    }

    public GestionVoitures getGarage() {
        return garage;
    }

    public GestionCourse getGestionCourse() {
        return gestionCourse;
    }

    public void run() {
        try {
            boolean continuer = true;
            while (continuer) {
                afficherMenu();
                String saisie = lire("Votre choix : ");
                int choix;
                try {
                    choix = Integer.parseInt(saisie);
                } catch (NumberFormatException e) {
                    out.println("Erreur : choix invalide '" + saisie + "'. Saisissez un nombre entre 0 et 10.");
                    continue;
                }
                try {
                    continuer = traiter(choix);
                } catch (ValidationException e) {
                    out.println("Erreur : " + e.getMessage());
                } catch (IllegalStateException e) {
                    out.println("Erreur d'exécution : " + e.getMessage());
                }
            }
        } catch (FinEntree e) {
            out.println();
            out.println("Fin de l'entrée : arrêt du programme.");
        }
        out.println("Au revoir !");
    }

    private boolean traiter(int choix) {
        switch (choix) {
            case 1: ajouterVoiture(); break;
            case 2: afficherVoitures(); break;
            case 3: rechercherVoiture(); break;
            case 4: modifierVoiture(); break;
            case 5: supprimerVoiture(); break;
            case 6: creerCourse(); break;
            case 7: demarrer(Mode.SEQUENTIEL); break;
            case 8: consulterResultats(); break;
            case 9: demarrer(Mode.THREADS); break;
            case 10: demarrer(Mode.POOL); break;
            case 0: return false;
            default:
                out.println("Erreur : choix invalide '" + choix + "'. Saisissez un nombre entre 0 et 10.");
        }
        return true;
    }

    private void afficherMenu() {
        out.println();
        out.println("=========================================");
        out.println("      GESTION DE COURSES AUTOMOBILES");
        out.println("=========================================");
        out.println("1. Ajouter une voiture");
        out.println("2. Afficher les voitures");
        out.println("3. Rechercher une voiture");
        out.println("4. Modifier une voiture");
        out.println("5. Supprimer une voiture");
        out.println("6. Créer et configurer une course");
        out.println("7. Démarrer une course");
        out.println("8. Consulter les résultats");
        out.println("9. Démarrer une course en mode concurrent (threads)");
        out.println("10. Démarrer une course en mode concurrent (pool de threads)");
        out.println("0. Quitter");
        out.println("=========================================");
    }

    // ---- F1 ----
    private void ajouterVoiture() {
        String id = lire("Identifiant : ");
        String nom = lire("Nom : ");
        String marque = lire("Marque : ");
        double vitesse = lireNombre("Vitesse (km/tour) : ");
        Voiture v = garage.ajouter(id, nom, marque, vitesse);
        out.println("Voiture ajoutée : " + v);
    }

    private void afficherVoitures() {
        List<Voiture> liste = garage.lister();
        if (liste.isEmpty()) {
            out.println("Aucune voiture enregistrée.");
            return;
        }
        out.println("Voitures enregistrées (" + liste.size() + ") :");
        for (Voiture v : liste) {
            out.println("  " + v);
        }
    }

    private void rechercherVoiture() {
        String id = lire("Identifiant à rechercher : ");
        Optional<Voiture> v = garage.rechercher(id);
        if (v.isPresent()) {
            out.println("Trouvée : " + v.get());
        } else {
            out.println("Aucune voiture avec l'identifiant '" + id + "'.");
        }
    }

    private void modifierVoiture() {
        String id = lire("Identifiant de la voiture à modifier : ");
        Voiture actuelle = garage.obtenir(id);
        out.println("Valeurs actuelles : " + actuelle + " (laisser vide pour conserver)");
        String nom = vide(lire("Nouveau nom : "));
        String marque = vide(lire("Nouvelle marque : "));
        String vit = vide(lire("Nouvelle vitesse : "));
        Double vitesse = vit == null ? null : Double.valueOf(convertir(vit));
        Voiture v = garage.modifier(id, nom, marque, vitesse);
        out.println("Voiture modifiée : " + v);
    }

    private void supprimerVoiture() {
        String id = lire("Identifiant de la voiture à supprimer : ");
        garage.supprimer(id);
        out.println("Voiture '" + id.trim() + "' supprimée.");
    }

    // ---- F2 ----
    private void creerCourse() {
        String id = lire("Identifiant de la course : ");
        String nom = lire("Nom de la course : ");
        double distance = lireNombre("Distance totale (km) : ");
        Course c = gestionCourse.creer(id, nom, distance);
        out.println("Course créée (la précédente, si elle existait, est remplacée).");
        if (garage.lister().isEmpty()) {
            out.println("Le garage est vide : ajoutez des voitures (option 1) puis recréez la course.");
            return;
        }
        afficherVoitures();
        while (true) {
            String vid = lire("Identifiant de la voiture à inscrire (vide pour terminer) : ");
            if (vid.isEmpty()) {
                break;
            }
            try {
                gestionCourse.inscrire(garage.obtenir(vid));
                out.println("Voiture '" + vid + "' inscrite (" + c.getParticipations().size() + " participant(s)).");
            } catch (ValidationException e) {
                out.println("Erreur : " + e.getMessage());
            }
        }
        if (c.getParticipations().size() < 2) {
            out.println("Attention : moins de 2 voitures inscrites, la course ne pourra pas démarrer.");
        }
        Affichage.infosCourse(c, out);
    }

    // ---- F3 / F4 / partie 2 ----
    private enum Mode { SEQUENTIEL, THREADS, POOL }

    private void demarrer(Mode mode) {
        Course c = gestionCourse.getCourse()
                .orElseThrow(() -> new ValidationException("Aucune course configurée (option 6)."));
        c.valider();
        Affichage.infosCourse(c, out);
        switch (mode) {
            case SEQUENTIEL: sequentiel.simuler(c, out); break;
            case THREADS: concurrent.executerAvecThreads(c, out); break;
            default: concurrent.executerAvecPool(c, POOL, out); break;
        }
    }

    private void consulterResultats() {
        Optional<Course> c = gestionCourse.getCourse();
        if (!c.isPresent() || c.get().getStatut() != Course.Statut.TERMINEE) {
            out.println("Aucun résultat disponible : aucune course terminée.");
            return;
        }
        out.println("Résultats de la course " + c.get().getId() + " : " + c.get().getNom());
        Affichage.resultats(c.get(), out);
    }

    // ---- saisies ----
    private String lire(String invite) {
        out.print(invite);
        out.flush();
        if (!in.hasNextLine()) {
            throw new FinEntree();
        }
        String ligne = in.nextLine().trim();
        // En entrée redirigée, on répète la saisie pour que les traces restent lisibles
        if (System.console() == null) {
            out.println(ligne);
        }
        return ligne;
    }

    private double lireNombre(String invite) {
        String s = lire(invite);
        try {
            return Double.parseDouble(convertir(s));
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + s + "' n'est pas un nombre valide.");
        }
    }

    private static String convertir(String s) {
        return s.replace(',', '.');
    }

    private static String vide(String s) {
        return s.isEmpty() ? null : s;
    }
}
