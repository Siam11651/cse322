package Offline_1;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable
{
    private String content;
    private String sender;
    private Date sentDate;

    public Message(String sender, String content)
    {
        this.sender = new String(sender);
        this.content = new String(content);
        this.sentDate = new Date(); // consider it sent when object created
    }

    public Message(Message other)
    {
        this.sender = new String(other.sender);
        this.content = new String(other.content);
        this.sentDate = new Date(other.sentDate.getTime());
    }

    public Date GetSentDate()
    {
        return sentDate;
    }

    public String GetSender()
    {
        return sender;
    }

    public String GetContent()
    {
        return content;
    }
}
