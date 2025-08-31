package org.trakket.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CountryCodeService {

    private static final String FLAGCDN_CODES_URL = "https://flagcdn.com/en/codes.json";

    private final Map<String, String> countryNameToCode;

    public CountryCodeService() {
        this.countryNameToCode = loadCountryCodes();
    }

    private Map<String, String> loadCountryCodes() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(FLAGCDN_CODES_URL, String.class);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> codeToCountry = mapper.readValue(json,
                    new TypeReference<Map<String, String>>() {});

            // Keep only valid ISO 3166-1 alpha-2 codes (2 letters)
            return codeToCountry.entrySet().stream()
                    .filter(e -> e.getKey().matches("^[a-z]{2}$"))
                    .collect(Collectors.toMap(
                            e -> e.getValue().toLowerCase(Locale.ENGLISH),
                            Map.Entry::getKey
                    ));
        } catch (Exception e) {
            log.error("Failed to load country codes", e);
            return Collections.emptyMap();
        }
    }


    public String getCountryCode(String countryName) {
        if (countryName == null) return null;
        return countryNameToCode.get(countryName.toLowerCase(Locale.ENGLISH));
    }
}
