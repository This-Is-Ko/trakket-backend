package org.trakket.service.football.sofascore;

import org.springframework.stereotype.Service;
import org.trakket.enums.Gender;
import org.trakket.service.amazon.S3StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
public class TeamLogoService {

    private final S3StorageService s3StorageService;

    public TeamLogoService(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    public String downloadAndUploadLogo(Long teamId, Long sofascoreTeamId) {
        String sofascoreUrl = "https://img.sofascore.com/api/v1/team/" + sofascoreTeamId + "/image";
        HttpURLConnection conn = null;

        try {
            URI uri = URI.create(sofascoreUrl);
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");

            try (InputStream is = conn.getInputStream()) {
                String objectKey = "teams/" + teamId + ".png";
                return s3StorageService.uploadFile(objectKey, is, conn.getContentLengthLong(), "image/png");
            }

        } catch (IOException ioEx) {
            String stage = (conn == null) ? "connecting" : "downloading";
            throw new RuntimeException(
                    String.format("Error %s from SofaScore URL: %s", stage, sofascoreUrl), ioEx);
        } catch (Exception uploadEx) {
            throw new RuntimeException(
                    String.format("Upload to S3 failed for team %d (SofaScore %d)", teamId, sofascoreTeamId),
                    uploadEx
            );
        }
    }
}
