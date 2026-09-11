package ultratraining.com.webapp.dto;

public class UserDto {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String auth0Id;

    // Getters and setters
    public void setAuth0Id(String auth0Id) { this.auth0Id = auth0Id; }
    public String getAuth0Id() { return auth0Id; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
