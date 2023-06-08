package Offline_1.Response;

public class ResponsePoller
{
    private Response response;

    public ResponsePoller()
    {
        response = null;
    }

    public synchronized void SetResponse(Response response)
    {
        this.response = response;
    }

    public synchronized Response GetRespone()
    {
        return response;
    }

    public synchronized void EndResponse()
    {
        response = null;
    }
}
