package ru.unisuite.pdfprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(PdfProcessorProperties.class)
public class PdfProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfProcessorApplication.class, args);
    }

}
