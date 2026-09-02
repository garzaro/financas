# syntax=docker/dockerfile:1.7

# Dockerfile para construir e executar a aplicação Financas.
# Este Dockerfile utiliza uma construção em múltiplos estágios
# (*multi-stage build*) para criar uma imagem leve para a execução da aplicação.
# O primeiro estágio utiliza o Maven para construir a aplicação, 
# e o segundo estágio utiliza uma imagem JRE para executá-la. 
#
# Uso:
# docker build -t financas-app . 
# docker run -p 8080:8080 financas-app

# O primeiro estágio utiliza uma imagem Maven para compilar a aplicação
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia os arquivos de configuração do Maven e baixa as dependências
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Copia o código-fonte da aplicação e compila o projeto
COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# O segundo estágio utiliza uma imagem JRE para executar a aplicação
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Instalação de certificados e configuração do fuso horário
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates tzdata \
    && groupadd --system spring \
    && useradd --system --gid spring --create-home --home-dir /home/spring spring \
    && rm -rf /var/lib/apt/lists/*

# Copia o arquivo JAR gerado no estágio de build para o diretório /app    
COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R spring:spring /app

# Define o usuário não-root para executar a aplicação - spring
USER financa

# Define a variável de ambiente PORT e expõe a porta 8080 para acesso externo
ENV PORT=8080
EXPOSE 8080

# Define o ponto de entrada da aplicação, utilizando a variável de ambiente JAVA_OPTS para permitir a configuração de opções adicionais do Java
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dserver.port=${PORT} -jar /app/app.jar"]
