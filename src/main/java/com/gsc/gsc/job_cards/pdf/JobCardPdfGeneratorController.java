package com.gsc.gsc.job_cards.pdf;

import com.gsc.gsc.constants.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Controller
public class JobCardPdfGeneratorController {

    @Autowired
    JobCardPdfGeneratorITextService jobCardPdfGeneratorITextService;

    @GetMapping("/pdf2/{jobCardId}")
    public ResponseEntity<ReturnObject> generateJobCardPdfIText(@RequestHeader("Authorization") String token, @PathVariable int jobCardId, @RequestParam String macAddress,
                                                                @RequestParam(value = "includePrivateNotes",required = false) Boolean includePrivateNotes) throws IOException {
        // Locate your PDF file
        ReturnObject returnObject = jobCardPdfGeneratorITextService.exportIText(token,jobCardId, macAddress,includePrivateNotes);

        if (returnObject.isStatus()) {
            Object data = returnObject.getData();

            // Log the class name of the data to confirm its type
            System.out.println("Data class: " + data.getClass().getName());
            System.out.println("Data: " + data);

            // Check if the data is a String (file path)
            if (data instanceof String) {
                String filePath = (String) data;

                // Read the file content into a byte array
                byte[] pdfBytes = Files.readAllBytes(Paths.get(filePath));

                // Convert the byte array to a Base64 string
                String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
                returnObject.setData("jobCard_"+jobCardId);
                // Return the Base64 encoded PDF string
                return new ResponseEntity<>(returnObject, HttpStatus.OK);
            } else {
                // If the data is not a valid file path or byte array, return an error message
                return new ResponseEntity<>(returnObject, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            // If no job card found or PDF generation failed
            return new ResponseEntity<>(returnObject, HttpStatus.NOT_FOUND);
        }
    }

}
