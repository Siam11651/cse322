package Offline_1;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable, Comparable<Message>
{
    private int id;
    private String content;
    private String sender;
    private boolean seen;
    private Date sentDate;

    public Message(int id, String sender, String content)
    {
        this.sender = new String(sender);
        this.content = new String(content);
        this.sentDate = new Date(); // consider it sent when object created
        seen = false;
    }

    public Message(Message other)
    {
        this.sender = new String(other.sender);
        this.content = new String(other.content);
        this.sentDate = new Date(other.sentDate.getTime());
        this.seen = other.seen;
        this.id = other.id;
    }
    
    @Override
    public int compareTo(Message other)
    {
        if(sentDate.getTime() < other.sentDate.getTime())
        {
            return -1;
        }
        else if(sentDate.getTime() == other.sentDate.getTime())
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    public int GetId()
    {
        return id;
    }

    public void SetSeen(boolean seen)
    {
        this.seen = seen;
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

    public boolean IsSeen()
    {
        return seen;
    }
}
