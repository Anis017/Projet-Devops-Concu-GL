package course.service;

import course.model.Course;
import course.model.ValidationException;
import course.model.Voiture;

import java.util.Optional;

/** F2 – Configuration d'une course. Une seule course à la fois : la nouvelle remplace l'ancienne. */
public class GestionCourse {
    private Course courseCourante;

    public Course creer(String id, String nom, double distanceTotale) {
        Course c = new Course(id, nom, distanceTotale); // valide avant de remplacer l'ancienne
        this.courseCourante = c;
        return c;
    }

    public void inscrire(Voiture voiture) {
        if (courseCourante == null) {
            throw new ValidationException("Aucune course configurée.");
        }
        courseCourante.ajouterParticipant(voiture);
    }

    public Optional<Course> getCourse() {
        return Optional.ofNullable(courseCourante);
    }

    public boolean voitureEngagee(String voitureId) {
        return courseCourante != null && courseCourante.engage(voitureId);
    }
}
