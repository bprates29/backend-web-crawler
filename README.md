# Backend Web Crawler

This project is a backend web crawler that tracks URLs and checks if they contain a given keyword. It is developed in Java and uses the SparkJava framework to create a REST API.

## Requirements

- Java 14

## Configuration

### Environment Variables

- `BASE_URL`: Defines the base URL from which the crawler will start.

## Compilation and Execution

### Compilation

To compile the project, use your preferred Java IDE or run the following command:

```bash
javac -d out -sourcepath src src/com/axreng/backend/Main.java
```

## Docker
### Build Docker Image
To build the Docker image for the project, use the following command:

```bash
docker build . -t axreng/backend
```

### Run Docker Container
To run the Docker container, use the following command:

```bash
docker run -e BASE_URL=http://exemple.com/ -p 4567:4567 --rm axreng/backend
```
## API Endpoints

### GET /crawl
Returns the status of all ongoing or completed crawls.

### GET /crawl/:id
Returns the status of a specific crawl.

### POST /crawl
Starts a new crawl with the provided keyword. The request body must contain a JSON with the keyword:

```bash
{
    "keyword": "your-keyword"
}
```

## Project Structure
* src/main/java/com/axreng/backend: Main source code of the project.
  * controller: API controllers.
  * model: Data models.
  * service: Business logic and services.
  * util: Helper utilities.
* src/test/java/com/axreng/backend: Test source code.

## Version History

- **v1.0** - Initial project
- **v1.1** - Future - Attempt to perform all operations in parallel
- **v1.2** - Addition of unit tests and bug fixes
- **v1.3** - General improvements
- **v1.4** - Addition of README
- **v1.5** - Performance improvements
- **v1.6** - Performance improvements (Official)