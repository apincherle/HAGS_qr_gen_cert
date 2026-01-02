# QR Certificate System

A Spring Boot application for generating QR codes and managing card certificates with anti-tamper protection.

## Features

- Generate unique QR codes for card certificates
- Store certificate data in PostgreSQL database
- Signed URLs for anti-tamper protection
- RESTful API endpoints for certificate management
- Configurable base URL via environment variables

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database

## Configuration

The application uses environment variables for configuration. Key settings:

### Database Configuration
- `SPRING_PROFILES_ACTIVE`: Spring profile (default: `dev`)
- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/hags_customer`)
- `SPRING_DATASOURCE_USERNAME`: Database username (default: `hags_user`)
- `SPRING_DATASOURCE_PASSWORD`: Database password (default: `hags_password`)

### QR Certificate Configuration
- `QR_BASE_URL`: Base URL for certificate links (default: `https://www.hags-grading.co.uk`)
- `QR_CERT_SECRET`: Secret key for URL signing (default: `change-me-please`)
- `QR_STORAGE_PATH`: Path to store QR code images (default: `./static/qrs`)
- `QR_SERIAL_PREFIX`: Prefix for serial numbers (default: `HAGS`)

### Server Configuration
- `SERVER_PORT`: Server port (default: `8019`)

## Database Schema

The application will automatically create the following tables:

- `card_certificate`: Main certificate records
- `card_image`: Images associated with certificates (placeholder for now)

## API Endpoints

### Generate Certificate and QR Code

**POST** `http://localhost:8019/api/qr-certificate/generate`

Creates a new certificate and generates a QR code.

**Request Body:**
```json
{
  "cardName": "Charizard",
  "setName": "Base Set",
  "year": 1999,
  "cardNumber": "4",
  "variant": "Holo",
  "grade": 9.5,
  "graderVersion": "v1.0",
  "gradedAt": "2026-01-15T10:30:00",
  "notesPublic": "Excellent condition",
  "status": "VERIFIED"
}
```

**Response:**
```json
{
  "id": 1,
  "publicId": "A1B2C3D4E5F6G7H8",
  "serialNumber": "HAGS-2026-000001",
  "status": "VERIFIED",
  "cardName": "Charizard",
  "setName": "Base Set",
  "year": 1999,
  "cardNumber": "4",
  "variant": "Holo",
  "grade": 9.5,
  "graderVersion": "v1.0",
  "gradedAt": "2026-01-15T10:30:00",
  "notesPublic": "Excellent condition",
  "certificateUrl": "https://www.hags-grading.co.uk/c/A1B2C3D4E5F6G7H8?sig=...",
  "qrImageUrl": "/api/qr-certificate/qr/A1B2C3D4E5F6G7H8"
}
```

### Get QR Code Image

**GET** `/api/qr-certificate/qr/{publicId}`

Returns the QR code PNG image for a certificate.

**Example:**
```
GET http://localhost:8019/api/qr-certificate/qr/A1B2C3D4E5F6G7H8
```

Returns: PNG image file

### Get Certificate Details

**GET** `/api/qr-certificate/{publicId}`

Returns certificate details by public ID.

**Example:**
```
GET http://localhost:8019/api/qr-certificate/A1B2C3D4E5F6G7H8
```

**Response:**
```json
{
  "id": 1,
  "publicId": "A1B2C3D4E5F6G7H8",
  "serialNumber": "HAGS-2026-000001",
  "status": "VERIFIED",
  "cardName": "Charizard",
  "setName": "Base Set",
  "year": 1999,
  "cardNumber": "4",
  "variant": "Holo",
  "grade": 9.5,
  "graderVersion": "v1.0",
  "gradedAt": "2026-01-15T10:30:00",
  "notesPublic": "Excellent condition",
  "certificateUrl": "https://www.hags-grading.co.uk/c/A1B2C3D4E5F6G7H8?sig=...",
  "qrImageUrl": "/api/qr-certificate/qr/A1B2C3D4E5F6G7H8"
}
```

## Building and Running

### Using Docker Compose (Recommended)

The easiest way to run the application is using Docker Compose:

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f qr-certificate-app

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

The application will be available at:
- API: `http://localhost:8019`
- PostgreSQL: `localhost:5432`

### Manual Build and Run

#### Build the application
```bash
mvn clean package
```

#### Run the application
```bash
mvn spring-boot:run
```

Or with environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/hags_customer
export SPRING_DATASOURCE_USERNAME=hags_user
export SPRING_DATASOURCE_PASSWORD=hags_password
export QR_BASE_URL=https://www.hags-grading.co.uk
export SERVER_PORT=8019
mvn spring-boot:run
```

### Docker Compose Configuration

The `docker-compose.yml` includes:
- **PostgreSQL database** (if you need a fresh database)
- **QR Certificate Application** on port 8019
- **Persistent volumes** for database and QR code storage
- **Health checks** to ensure database is ready before starting the app

If you're using an existing PostgreSQL database, you can modify the `docker-compose.yml` to remove the `postgres` service and update the `SPRING_DATASOURCE_URL` to point to your existing database.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/hags/qrcert/
│   │       ├── QrCertificateApplication.java
│   │       ├── config/
│   │       │   └── QrCertificateProperties.java
│   │       ├── controller/
│   │       │   └── QrCertificateController.java
│   │       ├── entity/
│   │       │   ├── CardCertificate.java
│   │       │   └── CardImage.java
│   │       ├── repository/
│   │       │   ├── CardCertificateRepository.java
│   │       │   └── CardImageRepository.java
│   │       └── service/
│   │           ├── CertificateService.java
│   │           └── QrCodeService.java
│   └── resources/
│       └── application.yml
└── pom.xml
```

## Notes

- QR codes are stored in the directory specified by `QR_STORAGE_PATH` (default: `./static/qrs`)
- The photo/image table (`card_image`) is implemented as a placeholder and can be extended when photo capture is implemented
- URLs are signed using HMAC-SHA256 for anti-tamper protection
- Serial numbers follow the format: `{PREFIX}-{YEAR}-{SEQUENCE}` (e.g., `HAGS-2026-000001`)

