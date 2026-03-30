# --- ÉTAPE 1 : Compilation (Build) ---
# On utilise une image Maven officielle pour compiler le code
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# On copie le pom.xml et les libs locales (très important pour ton projet !)
COPY pom.xml .
COPY libs ./libs

# On copie le code source
COPY src ./src

# On compile le projet en ignorant les tests pour gagner du temps
RUN mvn clean package -DskipTests

# --- ÉTAPE 2 : Exécution (Runtime) ---
# On utilise une image Java légère pour faire tourner l'app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# On récupère UNIQUEMENT le fichier .jar généré à l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# On expose le port
EXPOSE 8080

# On lance l'application
ENTRYPOINT ["java", "-jar", "app.jar"]