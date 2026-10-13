package com.maker.website.normas;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/api/normas")
public class NormasController {

    private static final String POLICY_PDF_PATH = "static/politica_de_uso.pdf";

    @GetMapping("/pdf")
    public ResponseEntity<Resource> getPolicyPdf() throws IOException {
        Resource policyPdf = new ClassPathResource(POLICY_PDF_PATH);
        if (!policyPdf.exists() || !policyPdf.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(policyPdf.contentLength())
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"politica_de_uso.pdf\"")
                .body(policyPdf);
    }
}
