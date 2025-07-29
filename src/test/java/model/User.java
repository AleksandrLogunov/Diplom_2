package model;

public class User {
    private String email;
    private String password;
    private String name;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static User getRandomUser() {
        long timestamp = System.currentTimeMillis();
        String email = "user" + timestamp + "@mail.ru";
        String password = "pass" + timestamp;
        String name = "User" + timestamp;
        return new User(email, password, name);
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
}