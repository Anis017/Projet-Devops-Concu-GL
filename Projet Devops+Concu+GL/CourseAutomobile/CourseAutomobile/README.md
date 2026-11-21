# CourseAutomobile – Mini-projet transversal L3 MIAGE

Simulation de courses automobiles en Java (console) : partie 1 (séquentiel), partie 2 (threads / pool), partie 3 (CI/CD).

## Compilation et exécution (javac uniquement)
```sh
mkdir -p out/main out/test
javac -encoding UTF-8 -d out/main $(find src/main -name "*.java")
javac -encoding UTF-8 -cp out/main -d out/test $(find src/test -name "*.java")

java -cp out/main course.Main                       # menu interactif
java -cp out/main course.Main --auto                # simulation automatique séquentielle
java -cp out/main course.Main --auto --concurrent   # un thread par voiture (--delay=MS, défaut 500)
java -cp out/main course.Main --auto --pool         # pool fixe de 3 threads
java -cp out/main:out/test course.test.TestRunner   # tests (code de sortie != 0 si échec)
```
Menu : options 1 à 8 (partie 1), 9 = course concurrente (threads), 10 = course concurrente (pool de 3 threads).

## Docker
```sh
docker build -t course-automobile .
docker run --name course-run course-automobile      # lance --auto, s'arrête à la fin de la course
docker ps -a ; docker logs course-run ; docker inspect -f '{{.State.ExitCode}}' course-run
```
## Jenkins
`Jenkinsfile` : CODE, BUILD, TEST, RELEASE, DEPLOY, OPERATE, MONITOR. Déclenchement : Poll SCM `H/5 * * * *`.
