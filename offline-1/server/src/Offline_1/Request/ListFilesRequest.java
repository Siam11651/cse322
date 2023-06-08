package Offline_1.Request;

public class ListFilesRequest extends Request
{
    public enum RequestType
    {
        ALL, PUBLIC, PRIVATE
    }

    final private RequestType requestType;
    final private String userName;

    public ListFilesRequest(String userName, RequestType requestType)
    {
        this.requestType = requestType;
        this.userName = userName;
    }

    public RequestType GetRequestType()
    {
        return requestType;
    }

    public String GetUserName()
    {
        return userName;
    }
}
