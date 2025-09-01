package workout;

public class User {
    private int id;
    private String username;
    private String name;
    private String email;
    private int age;
    private String profileImagePath;
    private String preferredUnit;

    // Constructor, getters, setters
    public User(int id, String username, String name, String email, int age, String profileImagePath, String preferredUnit) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.age = age;
        this.profileImagePath = profileImagePath;
        this.preferredUnit = preferredUnit;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
    public String getPreferredUnit() { return preferredUnit; }
    public void setPreferredUnit(String preferredUnit) { this.preferredUnit = preferredUnit; }
}