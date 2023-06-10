package Offline_1.Responses;

public class LoginSuccessfulResponse extends Response
{
    private String userName;

    public LoginSuccessfulResponse(String userName)
    {
        this.userName = new String(userName);
    }

    public String GetUserName()
    {
        return userName;
    }
}
