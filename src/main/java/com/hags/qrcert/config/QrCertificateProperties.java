package com.hags.qrcert.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qr.certificate")
@Getter
@Setter
public class QrCertificateProperties {
    private String qrStoragePath = "./static/qrs";
}

