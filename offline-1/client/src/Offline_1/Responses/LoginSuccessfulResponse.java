package Offline_1.Responses;

public class LoginSuccessfulResponse extends Response
{
    private String userName;

    public LoginSuccessfulResponse(String userName)
    {
        this.userName = userName;
    }

    public String GetUserName()
    {
        return userName;
    }
}
