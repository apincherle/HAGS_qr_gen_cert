# API Examples

## Generate URL QR

```bash
curl -X POST http://localhost:8019/api/qr-certificate/generate-url-qr/image \
  -H "Content-Type: application/json" \
  -H "accept: image/png" \
  -d '{
    "url": "https://example.com/orders/123",
    "qrId": "order_123"
  }'
```

## Fetch QR Image

```bash
curl -O http://localhost:8019/api/qr-certificate/qr/generic/order_123
```

## Fetch URL Mapping

```bash
curl http://localhost:8019/api/qr-certificate/qr/generic/order_123/url
```

Example response:

```json
{
  "qrId": "order_123",
  "url": "https://example.com/orders/123",
  "imageUrl": "/api/qr-certificate/qr/generic/order_123",
  "imageFile": "order_123.png",
  "createdAt": "2026-04-13T12:20:00Z"
}
```
