package Offline_1.Requests;

public class FileRequest extends Request
{
    private String description;
    private String requestId;

    public FileRequest(String requestId, String description)
    {
        this.requestId = new String(requestId);
        this.description = new String(description);
    }

    public String GetRequestId()
    {
        return requestId;
    }

    public String GetDescription()
    {
        return description;
    }
}
