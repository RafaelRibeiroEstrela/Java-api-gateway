# Etapa de build: utiliza a imagem do Maven para compilar o projeto
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app
# Copia os arquivos de configuração e dependências
COPY pom.xml .
# Baixa as dependências definidas no pom.xml
RUN mvn dependency:go-offline
# Copia o código-fonte
COPY src/ ./src/
# Executa o build: clean e install
RUN mvn clean install -DskipTests

# Etapa de runtime: utiliza uma imagem mais leve apenas com o JDK para rodar a aplicação
FROM amazoncorretto:21
WORKDIR /app
# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]