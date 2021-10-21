package ru.unisuite.pdfprocessor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "pdf-processor")
public class PdfProcessorProperties {
    List<PdfAttachment> attachments;
}
