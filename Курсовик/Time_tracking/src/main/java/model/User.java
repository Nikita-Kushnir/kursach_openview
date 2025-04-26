package model;

public class User {
    private int id;
    private String name;
    private String login;
    private String role;

    public User(int id, String name, String login, String role) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.role = role;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLogin() { return login; }
    public String getRole() { return role; }
}
