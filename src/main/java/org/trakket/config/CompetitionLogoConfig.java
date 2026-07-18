package org.trakket.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CompetitionLogoConfig {

    private static String s3Bucket;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @PostConstruct
    private void init() {
        s3Bucket = bucket;
    }

    public static String getLogoUrl(String competitionKey) {
        if (s3Bucket == null || s3Bucket.isEmpty() || "replace".equals(s3Bucket)) {
            return null;
        }
        return "https://" + s3Bucket + ".s3.amazonaws.com/competitions/" + competitionKey + ".png";
    }
}
