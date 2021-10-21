package ru.unisuite.pdfwatermark;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
public class PdfProcessorController {
    private static final Logger logger = LoggerFactory.getLogger(PdfProcessorController.class);

    private final OverlayPostProcessor overlayPostProcessor;
    private final MergePostProcessor mergePostProcessor;

    private final Map<String, PdfAttachment> namedAttachments;
    private final List<PdfAttachment> orderedAttachments;


    @PostMapping(path = "/overlay", consumes = MULTIPART_FORM_DATA_VALUE)
    public void overlay(@RequestParam("file") MultipartFile file,
                        @RequestParam(name = "attachment", defaultValue = "0") String attachmentParam,
                        @RequestParam(name = "pages", defaultValue = "1") String pages,
                        HttpServletResponse response) throws IOException {

        PdfAttachment pdfAttachment = extractPdfAttachment(attachmentParam);
        Supplier<InputStream> attachmentInputStreamSupplier = getPdfAttachmentImputStreamSupplier(pdfAttachment);

        try {
            overlayPostProcessor.overlay(response.getOutputStream(), file.getInputStream(), attachmentInputStreamSupplier, pages);
        } catch (Exception e) {
            logger.error("overlayPostProcessor failed {attachment='{}', pages='{}'}", attachmentParam, pages, e);
            IOUtils.copy(file.getInputStream(), response.getOutputStream());
        }
    }



    @PostMapping(path = "/merge", consumes = MULTIPART_FORM_DATA_VALUE)
    public void process(@RequestParam("file") MultipartFile file,
                        @RequestParam(name = "attachment", defaultValue = "0") String attachmentParam,
                        @RequestParam(name = "position", defaultValue = "before") String position,
                        HttpServletResponse response) throws IOException {

        PdfAttachment pdfAttachment = extractPdfAttachment(attachmentParam);
        Supplier<InputStream> attachmentInputStreamSupplier = getPdfAttachmentImputStreamSupplier(pdfAttachment);

        try {
            mergePostProcessor.merge(response.getOutputStream(), file.getInputStream(), attachmentInputStreamSupplier, position);
        } catch (Exception e) {
            logger.error("mergePostProcessor failed {attachment='{}', position='{}'}", attachmentParam, position, e);
            IOUtils.copy(file.getInputStream(), response.getOutputStream());
        }
    }



    private Supplier<InputStream> getPdfAttachmentImputStreamSupplier(PdfAttachment pdfAttachment) {
        //TODO make ENUM
        switch (pdfAttachment.getType()) {
            case "file":
                return () -> {
                    try {
                        return new FileInputStream(pdfAttachment.getSource());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Could not get '" + pdfAttachment.getName() + "' pdf attachment input stream " + pdfAttachment.getSource() + "'", e);
                    }
                };

            case "url":
                return () -> {
                    try {
                        return new URL(pdfAttachment.getSource()).openStream();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not get '" + pdfAttachment.getName() + "' pdf attachment input stream " + pdfAttachment.getSource() + "'", e);
                    }
                };
        }
        throw new IllegalArgumentException("Unknown pdf attachment type '" + pdfAttachment.getType() + '\'');
    }

    private PdfAttachment extractPdfAttachment(String attachmentParam) {
        try {
            int attachmentIndex = Integer.parseInt(attachmentParam);
            return orderedAttachments.get(attachmentIndex);
        } catch (NumberFormatException nfe) {
            return namedAttachments.get(attachmentParam);
        }
    }

}
