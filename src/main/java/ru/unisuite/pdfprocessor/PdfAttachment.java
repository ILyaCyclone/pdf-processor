package ru.unisuite.pdfprocessor;

import lombok.Data;

@Data
public class PdfAttachment {
    private String name;
    private String type;
    private String source;
}
