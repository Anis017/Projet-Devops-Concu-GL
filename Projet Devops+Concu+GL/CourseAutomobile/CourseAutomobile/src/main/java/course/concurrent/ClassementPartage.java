package course.concurrent;

import course.model.Participation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Ressource partagée : ordre réel d'arrivée, protégé par synchronized (moniteur de l'instance). */
public class ClassementPartage {
    private final List<Participation> ordre = new ArrayList<>();
    private final Set<String> dejaEnregistrees = new HashSet<>();

    /** @return la position obtenue (1, 2, ...) ou -1 si la voiture était déjà enregistrée */
    public synchronized int enregistrer(Participation p) {
        if (!dejaEnregistrees.add(p.getVoitureId())) {
            return -1;
        }
        ordre.add(p);
        return ordre.size();
    }

    public synchronized List<Participation> getOrdre() {
        return new ArrayList<>(ordre);
    }

    public synchronized int taille() {
        return ordre.size();
    }
}
