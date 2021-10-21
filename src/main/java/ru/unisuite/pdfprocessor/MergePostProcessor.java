package ru.unisuite.pdfprocessor;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

@Service
public class MergePostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MergePostProcessor.class);

    public void merge(ServletOutputStream out, InputStream originalDocumentInputStream,
                      Supplier<InputStream> attachmentDocumentInputStreamSupplier,
                      String position) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationStream(out);

        InputStream attachmentDocumentInputStream = attachmentDocumentInputStreamSupplier.get();

        switch (position) {
            case "before":
                merger.addSource(attachmentDocumentInputStream);
                merger.addSource(originalDocumentInputStream);
                break;
            case "after":
                merger.addSource(originalDocumentInputStream);
                merger.addSource(attachmentDocumentInputStream);
                break;
            case "before,after":
            case "both":
                merger.addSource(attachmentDocumentInputStream);
                merger.addSource(originalDocumentInputStream);
                merger.addSource(attachmentDocumentInputStreamSupplier.get());
                break;
            default:
                logger.warn("Unsupported merge position '{}', using default 'before'", position);
                merger.addSource(attachmentDocumentInputStream);
                merger.addSource(originalDocumentInputStream);
        }

        merger.mergeDocuments(MemoryUsageSetting.setupMixed(3 * 1024 * 1000 * 1000));
    }

}
