#############################################
# Stage 1 - Build
#############################################
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copia primeiro os artefatos do Maven Wrapper e o pom.xml para aproveitar o cache de camadas
# do Docker: as dependências só serão baixadas novamente se o pom.xml mudar.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Agora copia o restante do código-fonte e realiza o build.
COPY src ./src
RUN ./mvnw -B clean package -DskipTests \
    && cp target/*.jar app.jar

#############################################
# Stage 2 - Runtime
#############################################
FROM eclipse-temurin:21-jre AS runtime

# Usuário não-root para rodar a aplicação (boa prática de segurança em produção)
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
COPY --from=build /app/app.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Healthcheck simples via TCP na porta da aplicação (não depende do Spring Actuator,
# que não está entre as dependências do projeto no momento).
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
    CMD bash -c 'exec 3<>/dev/tcp/localhost/8080' || exit 1

# Permite ajustar opções da JVM (heap, GC, etc.) em tempo de execução sem rebuild da imagem.
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
