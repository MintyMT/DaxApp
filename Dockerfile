# --- BUILD STAGE ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copia archivos de configuración para caché de dependencias
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Descarga dependencias sin compilar código
RUN ./gradlew dependencies --no-daemon

# Copia código fuente y construye el JAR
COPY src src
RUN ./gradlew bootJar --no-daemon

# --- RUNNER STAGE ---
FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app

# Copia el JAR generado
COPY --from=builder /app/build/libs/*.jar app.jar

# Expone el puerto 8081 configurado en tu properties
EXPOSE 8081

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]