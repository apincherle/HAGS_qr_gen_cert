package com.hags.qrcert.controller;

import com.hags.qrcert.service.QrCodeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/qr-certificate")
@RequiredArgsConstructor
@Slf4j
public class QrCertificateController {

    private final QrCodeService qrCodeService;
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    @PostMapping(value = "/generate-url-qr/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> generateUrlQrCodeImage(@RequestBody GenericQrRequest request) {
        try {
            if (!isValidRequest(request)) {
                return ResponseEntity.badRequest().build();
            }

            String qrId = resolveQrId(request.getQrId());
            Path qrPath = qrCodeService.generateQrCodeImage(request.getUrl(), qrId);
            qrCodeService.saveQrUrlReference(qrId, request.getUrl());

            Resource resource = new FileSystemResource(qrPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + qrId + ".png\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error generating URL QR image response", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/qr/generic/{qrId}")
    public ResponseEntity<Resource> getGenericQrCodeImage(@PathVariable String qrId) {
        try {
            Path qrPath = qrCodeService.getQrCodeImagePath(qrId);
            if (!Files.exists(qrPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(qrPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + qrId + ".png\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error retrieving generic QR code image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/qr/generic/{qrId}/url")
    public ResponseEntity<Map<String, String>> getGenericQrCodeUrl(@PathVariable String qrId) {
        try {
            if (!REFERENCE_PATTERN.matcher(qrId).matches()) {
                return ResponseEntity.badRequest().build();
            }

            return qrCodeService.readQrUrlReference(qrId)
                    .map(meta -> ResponseEntity.ok(Map.of(
                            "qrId", meta.qrId(),
                            "url", meta.sourceUrl(),
                            "imageUrl", meta.imageUrl(),
                            "imageFile", meta.imageFile(),
                            "createdAt", meta.createdAt()
                    )))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving generic QR URL reference", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Data
    public static class GenericQrRequest {
        private String url;
        private String qrId;
    }

    private boolean isValidRequest(GenericQrRequest request) {
        if (request == null || request.getUrl() == null || request.getUrl().isBlank()) {
            return false;
        }
        String qrId = resolveQrId(request.getQrId());
        return REFERENCE_PATTERN.matcher(qrId).matches();
    }

    private String resolveQrId(String qrId) {
        return (qrId == null || qrId.isBlank()) ? UUID.randomUUID().toString() : qrId;
    }
}

