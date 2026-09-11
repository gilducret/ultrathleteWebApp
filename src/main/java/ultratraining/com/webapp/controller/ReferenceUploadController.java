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
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal OidcUser oidcUser) throws IOException {

        System.out.println(">>> upload called, files: " + files.length);

        String auth0Id = oidcUser.getSubject();
        System.out.println(">>> auth0Id: " + auth0Id);

        UserDto user = userApiClient.findByAuth0Id(auth0Id);
        System.out.println(">>> user found: " + (user != null ? user.getId() : "null"));

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        System.out.println(">>> forwarding to ultratraining...");

        // Build multipart request to forward to ultratraining
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userId", String.valueOf(user.getId()));

        for (MultipartFile file : files) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("files", resource);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiBaseUrl + "/api/forecast/reference",
                request,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .body(response.getBody());
    }
}
