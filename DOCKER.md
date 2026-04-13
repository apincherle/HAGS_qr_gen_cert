# Docker Usage

## Run with Docker Compose

```bash
docker-compose up -d
```

API: `http://localhost:8019`  
Swagger: `http://localhost:8019/docs`

## Environment Variables

- `SERVER_PORT` (default `8019`)
- `QR_STORAGE_PATH` (default `/app/static/qrs` in container)

## Volumes

- `qr_storage` -> `/app/static/qrs`

This volume stores both generated files:
- `<qrId>.png`
- `<qrId>.json`

## Logs

```bash
docker-compose logs -f qr-certificate-app
```

## Stop

```bash
docker-compose down
```
