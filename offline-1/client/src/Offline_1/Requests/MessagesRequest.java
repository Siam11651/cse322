package Offline_1.Requests;

public class MessagesRequest extends Request
{
    private int latestCount;

    public MessagesRequest(int latestCount)
    {
        this.latestCount = latestCount;
    }

    public int GetLatestCount()
    {
        return latestCount;
    }
}
