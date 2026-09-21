package ultratraining.com.webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ultratraining.com.webapp.client.UserApiClient;
import ultratraining.com.webapp.dto.UserDto;

@Controller
@CrossOrigin
@RequestMapping("/races")
public class PlannedRaceController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserApiClient userApiClient;

    @Value("${ultratraining.api.url}")
    private String apiBaseUrl;

    public PlannedRaceController(UserApiClient userApiClient) {
        this.userApiClient = userApiClient;
    }

    /**
     * GET /races/{sessionId}
     * Shows the prediction detail page for one planned race
     */
    @GetMapping("/{sessionId}")
    public String raceDetail(
            @PathVariable int sessionId,
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {

        String displayName = oidcUser.getFullName() != null
                ? oidcUser.getFullName() : oidcUser.getEmail();
        model.addAttribute("displayName", displayName);
        model.addAttribute("sessionId", sessionId);
        return "race-detail";
    }

    /**
     * GET /api/races/{sessionId}
     * Returns full session JSON — called by race-detail.html via fetch
     */
    @GetMapping("/api/{sessionId}")
    @ResponseBody
    public ResponseEntity<String> getSession(
            @PathVariable int sessionId) {

        ResponseEntity<String> response = restTemplate.getForEntity(
                apiBaseUrl + "/api/predictedsession/" + sessionId,
                String.class
        );
        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

    /**
     * DELETE /api/races/{sessionId}
     * Deletes a planned race
     */
    @DeleteMapping("/api/{sessionId}")
    @ResponseBody
    public ResponseEntity<Void> deleteSession(
            @PathVariable int sessionId) {

        restTemplate.delete(
                apiBaseUrl + "/api/predictedsession/" + sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public ResponseEntity<String> getUserSessions(
            @PathVariable int userId) {

        ResponseEntity<String> response = restTemplate.getForEntity(
                apiBaseUrl + "/api/predictedsession/user/" + userId,
                String.class
        );
        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }
}
