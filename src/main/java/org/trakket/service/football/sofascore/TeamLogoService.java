package org.trakket.service.football.sofascore;

import org.springframework.stereotype.Service;
import org.trakket.enums.Gender;
import org.trakket.service.amazon.S3StorageService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class TeamLogoService {

    private final S3StorageService s3StorageService;

    public TeamLogoService(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    public String downloadAndUploadLogo(Long teamId, Long sofascoreTeamId) {
        try {
            String sofascoreUrl = "https://img.sofascore.com/api/v1/team/" + sofascoreTeamId + "/image";
            HttpURLConnection conn = (HttpURLConnection) new URL(sofascoreUrl).openConnection();
            conn.setRequestMethod("GET");

            try (InputStream is = conn.getInputStream()) {
                String objectKey = "teams/" + teamId + ".png";
                return s3StorageService.uploadFile(objectKey, is, conn.getContentLengthLong(), "image/png");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download/upload team logo from SofaScore", e);
        }
    }
}