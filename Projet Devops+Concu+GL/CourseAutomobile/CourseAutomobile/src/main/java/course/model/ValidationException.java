package course.model;

/** Erreur de validation des données saisies (message explicite destiné à l'utilisateur). */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
