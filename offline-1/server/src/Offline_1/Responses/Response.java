package Offline_1.Responses;

import java.io.Serializable;
import java.util.Date;

public class Response implements Serializable
{
    private Date date;

    public Response()
    {
        date = new Date();
    }

    public Date GetDate()
    {
        return date;
    }
}
