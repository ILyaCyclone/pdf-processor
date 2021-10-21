package ru.unisuite.pdfwatermark;

import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class OverlayPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OverlayPostProcessor.class);

    public void overlay(ServletOutputStream out, InputStream originalDocumentInputStream,
                        Supplier<InputStream> overlayDocumentInputStreamSupplier,
                        String pagesString) throws IOException {
        try (
                PDDocument originalDocument = PDDocument.load(originalDocumentInputStream);
                Overlay overlay = new Overlay();
                PDDocument overlayDocument = PDDocument.load(overlayDocumentInputStreamSupplier.get())
        ) {
            Set<Integer> pages = getPageNumbersToOverlay(pagesString, originalDocument);

            overlay.setInputPDF(originalDocument);
            overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
//        overlay.setOverlayPosition(Overlay.Position.BACKGROUND);

//        overlay.setAllPagesOverlayFile(watermarkPdfPath); // doesn't work?


            Map<Integer, PDDocument> pagesOverlayDocuments = pages.stream()
                    .collect(Collectors.toMap(Function.identity(), page -> overlayDocument));
            overlay.overlayDocuments(pagesOverlayDocuments);

            originalDocument.save(out);
        }
    }



    /**
     * page numbers start with 1
     */
    private Set<Integer> getPageNumbersToOverlay(String pagesString, PDDocument originalDocument) {
        if ("first".equals(pagesString) || "1".equals(pagesString)) return Collections.singleton(1);
        if ("all".equals(pagesString)) return allPages(originalDocument);

        Set<Integer> pages = new HashSet<>();
        for (String page : pagesString.split(",")) {
            switch (page) {
                case "all":
                    return allPages(originalDocument);
                case "first":
                    pages.add(1);
                    break;
                case "last":
                    pages.add(originalDocument.getNumberOfPages());
                    break;
                case "even":
                    pages.addAll(allPagesStream(originalDocument).filter(num -> num % 2 == 0).collect(Collectors.toSet()));
                    break;
                case "odd":
                    pages.addAll(allPagesStream(originalDocument).filter(num -> num % 2 != 0).collect(Collectors.toSet()));
                    break;
                default:
                    try {
                        pages.add(Integer.parseInt(page));
                    } catch (NumberFormatException e) {
                        logger.warn("Unsupported page parameter '{}'", page);
                    }
            }
        }
        return pages;
    }

    private Set<Integer> allPages(PDDocument originalDocument) {
        return allPagesStream(originalDocument).collect(Collectors.toSet());
    }

    private Stream<Integer> allPagesStream(PDDocument originalDocument) {
        return IntStream.rangeClosed(1, originalDocument.getNumberOfPages()).boxed();
    }
}
