# QR UI Plan (Cross-Device Local Save)

## Goal

Build a small UI app that:
- Calls the QR API endpoints in `HAGS_qr_gen_cert`
- Shows generated QR images immediately
- Saves generated files to the user's local device (`phone`, `tablet`, `PC`)
- Organizes downloaded files into a `qrs` folder pattern where possible

## API Endpoints To Use

- `POST /api/qr-certificate/generate-url-qr/image`
  - Input: `{ url, qrId }`
  - Output: `image/png` binary
  - Side-effect on server: writes `<qrId>.png` and `<qrId>.txt`

- `GET /api/qr-certificate/qr/generic/{qrId}/url`
  - Input: `qrId`
  - Output: JSON mapping `{ qrId, url }`

## UI Scope (MVP)

Single-page app with:
- URL field (required)
- Reference field `qrId` (optional, auto-generate if empty)
- "Generate QR" button
- QR image preview panel
- Status panel (success/errors)
- "Download PNG" button
- "Download TXT" button (client-generated from URL + qrId)
- "Download ZIP" button (PNG + TXT bundled)

## Recommended Frontend Stack

- `Vite + React + TypeScript`
- `JSZip` for creating downloadable ZIPs
- Optional: `uuid` package for reference generation

## Local Save Strategy By Device

Because browsers have different file-system permissions, implement layered save options:

1. Primary (all devices): file download via browser
   - Save files into Downloads (default browser behavior)
   - Filename convention: `qrs/<qrId>.png` and `qrs/<qrId>.txt` represented as `qrs_<qrId>.png` if folder paths are not supported

2. Enhanced desktop support (Chromium browsers):
   - Use File System Access API (if available)
   - Prompt user to pick/create a local `qrs` folder
   - Write `qrId.png` and `qrId.txt` directly into that folder

3. Mobile fallback:
   - Use standard download/share sheet behavior
   - Offer "Download ZIP" for easier single-tap save/share

## File Output Rules

Client-side saved files:
- `<qrId>.png` from API image response
- `<qrId>.txt` with URL content (single line)
- Optional `<qrId>.zip` containing both files

Server-side files remain as-is:
- `static/qrs/<qrId>.png`
- `static/qrs/<qrId>.txt`

## UX Flow

1. User enters URL and optional `qrId`
2. UI sends `POST /generate-url-qr/image`
3. UI displays PNG preview immediately
4. UI creates TXT content in memory (`url + newline`)
5. UI enables:
   - Download PNG
   - Download TXT
   - Download ZIP
6. Optional: UI verifies mapping by calling `GET /qr/generic/{qrId}/url`

## Validation Rules

- URL must not be empty
- `qrId` should match: `^[A-Za-z0-9_-]+$`
- If `qrId` is empty, generate UUID

## Security / Platform Notes

- Prefer HTTPS in production for camera/share APIs and better browser compatibility
- Configure CORS if UI is served from a different origin
- Do not expose server filesystem paths in UI messages for public deployments

## Future Enhancements

- PWA install mode for app-like behavior on phone/tablet
- History list (recent generated QR references)
- Bulk CSV mode (generate many QR codes)
- Optional cloud sync export (Google Drive/OneDrive)

## Implementation Phases

Phase 1 (MVP):
- Build page, call API, preview image, download PNG/TXT

Phase 2:
- ZIP export, mobile polish, better errors

Phase 3:
- File System Access API desktop folder writing to user-selected `qrs`
- PWA support
