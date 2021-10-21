package ru.unisuite.pdfwatermark;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class AttachmentsConfig {

    @Bean(name = "namedAttachments")
    Map<String, PdfAttachment> namedAttachments(PdfProcessorProperties attachmentsProperties) {
        return attachmentsProperties.getAttachments().stream()
                .collect(Collectors.toMap(PdfAttachment::getName, Function.identity()));
    }

    @Bean(name = "orderedAttachments")
    List<PdfAttachment> orderedAttachments(PdfProcessorProperties attachmentsProperties) {
        return attachmentsProperties.getAttachments();
    }
}
