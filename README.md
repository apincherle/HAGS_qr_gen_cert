# Generic QR Generator Service

Spring Boot API that generates QR codes from URLs and stores a reference file for lookup.

## Features

- Generate QR code PNG from any URL.
- Save matching URL reference JSON file using the same ID.
- Retrieve generated PNG by reference.
- Retrieve stored URL mapping by reference.
- Use Swagger UI for running endpoints interactively.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

Service default URL: `http://localhost:8019`  
Swagger UI: `http://localhost:8019/docs`

## Configuration

- `SERVER_PORT` (default: `8019`)
- `QR_STORAGE_PATH` (default: `./static/qrs`)

## API

### `POST /api/qr-certificate/generate-url-qr/image`

Generates a QR code from URL and returns the image as `image/png` directly.

Request:

```json
{
  "url": "https://example.com/item/123",
  "qrId": "optional_reference"
}
```

Notes:
- `url` is required.
- `qrId` is optional.
- If `qrId` is omitted, a UUID is generated.

### `GET /api/qr-certificate/qr/generic/{qrId}`

Returns the QR code PNG.

### `GET /api/qr-certificate/qr/generic/{qrId}/url`

Returns the stored mapping:

```json
{
  "qrId": "optional_reference",
  "url": "https://example.com/item/123",
  "imageUrl": "/api/qr-certificate/qr/generic/optional_reference",
  "imageFile": "optional_reference.png",
  "createdAt": "2026-04-13T12:20:00Z"
}
```

## File Output

Each generated reference writes:
- `<qrId>.png`
- `<qrId>.json` (URL + metadata)
