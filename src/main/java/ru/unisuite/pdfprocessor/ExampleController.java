package ru.unisuite.pdfprocessor;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/example")
@RequiredArgsConstructor
public class ExampleController {
    private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);

    String watermarkImagePath = "D:\\idea_workspace\\pdf-watermark\\temp\\water.png";
    String watermarkPdfPath = "D:\\idea_workspace\\pdf-watermark\\temp\\water-eagle.pdf";

    private final Map<String, PdfAttachment> namedAttachments;
    private final List<PdfAttachment> orderedAttachments;

    @PostMapping("/example/blend")
    public void processFile(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(name = "p", defaultValue = "first") String pages) throws IOException {
        InputStream originalPdfInputStream = request.getInputStream();
        waterWithPdf(originalPdfInputStream, response.getOutputStream());
    }

    //    @PostMapping(path = "/blend/multipart", consumes = MULTIPART_FORM_DATA_VALUE)
    @PostMapping(path = "/example/blend/multipart")
    public void processMultipartFile(
            @RequestParam("file") MultipartFile file
            , HttpServletResponse response) throws IOException {
        InputStream originalPdfInputStream = file.getInputStream();
        waterWithPdf(originalPdfInputStream, response.getOutputStream());
    }


    @GetMapping("/example/image")
//    public ResponseEntity<Resource> download() {
    public void image(HttpServletResponse response) throws IOException {
        String pdfRelativeUrl = "/content/Положение об АБП?id_vf=1753803";
        String pdfAbsoluteUrl = "http://wl3n1.miit.ru:7003" + pdfRelativeUrl;

        InputStream originalPdfInputStream = getOriginalPdf(pdfAbsoluteUrl);

        waterWithImage(originalPdfInputStream, watermarkImagePath, response.getOutputStream());

//        writeWatermark(originalPdfInputStream, response.getOutputStream());

//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    } else {
//        return ResponseEntity.notFound().build();
//    }
    }


    @GetMapping("/example/prepend")
    public void pdfAppend(HttpServletResponse response) throws IOException {
        String pdfRelativeUrl = "/content/Положение об АБП?id_vf=1753803";
        String pdfAbsoluteUrl = "http://wl3n1.miit.ru:7003" + pdfRelativeUrl;
        InputStream originalPdfInputStream = getOriginalPdf(pdfAbsoluteUrl);

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.addSource(watermarkPdfPath);
        merger.addSource(originalPdfInputStream);
        merger.setDestinationStream(response.getOutputStream());
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }

    @GetMapping("/example/pdf")
    public void pdf(HttpServletResponse response) throws IOException {
        String pdfRelativeUrl = "/content/Положение об АБП?id_vf=1753803";
        String pdfAbsoluteUrl = "http://wl3n1.miit.ru:7003" + pdfRelativeUrl;


        InputStream originalPdfInputStream = getOriginalPdf(pdfAbsoluteUrl);

        waterWithPdf(originalPdfInputStream, response.getOutputStream());
    }



    private InputStream getOriginalPdf(String url) throws IOException {
        return new UrlResource(new URL(url)).getInputStream();
    }

    private void waterWithPdf(InputStream originalPdfInputStream, OutputStream outputStream) throws IOException {

        PDDocument realDoc = PDDocument.load(originalPdfInputStream);

        Overlay overlay = new Overlay();
        overlay.setInputPDF(realDoc);
        overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
//        overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
//        overlay.setAllPagesOverlayFile(watermarkPdfPath);


        PDDocument watermarkDocument = PDDocument.load(new File(watermarkPdfPath));

//        Map<Integer, String> overlayProps = new HashMap<>();
//        for (int i = 0; i < realDoc.getNumberOfPages(); i++) {
//            overlayProps.put(i + 1, watermarkPdfPath);
//        }
//        overlay.overlay(overlayProps);

//        Map<Integer, PDDocument> pagesOverlayDocuments = new HashMap<>();
//        for (int i = 0; i < realDoc.getNumberOfPages(); i++) {
//            pagesOverlayDocuments.put(i + 1, watermarkDocument);
//        }
//        overlay.overlayDocuments(pagesOverlayDocuments);

//        overlay.setFirstPageOverlayPDF(watermarkDocument); // nothing happens ???

        Map<Integer, PDDocument> specificPageOverlayDocuments = Collections.singletonMap(1, watermarkDocument);
        overlay.overlayDocuments(specificPageOverlayDocuments);

        realDoc.save(outputStream);
        watermarkDocument.close();
        overlay.close();
        realDoc.close();
    }

    void waterWithImage(InputStream originalPdfInputStream, String imagePath, OutputStream outputStream) throws IOException {
        try (PDDocument document = PDDocument.load(originalPdfInputStream)) {
            PDImageXObject imageXObject = PDImageXObject.createFromFile(imagePath, document);
            document.getPages().forEach(page -> water(document, page, imageXObject));
            document.save(outputStream);
        }
    }

    void water(PDDocument document, PDPage page, PDImageXObject imageXObject) {
        try (PDPageContentStream contentStream
                     = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            PDRectangle pageSize = page.getMediaBox();

            contentStream.drawImage(imageXObject, 200, 50);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public Resource loadFileAsResource(String url) throws Exception {
//        try {
//            Resource resource = new UrlResource(new URL(url));
//            if (resource.exists()) {
//                return resource;
//            } else {
//                throw new FileNotFoundException("File not found " + url);
//            }
//        } catch (MalformedURLException ex) {
//            throw new FileNotFoundException("File not found " + url);
//        }
//    }

}
