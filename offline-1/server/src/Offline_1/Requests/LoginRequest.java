package Offline_1.Requests;

public class LoginRequest extends Request
{
    private String userName;

    public LoginRequest(String userName)
    {
        this.userName = userName;
    }

    public String GetUserName()
    {
        return userName;
    }
}
