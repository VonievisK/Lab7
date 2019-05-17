package Lab7;

public class UserAccount {
    private String userName;
    private String password;

    public UserAccount(String userName, String password){
        this.password = password;
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }
}