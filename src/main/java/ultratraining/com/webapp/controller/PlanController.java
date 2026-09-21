package ultratraining.com.webapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ultratraining.com.webapp.client.UserApiClient;
import ultratraining.com.webapp.dto.UserDto;

import java.io.IOException;

@Controller
@RequestMapping("/plan")
public class PlanController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserApiClient userApiClient;

    @Value("${ultratraining.api.url}")
    private String apiBaseUrl;

    public PlanController(UserApiClient userApiClient) {
        this.userApiClient = userApiClient;
    }

    /** Show the plan page */
    @GetMapping
    public String plan(@AuthenticationPrincipal OidcUser oidcUser,
                       Model model) {
        String displayName = oidcUser.getFullName() != null
                ? oidcUser.getFullName() : oidcUser.getEmail();
        model.addAttribute("displayName", displayName);

        UserDto user = userApiClient.findByAuth0Id(oidcUser.getSubject());
        model.addAttribute("userId", user != null ? user.getId() : 0);
        return "plan";
    }

    @PostMapping("/analyse")
    @ResponseBody
    public ResponseEntity<String> analyse(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal OidcUser oidcUser) throws IOException {

        UserDto user = userApiClient.findByAuth0Id(oidcUser.getSubject());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", resource);
        body.add("userId", String.valueOf(user.getId()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiBaseUrl + "/api/forecast/analyse",
                request,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<String> generate(
            @RequestBody String payload) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                apiBaseUrl + "/api/forecast/generate",
                request,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .body(response.getBody());
    }
}
