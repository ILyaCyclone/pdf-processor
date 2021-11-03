package ru.unisuite.pdfprocessor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfAttachment {
    private String name;
    private String type;
    private String source;
}
