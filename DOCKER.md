# Docker Setup Guide

## Quick Start

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f qr-certificate-app

# Stop services
docker-compose down
```

## Services

### PostgreSQL Database
- **Container**: `hags-postgres`
- **Port**: `5432` (exposed to host)
- **Database**: `hags_customer`
- **User**: `hags_user`
- **Password**: `hags_password`
- **Volume**: `postgres_data` (persistent storage)

### QR Certificate Application
- **Container**: `qr-certificate-system`
- **Port**: `8019` (exposed to host)
- **Volume**: `qr_storage` (for QR code images)
- **Depends on**: PostgreSQL (waits for health check)

## Using Existing Database

If you already have a PostgreSQL database running, you can modify `docker-compose.yml`:

1. Remove or comment out the `postgres` service
2. Update the `SPRING_DATASOURCE_URL` in `qr-certificate-app` to point to your database:
   ```yaml
   environment:
     - SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/hags_customer
   ```
3. Remove the `depends_on` section or update it accordingly

## Environment Variables

You can override environment variables in several ways:

### 1. Using .env file
Create a `.env` file in the project root:
```env
QR_BASE_URL=https://www.hags-grading.co.uk
QR_CERT_SECRET=your-secret-key-here
QR_SERIAL_PREFIX=HAGS
```

### 2. Using docker-compose.override.yml
Copy `docker-compose.override.yml.example` to `docker-compose.override.yml` and customize:
```bash
cp docker-compose.override.yml.example docker-compose.override.yml
```

### 3. Command line
```bash
QR_BASE_URL=http://localhost:8019 docker-compose up
```

## Volumes

### PostgreSQL Data
- **Volume**: `postgres_data`
- **Location**: `/var/lib/postgresql/data` (inside container)
- **Purpose**: Persistent database storage

### QR Code Storage
- **Volume**: `qr_storage`
- **Location**: `/app/static/qrs` (inside container)
- **Purpose**: Store generated QR code PNG files

To access QR codes from the host, you can mount a local directory:
```yaml
volumes:
  - ./static/qrs:/app/static/qrs
```

## Building

### Build from source
```bash
docker-compose build
```

### Rebuild without cache
```bash
docker-compose build --no-cache
```

## Troubleshooting

### Application won't start
1. Check if PostgreSQL is healthy:
   ```bash
   docker-compose ps
   docker-compose logs postgres
   ```

2. Check application logs:
   ```bash
   docker-compose logs qr-certificate-app
   ```

3. Verify database connection:
   ```bash
   docker-compose exec qr-certificate-app env | grep DATASOURCE
   ```

### Port already in use
If port 8019 is already in use, change it in `docker-compose.yml`:
```yaml
ports:
  - "8020:8019"  # Host port:Container port
```

### Database connection issues
- Ensure PostgreSQL is healthy before the app starts
- Check network connectivity: `docker-compose exec qr-certificate-app ping postgres`
- Verify credentials match in both services

## Development

For local development with hot-reload, you can:

1. Run the database only:
   ```bash
   docker-compose up -d postgres
   ```

2. Run the application locally:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hags_customer
   export SPRING_DATASOURCE_USERNAME=hags_user
   export SPRING_DATASOURCE_PASSWORD=hags_password
   mvn spring-boot:run
   ```

## Production Considerations

For production deployment:

1. **Change default secrets**: Set `QR_CERT_SECRET` to a strong random value
2. **Use external database**: Don't run PostgreSQL in Docker for production
3. **Configure proper base URL**: Set `QR_BASE_URL` to your production domain
4. **Set up backups**: Backup the `postgres_data` volume regularly
5. **Use reverse proxy**: Consider nginx/traefik for SSL termination
6. **Resource limits**: Add memory/CPU limits to services
7. **Health checks**: Configure proper health check endpoints

Example production overrides:
```yaml
services:
  qr-certificate-app:
    environment:
      - QR_CERT_SECRET=${QR_CERT_SECRET}  # From secrets manager
      - QR_BASE_URL=https://www.hags-grading.co.uk
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
```

