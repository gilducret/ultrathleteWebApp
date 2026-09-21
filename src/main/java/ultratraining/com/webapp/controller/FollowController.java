package ultratraining.com.webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * FollowController — webapp proxy for the public follower page.
 *
 * The follow page is PUBLIC — no login required.
 * The runner shares a link like: https://ultrathlete.run/follow/42
 * Anyone with the link can see the runner's position and ETAs.
 *
 * All data calls are proxied to the ultratraining backend.
 */
@Controller
@RequestMapping("/follow")
public class FollowController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ultratraining.api.url}")
    private String apiBaseUrl;

    // ─────────────────────────────────────────────────────────────
    // Public follower page — no authentication needed
    // URL: /follow/{sessionId}
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{sessionId}")
    public String followPage(
            @PathVariable int sessionId,
            Model model) {
        model.addAttribute("sessionId", sessionId);
        return "follow";
    }

    // ─────────────────────────────────────────────────────────────
    // Proxy: GET session info
    // Returns race name, route GPS points, elevation profile,
    // and checkpoint names/distances — called once on page load
    // URL: /follow/api/{sessionId}/session
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/api/{sessionId}/session")
    @ResponseBody
    public ResponseEntity<String> getSession(
            @PathVariable int sessionId) {

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    apiBaseUrl + "/api/follow/" + sessionId + "/session",
                    String.class
            );
            return ResponseEntity
                    .status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Could not load session\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Proxy: GET live position + ETAs
    // Polled every 30 seconds by the follower page
    // Returns runner's last known GPS + predicted arrival times
    // URL: /follow/api/{sessionId}/position
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/api/{sessionId}/position")
    @ResponseBody
    public ResponseEntity<String> getPosition(
            @PathVariable int sessionId) {

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    apiBaseUrl + "/api/follow/" + sessionId + "/position",
                    String.class
            );
            return ResponseEntity
                    .status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Could not load position\"}");
        }
    }
}