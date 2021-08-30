package model;

public class Driver {
    public String email;
    public String lat;
    public String lng;
    public String name;
    public String password;
    public String vehiclenumber;

    Driver() {
    }

    public Driver(String name, String email, String password, String vehiclenumber, String lat, String lng) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.vehiclenumber = vehiclenumber;
        this.lat = lat;
        this.lng = lng;
    }
}
