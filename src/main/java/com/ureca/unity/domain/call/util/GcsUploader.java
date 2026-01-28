package com.ureca.unity.domain.call.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class GcsUploader {

    @Value("${google.cloud.project-id}") private String projectId;
    @Value("${google.cloud.bucket-name}") private String bucketName;
    @Value("${google.cloud.credentials.location}") private String keyPath;

    public String uploadWav(
            String objectName,
            Path wavPath
    ) throws Exception {
        Resource resource = new DefaultResourceLoader().getResource(keyPath);
        GoogleCredentials credentials =
                GoogleCredentials.fromStream(resource.getInputStream());

        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();

        log.error("DEBUG GCS PARAMS >>> bucketName=[{}], objectName=[{}], wavPath=[{}]",
                bucketName, objectName, wavPath);

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("audio/wav")
                .build();

        storage.create(blobInfo, Files.readAllBytes(wavPath));

        return "gs://" + bucketName + "/" + objectName;
    }
}
