# MarketMaster Backend

This project is the backend of the MarketMaster application. It is a REST API built with Spring Boot and MySQL.

## Getting Started

### To compile the project

```bash
./mvnw clean install
```

### To run the project

```bash
java -jar target/marketmaster-backend-0.0.1-SNAPSHOT.jar
```

### Using Docker

To build the Docker image, run the following command:

```bash
docker build -t marketmaster-backend .
```

To run the Docker image, run the following command:

```bash
docker run -p 8081:8081 marketmaster-backend
```

## Diagrams

The diagrams are written in PlantUML and can be found in the `docs` folder.
To preview them, you can use the PlantUML Integration plugin for VSCode or IntelliJ IDEA.
