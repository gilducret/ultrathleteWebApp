package ultratraining.com.webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import ultratraining.com.webapp.client.UserApiClient;
import ultratraining.com.webapp.dto.UserDto;

import java.util.List;

@Controller
public class HomeController {

    private final UserApiClient userApiClient;

    @Value("${ultratraining.api.url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public HomeController(UserApiClient userApiClient) {
        this.userApiClient = userApiClient;
    }

    @GetMapping("/")
    public String home() {
        return "home";   // → src/main/resources/templates/home.html
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {

        String displayName = oidcUser.getFullName() != null
                ? oidcUser.getFullName() : oidcUser.getEmail();
        model.addAttribute("displayName", displayName);

        UserDto user = userApiClient.findByAuth0Id(oidcUser.getSubject());
        model.addAttribute("userId", user != null ? user.getId() : 0);

        // Load planned races
        if (user != null) {
            try {
                String url = apiBaseUrl + "/api/predictedsession/user/"
                        + user.getId();
                ResponseEntity<String> response =
                        restTemplate.getForEntity(url, String.class);
                model.addAttribute("plannedRaces", response.getBody());
            } catch (Exception e) {
                model.addAttribute("plannedRaces", "[]");
            }
        } else {
            model.addAttribute("plannedRaces", "[]");
        }

        // Show success message if just created a plan
        return "dashboard";
    }

    @GetMapping("/upload")
    public String upload(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        String displayName = oidcUser.getFullName() != null
                ? oidcUser.getFullName()
                : oidcUser.getEmail();
        model.addAttribute("displayName", displayName);

        // Get user ID from ultratraining
        UserDto user = userApiClient.findByAuth0Id(oidcUser.getSubject());
        model.addAttribute("userId", user != null ? user.getId() : 0);

        return "upload";
    }

    @GetMapping("/predict")
    public String predict(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        String displayName = oidcUser.getFullName() != null
                ? oidcUser.getFullName()
                : oidcUser.getEmail();
        model.addAttribute("displayName", displayName);

        UserDto user = userApiClient.findByAuth0Id(oidcUser.getSubject());
        model.addAttribute("userId", user != null ? user.getId() : 0);

        return "predict";
    }
}