package Offline_1.Requests;

public class FileRequest extends Request
{
    private String description;
    private String requestId;
    private String sender;

    public FileRequest(String sender, String requestId, String description)
    {
        this.sender = new String(sender);
        this.requestId = new String(requestId);
        this.description = new String(description);
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof FileRequest)
        {
            if(((FileRequest)other).requestId.equals(requestId))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public FileRequest(FileRequest other)
    {
        this.sender = new String(other.sender);
        this.requestId = new String(other.requestId);
        this.description = new String(other.description);
    }

    public String GetSender()
    {
        return sender;
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
