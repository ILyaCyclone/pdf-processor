package ru.unisuite.pdfwatermark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PdfProcessorProperties.class)
public class PdfWatermarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfWatermarkApplication.class, args);
    }

}
