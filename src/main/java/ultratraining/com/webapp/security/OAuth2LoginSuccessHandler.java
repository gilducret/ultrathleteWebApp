package ultratraining.com.webapp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ultratraining.com.webapp.client.UserApiClient;
import ultratraining.com.webapp.dto.UserDto;
import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserApiClient userApiClient;

    public OAuth2LoginSuccessHandler(UserApiClient userApiClient) {
        super("/dashboard");
        this.userApiClient = userApiClient;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String auth0Id   = oidcUser.getSubject();  // "auth0|64abc123..."
        String email     = oidcUser.getEmail();
        String firstName = oidcUser.getGivenName();
        String lastName  = oidcUser.getFamilyName();

        // Fall back if Auth0 doesn't have given/family name
        if (firstName == null) {
            String full = oidcUser.getFullName();
            if (full != null && full.contains(" ")) {
                firstName = full.substring(0, full.indexOf(" "));
                lastName  = full.substring(full.indexOf(" ") + 1);
            } else {
                firstName = full != null ? full : email.split("@")[0];
                lastName  = "";
            }
        }

        // Use auth0Id as primary lookup — not email
        UserDto existing = userApiClient.findByAuth0Id(auth0Id);
        if (existing == null) {
            userApiClient.createUser(auth0Id, firstName, lastName, email);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}