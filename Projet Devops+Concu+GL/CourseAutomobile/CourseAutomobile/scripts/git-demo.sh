#!/bin/sh
# Séquence Git de la partie 3 (exercice 1) – à adapter avec l'URL de votre dépôt.
set -e
git init -b main
git add . && git commit -m "feat: version initiale (parties 1 et 2)"
git checkout -b develop
git checkout -b feature/message-erreur-menu
# ... modification du code (ex. amélioration d'un message d'erreur) ...
git commit -am "fix: message d'erreur plus explicite pour un choix de menu invalide"
git checkout develop && git merge --no-ff feature/message-erreur-menu -m "merge: feature/message-erreur-menu dans develop"
git checkout main && git merge --no-ff develop -m "release: intégration de la version validée dans main"
git tag -a v1.0.0 -m "Version stable 1.0.0"
git log --oneline --graph --all --decorate
# git remote add origin https://github.com/<compte>/CourseAutomobile.git
# git push -u origin main develop && git push origin v1.0.0
