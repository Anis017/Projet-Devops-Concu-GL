# Image d'exécution de la simulation (compilation avec javac, sans Maven/Gradle)
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY src/main ./src/main
RUN mkdir -p out && javac -encoding UTF-8 -d out $(find src/main -name "*.java")
# Mode de simulation automatique (sans saisie utilisateur) ; le conteneur s'arrête à la fin de la course
ENTRYPOINT ["java", "-cp", "out", "course.Main", "--auto"]
