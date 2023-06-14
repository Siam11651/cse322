package Offline_1.Responses;

public class LoginResponse extends Response
{
    private String userName;

    public LoginResponse(String userName)
    {
        this.userName = new String(userName);
    }

    public String GetUserName()
    {
        return userName;
    }

    public boolean IsSuccessful()
    {
        return userName.length() > 0;
    }
}
