package Offline_1.Requests;

import java.util.Vector;

public class MessagesSeenRequest extends Request
{
    private Vector<Integer> messageIndices;

    public MessagesSeenRequest(Vector<Integer> messageIndices)
    {
        if(messageIndices != null)
        {
            this.messageIndices = new Vector<>();

            for(int i = 0; i < messageIndices.size(); ++i)
            {
                this.messageIndices.add(messageIndices.get(i));
            }
        }
    }

    public Vector<Integer> GetMessageIndices()
    {
        return messageIndices;
    }
}
