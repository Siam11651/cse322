package Offline_1.Requests;

public class MessagesRequest extends Request
{
    private boolean all;

    public MessagesRequest(boolean all)
    {
        this.all = all;
    }

    public boolean ShallGetAll()
    {
        return all;
    }
}
