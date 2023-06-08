package Offline_1.Response;

import java.io.Serializable;

public class Response implements Serializable
{
    private String data;

    public Response()
    {
        SetData(null);
    }

    public Response(String data)
    {
        SetData(data);
    }

    public void SetData(String data)
    {
        this.data = data;
    }

    public String GetData()
    {
        return data;
    }
}
