package Offline_1.Request;

import java.io.Serializable;

public class Request implements Serializable
{
    private String data;

    public Request()
    {
        SetData(null);
    }

    public Request(String data)
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
