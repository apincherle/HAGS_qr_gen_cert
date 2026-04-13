package com.hags.qrcert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hags.qrcert.config.QrCertificateProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {

    private final QrCertificateProperties properties;
    private final ObjectMapper objectMapper;

    public Path generateQrCodeImage(String url, String publicId) throws IOException, WriterException {
        Path storagePath = Paths.get(properties.getQrStoragePath());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        int size = 300;
        int border = 2;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, border);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, size, size);
        graphics.setColor(Color.BLACK);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        graphics.dispose();

        Path qrPath = storagePath.resolve(publicId + ".png");
        javax.imageio.ImageIO.write(image, "PNG", qrPath.toFile());

        log.info("QR code generated and saved to: {}", qrPath);
        return qrPath;
    }

    public Path saveQrUrlReference(String qrId, String url) throws IOException {
        Path storagePath = Paths.get(properties.getQrStoragePath());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        Path referencePath = storagePath.resolve(qrId + ".json");
        QrReference metadata = new QrReference(
                qrId,
                url,
                "/api/qr-certificate/qr/generic/" + qrId,
                qrId + ".png",
                Instant.now().toString()
        );
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(referencePath.toFile(), metadata);

        log.info("QR URL reference saved to: {}", referencePath);
        return referencePath;
    }

    public Path getQrUrlReferencePath(String qrId) {
        Path storagePath = Paths.get(properties.getQrStoragePath());
        return storagePath.resolve(qrId + ".json");
    }

    public Optional<QrReference> readQrUrlReference(String qrId) throws IOException {
        Path referencePath = getQrUrlReferencePath(qrId);
        if (!Files.exists(referencePath)) {
            return Optional.empty();
        }

        return Optional.of(objectMapper.readValue(referencePath.toFile(), QrReference.class));
    }

    public Path getQrCodeImagePath(String publicId) {
        Path storagePath = Paths.get(properties.getQrStoragePath());
        return storagePath.resolve(publicId + ".png");
    }

    public record QrReference(
            String qrId,
            String sourceUrl,
            String imageUrl,
            String imageFile,
            String createdAt
    ) {
    }
}

