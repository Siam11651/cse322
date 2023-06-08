package Offline_1.Response;

import java.util.Vector;

public class ClientsListResponse extends Response
{
    final private Vector<String> clientUserNames;

    public ClientsListResponse(Vector<String> clientUserNames)
    {
        this.clientUserNames = new Vector<>(clientUserNames.size());

        for(int i = 0; i < clientUserNames.size(); ++i)
        {
            this.clientUserNames.set(i, new String(clientUserNames.get(i)));
        }
    }

    public Vector<String> GetClientsList()
    {
        return clientUserNames;
    }
}
