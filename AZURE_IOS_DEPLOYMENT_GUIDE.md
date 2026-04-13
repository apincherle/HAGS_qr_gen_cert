# Azure + iOS Deployment Guide

## Goal

Make the QR solution available on iOS devices by deploying:
- Backend API (`HAGS_qr_gen_cert`)
- Frontend UI (`HAGS_qr_gen_ui`)

## Recommended Architecture

- API -> Azure App Service (or Azure Container Apps)
- UI -> Azure Static Web Apps
- File storage -> Azure Blob Storage (recommended for production)
- TLS -> HTTPS for both UI and API

## Why This Works Well For iOS

- iPhone/iPad can use Safari directly
- UI can be installed as a PWA ("Add to Home Screen")
- No native App Store build needed for initial rollout

## Current State vs Production Note

Current API writes files to local disk (`static/qrs`).

For production in Azure, local disk can be ephemeral or instance-bound.
Use Azure Blob Storage for generated PNG/JSON files so files persist and scale.

## Deployment Plan

### 1) Deploy API (`HAGS_qr_gen_cert`)

Use Azure App Service (Java 17, Linux) or Container Apps.

Set environment variables:
- `SERVER_PORT`
- `QR_STORAGE_PATH` (temporary if local disk is still used)
- CORS allowed origins (include deployed UI domain)

Use custom domain + HTTPS certificate.

### 2) Deploy UI (`HAGS_qr_gen_ui`)

Build with:
- `VITE_API_BASE_URL=https://<your-api-domain>`

Deploy to Azure Static Web Apps (or Blob static website hosting).

Use custom domain + HTTPS.

### 3) Configure CORS

Allow production UI origins in API:
- `https://<your-ui>.azurestaticapps.net`
- `https://<your-custom-ui-domain>`

### 4) Validate End-to-End

- Generate QR from UI
- Confirm PNG + JSON creation
- Verify mapping endpoint
- Confirm iOS Safari works
- Test "Add to Home Screen" install flow

## iOS Delivery Options

### Option A: Web/PWA (Recommended first)
- Fastest path
- Works immediately on iPhone/iPad
- No App Store process

### Option B: Native Wrapper (Later)
- Wrap web app using Capacitor/React Native WebView if native features are needed

## Production Hardening Checklist

- [ ] Move generated files to Azure Blob Storage
- [ ] Add retention policy / cleanup job
- [ ] Add auth if endpoint should not be public
- [ ] Rate-limit generate endpoint
- [ ] Add monitoring (Application Insights)
- [ ] Add structured error responses
- [ ] Add backups/versioning for storage

## Suggested Next Technical Step

Implement Blob Storage in API:
- write PNG + JSON to blob container
- return blob URL (or signed URL)
- keep `/qr/generic/{id}` endpoint as compatibility proxy if needed
