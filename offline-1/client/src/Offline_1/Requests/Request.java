package Offline_1.Requests;

import java.io.Serializable;
import java.util.Date;

public class Request implements Serializable
{
    private Date date;

    public Request()
    {
        date = new Date();
    }

    public Date GetDate()
    {
        return date;
    }
}
