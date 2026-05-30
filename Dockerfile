# Estágio 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 1. Velocidade de build (cache): Copia somente o pom.xml primeiro.
# Assim, o Docker faz cache do download das dependências se o pom.xml não for alterado.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copia o código fonte e realiza o build da aplicação.
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run
# 3. Tamanho reduzido: Usei uma imagem JRE (Java Runtime Environment) menor,
# em vez de uma imagem JDK, já que não preisei mais compilar código.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 4. Segurança: Criei um usuário não-root para rodar a aplicação,
# evitando que a aplicação tenha privilégios totais de root dentro do container.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia apenas o artefato final (JAR) gerado no estágio anterior (builder).
COPY --from=builder /build/target/*.jar app.jar

# Expor a porta que a aplicação vai rodar
EXPOSE 8081

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]