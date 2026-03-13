# Cloud File Processing (Spring Boot + AWS SDK v2)

Production-style Java 17 sample project demonstrating integration with:

- Amazon S3 for file storage
- Amazon SQS for asynchronous processing
- Amazon RDS MySQL (via Spring Data JPA)

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- Spring Web
- Spring Data JPA
- AWS SDK v2

## Project Structure

```
src/main/java/com/example/cloudfileprocessing
├── config       # AWS clients and property binding
├── controller   # REST endpoints
├── dto          # API responses and error payloads
├── exception    # Custom exceptions and global handlers
├── model        # JPA entities and enums
├── repository   # Data access layer
├── service      # Business logic + AWS wrappers
└── worker       # SQS polling worker
```

## Functional Flow

### 1) Upload File API

`POST /files/upload` (multipart form-data with key: `file`)

1. File is uploaded to S3.
2. Initial metadata is persisted in MySQL.
3. A message with file details is sent to SQS.
4. Status transitions from `UPLOADED` to `QUEUED`.

### 2) Worker Service

- `SqsFileProcessingWorker` polls SQS on a schedule.
- For each message:
    - Set status to `PROCESSING`
    - Execute processing logic (demo placeholder)
    - Set status to `PROCESSED`
    - Delete message from queue
- On error, status is set to `FAILED`.

### 3) File List API

`GET /files`

Returns:
- `id`
- `filename`
- `uploadTime`
- `processingStatus`

### 4) Download File API

`GET /files/{id}`

- Reads metadata from DB
- Downloads content from S3
- Returns binary response

## AWS Configuration

Use `src/main/resources/application-example.yml` as a template.

### Credentials

The project supports:

- static credentials via `app.aws.access-key` and `app.aws.secret-access-key`
- `DefaultCredentialsProvider` when static credentials are not configured

With `DefaultCredentialsProvider` you can authenticate with:

- environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
- AWS profile (`~/.aws/credentials`)
- EC2/ECS role in AWS runtime

## Run Locally

1. Update `application.yml` (or copy from `application-example.yml`).
2. Ensure MySQL database is reachable.
3. Build and run:

```bash
mvn clean spring-boot:run
```

## Example Requests

Health check:

```bash
curl "http://localhost:8080/actuator/health"
```

Upload:

```bash
curl -X POST "http://localhost:8080/files/upload" ^
  -H "Content-Type: multipart/form-data" ^
  -F "file=@C:/temp/sample.txt"
```

List files:

```bash
curl "http://localhost:8080/files"
```

Download:

```bash
curl -X GET "http://localhost:8080/files/1" --output downloaded-file.bin
```
