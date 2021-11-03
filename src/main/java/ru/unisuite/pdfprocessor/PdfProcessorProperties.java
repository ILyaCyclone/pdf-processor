package ru.unisuite.pdfprocessor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "pdf-processor")
public class PdfProcessorProperties {

    private final List<PdfAttachment> attachments;

    private final String contentServiceUrl;

}
