# ---- Fase 1: Construcción (Build) ----
# Usamos una imagen de Maven para compilar el proyecto Java
FROM maven:3.8.5-openjdk-21 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el archivo pom.xml para descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente y construimos el .jar
COPY src ./src
RUN mvn package -DskipTests

# ---- Fase 2: Ejecución (Runtime) ----
# Usamos una imagen ligera con solo el entorno de ejecución de Java
FROM openjdk:21-jdk-slim

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos el .jar construido desde la fase anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor inicie
ENTRYPOINT ["java", "-jar", "app.jar"]