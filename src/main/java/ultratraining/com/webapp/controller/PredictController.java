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
@RequestMapping("/api/predict")
public class PredictController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserApiClient userApiClient;

    @Value("${ultratraining.api.url}")
    private String apiBaseUrl;

    public PredictController(UserApiClient userApiClient) {
        this.userApiClient = userApiClient;
    }

    @PostMapping("/run")
    public ResponseEntity<String> predict(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal OidcUser oidcUser) throws IOException {

        // Get user from ultratraining
        String auth0Id = oidcUser.getSubject();
        UserDto user = userApiClient.findByAuth0Id(auth0Id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        // Forward to ultratraining
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userId", String.valueOf(user.getId()));

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiBaseUrl + "/api/forecast/predict",
                request,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .body(response.getBody());
    }
}
