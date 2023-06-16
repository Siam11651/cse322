package Offline_1.Responses;

public class LoginResponse extends Response
{
    boolean successful;

    public LoginResponse(boolean successful)
    {
        this.successful = true;
    }

    public boolean IsSuccessful()
    {
        return successful;
    }
}
