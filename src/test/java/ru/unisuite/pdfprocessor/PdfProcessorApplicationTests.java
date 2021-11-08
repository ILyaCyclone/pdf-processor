package ru.unisuite.pdfprocessor;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // add "test" profile so that application-default.yml is not read
class PdfProcessorApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void attachments() {
        ResponseEntity<PdfAttachment[]> responseEntity =
                restTemplate.getForEntity(baseUrl() + "/attachments", PdfAttachment[].class);
        PdfAttachment[] actualAttachments = responseEntity.getBody();

        org.assertj.core.api.Assertions.assertThat(actualAttachments)
                .usingElementComparatorOnFields("name", "type")
                .containsExactly(
                        new PdfAttachment("att1", "file", ""),
                        new PdfAttachment("att2", "file", "")
                );

        org.assertj.core.api.Assertions.assertThat(actualAttachments)
                .as("source property is not null")
                .extracting("source", String.class)
                .allMatch(StringUtils::hasText);
    }

    @ParameterizedTest
    @CsvSource({"att1,after", "att1,before", "att2,both"})
    void merge(String attachment, String position) throws IOException {
        String method = "merge";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("attachment", attachment);
        parameters.put("position", position);

        String filename = "merge&attachment=" + attachment + "&position=" + position;

        performTest(method, parameters, filename);
    }


    @ParameterizedTest
    @CsvSource(value = {"att1;1,last", "att2;odd,2"}, delimiter = ';')
    void overlay(String attachment, String pages) throws IOException {
        String method = "overlay";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("attachment", attachment);
        parameters.put("pages", pages);

        String filename = "overlay&attachment=" + attachment + "&pages=" + pages;

        performTest(method, parameters, filename);
    }


    private void performTest(String method, Map<String, String> parameters, String filename) throws IOException {
        // get rid of random PDF trailer /ID section
        PDDocument actualDocument = PDDocument.load(callPdfProcessor(method, parameters));
        actualDocument.setDocumentId(1L);
        actualDocument.getDocument().getDocumentID().clear();

        Path actualTempFile = Files.createTempFile("pdf-processor_" + filename + "_actual_", ".pdf");
        try (OutputStream actualTempOut = Files.newOutputStream(actualTempFile)) {
            actualDocument.save(actualTempOut);
        }

        ClassPathResource expectedResource = new ClassPathResource("expected/" + filename + ".pdf");
        try (InputStream expectedResourceIn = expectedResource.getInputStream();
             InputStream actualTempFileIn = Files.newInputStream(actualTempFile)) {
            Assertions.assertTrue(IOUtils.contentEquals(expectedResourceIn, actualTempFileIn)
                    , () -> {
                        try {
                            Path expectedTempFile = Files.createTempFile("pdf-processor_" + filename + "_expected_", ".pdf");
                            Files.copy(expectedResourceIn, expectedTempFile, StandardCopyOption.REPLACE_EXISTING);

                            return "Result content not equal expected file. Result file saved to " + actualTempFile.toAbsolutePath()
                                    + ", expected file saved to " + expectedTempFile.toAbsolutePath();
                        } catch (IOException e) {
                            return "Result content not equal expected file. Could not save result to temp file fir visual comparison (" + e.getMessage() + ')';
                        }
                    });
        }
    }

    private InputStream callPdfProcessor(String method, Map<String, String> parameters) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", originalDocumentResource());
        parameters.forEach(body::add);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = baseUrl() + '/' + method;

        ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(url, requestEntity, byte[].class);
        if (responseEntity.getStatusCode().isError()) {
            throw new RuntimeException("Error calling PDF processor, result code: " + responseEntity.getStatusCode());
        }

        return new ByteArrayInputStream(responseEntity.getBody());
    }

    private ClassPathResource originalDocumentResource() {
        return new ClassPathResource("original.pdf");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

}
