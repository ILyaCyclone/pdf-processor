package ru.unisuite.pdfprocessor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ShowAttachmentsController {

    private final List<PdfAttachment> orderedAttachments;

    @GetMapping(path = "/attachments")
    public List<PdfAttachment> attachments() {
        return orderedAttachments;
    }

    // /attachments?text
    @GetMapping(path = "/attachments", params = "text")
    public String attachmentsAsText() {
        return orderedAttachments.stream().map(PdfAttachment::toString).collect(Collectors.joining("\n"));
    }

}
