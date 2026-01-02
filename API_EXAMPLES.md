# API Usage Examples

## Generate Certificate and QR Code

### Request
```bash
curl -X POST http://localhost:8019/api/qr-certificate/generate \
  -H "Content-Type: application/json" \
  -d '{
    "cardName": "Charizard",
    "setName": "Base Set",
    "year": 1999,
    "cardNumber": "4",
    "variant": "Holo",
    "grade": 9.5,
    "graderVersion": "v1.0",
    "notesPublic": "Excellent condition with minimal edge wear",
    "status": "VERIFIED"
  }'
```

### Response
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
  "notesPublic": "Excellent condition with minimal edge wear",
  "certificateUrl": "https://www.hags-grading.co.uk/c/A1B2C3D4E5F6G7H8?sig=AbCDefGhIjKlMnOp",
  "qrImageUrl": "/api/qr-certificate/qr/A1B2C3D4E5F6G7H8"
}
```

## Get QR Code Image

### Request
```bash
curl -O http://localhost:8019/api/qr-certificate/qr/A1B2C3D4E5F6G7H8
```

Or open in browser:
```
http://localhost:8019/api/qr-certificate/qr/A1B2C3D4E5F6G7H8
```

### Response
Returns a PNG image file of the QR code.

## Get Certificate Details

### Request
```bash
curl http://localhost:8019/api/qr-certificate/A1B2C3D4E5F6G7H8
```

### Response
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
  "notesPublic": "Excellent condition with minimal edge wear",
  "certificateUrl": "https://www.hags-grading.co.uk/c/A1B2C3D4E5F6G7H8?sig=AbCDefGhIjKlMnOp",
  "qrImageUrl": "/api/qr-certificate/qr/A1B2C3D4E5F6G7H8"
}
```

## Using with Images (Placeholder)

When photo capture is implemented, you can include images in the request:

```json
{
  "cardName": "Pikachu",
  "setName": "Base Set",
  "year": 1999,
  "cardNumber": "58",
  "variant": "Standard",
  "grade": 10.0,
  "graderVersion": "v1.0",
  "notesPublic": "Perfect condition",
  "status": "VERIFIED",
  "images": [
    {
      "kind": "front",
      "url": "https://cdn.example.com/cards/pikachu-front.jpg",
      "width": 1200,
      "height": 1680
    },
    {
      "kind": "back",
      "url": "https://cdn.example.com/cards/pikachu-back.jpg",
      "width": 1200,
      "height": 1680
    }
  ]
}
```

