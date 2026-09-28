package ultratraining.com.webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ultratraining.com.webapp.client.UserApiClient;
import ultratraining.com.webapp.dto.UserDto;

import java.io.IOException;

@RestController
@RequestMapping("/api/reference")
public class ReferenceUploadController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserApiClient userApiClient;

    @Value("${ultratraining.api.url}")
    private String apiBaseUrl;

    public ReferenceUploadController(UserApiClient userApiClient) {
        this.userApiClient = userApiClient;
    }

    /**
     * POST /api/reference/upload
     * Receives GPX files from the browser, forwards them to ultratraining.
     * Returns the Models JSON so the browser can display R² quality indicators.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal OidcUser oidcUser) throws IOException {

        // Get the user's database ID from ultratraining
        String auth0Id = oidcUser.getSubject();
        UserDto user = userApiClient.findByAuth0Id(auth0Id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"User not found\"}");
        }

        // Build multipart request to forward to ultratraining
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userId", String.valueOf(user.getId()));

        for (MultipartFile file : files) {
            final String filename = file.getOriginalFilename();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            body.add("files", resource);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        // Forward to ultratraining — response is Models JSON
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiBaseUrl + "/api/forecast/reference",
                request,
                String.class
        );

        // Return JSON response to browser
        return ResponseEntity
                .status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }
}