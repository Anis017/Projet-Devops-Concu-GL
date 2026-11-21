package course.model;

/** Règles de validation partagées (F5). */
public final class Validation {
    private Validation() {
    }

    public static String texteNonVide(String champ, String valeur) {
        if (valeur == null || valeur.trim().isEmpty()) {
            throw new ValidationException("Le champ '" + champ + "' ne peut pas être vide.");
        }
        return valeur.trim();
    }

    public static double strictementPositif(String champ, double valeur) {
        if (Double.isNaN(valeur) || Double.isInfinite(valeur) || valeur <= 0) {
            throw new ValidationException("Le champ '" + champ
                    + "' doit être strictement positif (valeur refusée : " + valeur + ").");
        }
        return valeur;
    }
}
