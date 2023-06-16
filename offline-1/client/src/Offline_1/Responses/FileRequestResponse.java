package Offline_1.Responses;

public class FileRequestResponse extends Response
{
    private boolean successful;

    public FileRequestResponse(boolean successful)
    {
        this.successful = successful;
    }

    public boolean IsSuccessful()
    {
        return successful;
    }
}
