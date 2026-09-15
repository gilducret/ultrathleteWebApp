package ultratraining.com.webapp.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ultratraining.com.webapp.client.UserApiClient;
import ultratraining.com.webapp.dto.UserDto;

import java.util.List;

@Controller
public class HomeController {

    private final UserApiClient userApiClient;

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
                ? oidcUser.getFullName()
                : oidcUser.getEmail();

        model.addAttribute("displayName", displayName);
        model.addAttribute("races", List.of()); // will come from API later
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