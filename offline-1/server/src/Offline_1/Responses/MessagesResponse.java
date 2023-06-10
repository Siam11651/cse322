package Offline_1.Responses;

import java.util.Vector;

import Offline_1.Message;

public class MessagesResponse extends Response
{
    private Vector<Message> messageList;

    public MessagesResponse(Vector<Message> messageList)
    {
        this.messageList = new Vector<>();

        for(int i = 0; i < messageList.size(); ++i)
        {
            this.messageList.add(new Message(messageList.get(i)));
        }
    }

    public Vector<Message> GetMessageList()
    {
        return messageList;
    }
}
