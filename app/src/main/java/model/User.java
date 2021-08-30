package model;

public class User {
    public String email;
    public String name;
    public String password;

    public User(String email, String name, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
