package ltd.cylleneworks.sandbox.springboot.hbase;


public class User {
    private String username;
    private String fullname;
    private String email;
    private String dob;

    public User(String username, String fullname, String email, String dob) {
        super();
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.dob = dob;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getDob() { return dob; }
}
