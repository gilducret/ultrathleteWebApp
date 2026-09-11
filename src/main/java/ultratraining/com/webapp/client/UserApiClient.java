package ultratraining.com.webapp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ultratraining.com.webapp.dto.UserDto;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class UserApiClient {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public UserApiClient(
            @Value("${ultratraining.api.url}") String apiBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.apiBaseUrl = apiBaseUrl;
    }

    /**
     * Find user by email.
     * Returns null if not found (404).
     */
    public UserDto findByEmail(String email) {
        try {
            return restTemplate.getForObject(
                    apiBaseUrl + "/api/user/email/{email}",
                    UserDto.class,
                    email
            );
        } catch (HttpClientErrorException.NotFound e) {
            return null; // user doesn't exist yet
        }
    }

    public int createUser(String auth0Id, String firstName,
                          String lastName, String email) {
        UserDto user = new UserDto();
        user.setAuth0Id(auth0Id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        ResponseEntity<Integer> response = restTemplate.postForEntity(
                apiBaseUrl + "/api/user",
                user,
                Integer.class
        );

        Integer id = response.getBody();
        return id != null ? id : -1;  // -1 means something went wrong
    }

    public UserDto findByAuth0Id(String auth0Id) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(apiBaseUrl + "/api/user/auth0/{auth0Id}")
                    .build(auth0Id); // builds and encodes correctly — no double encoding

            return restTemplate.getForObject(uri, UserDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}