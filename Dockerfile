# Etapa 1: Build Otimizado
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Otimização do cache de dependências: Baixar dependências antes de compilar
COPY pom.xml ./

# O "-B" força o Maven a rodar em batch mode, melhorando a saída no Docker
RUN mvn dependency:go-offline -B -q

# Compila o projeto
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# Etapa 2: Runtime Minimalista
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Criação de usuário não-root (Boas práticas de segurança)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar apenas o JAR
COPY --from=builder /app/target/*.jar app.jar

# A aplicação Spring Boot está configurada no application.properties para usar a porta 8081
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
