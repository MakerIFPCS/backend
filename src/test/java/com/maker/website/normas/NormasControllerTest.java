package com.maker.website.normas;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormasControllerTest {

    @Test
    void servesPolicyPdf() throws Exception {
        var response = new NormasController().getPolicyPdf();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("inline; filename=\"politica_de_uso.pdf\"",
                response.getHeaders().getFirst("Content-Disposition"));
        assertEquals(true, response.getBody().exists());
    }
}
