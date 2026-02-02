package com.nagarro.training.rajesh.az_app.controller;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/files")
@Slf4j
public class FileController {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @GetMapping("/get-img")
    public ResponseEntity<byte[]> getTestImage(@RequestParam String fileName) {
        try {
            // Create BlobServiceClient
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            // Get container client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Get blob client for the specific file
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            // Check if blob exists
            if (!blobClient.exists().booleanValue()) {
                return ResponseEntity.notFound().build();
            }

            // Download the blob content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            byte[] imageData = outputStream.toByteArray();

            // Set appropriate headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageData.length);
            headers.setCacheControl("public, max-age=3600"); // Cache for 1 hour

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);

        } catch (Exception e) {
            log.error("Error retrieving image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}