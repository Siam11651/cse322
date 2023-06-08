package Offline_1;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

import Offline_1.Response.Response;

public class Server
{
    private ServerSocket serverSocket;
    private static Server server;
    final private Vector<Client> clients;

    private Server()
    {
        clients = new Vector<>();

        try
        {
            serverSocket = new ServerSocket(6666);

            System.out.println("Launched server");
            System.out.println("Local socket address: " + serverSocket.getLocalSocketAddress());
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public void Connect() throws IOException
    {
        Client client = new Client(serverSocket.accept());
        
        clients.add(client);
        client.SendResponse(new Response("connection-established"));
    }

    public synchronized void RemoveClient(Client client)
    {
        clients.remove(client);
    }

    public synchronized boolean LoginClient(String userName, Client client)
    {
        for(int i = 0; i < clients.size(); ++i)
        {
            if(clients.get(i).GetUserName().equals(userName))
            {
                clients.remove(client);

                return false;
            }
        }

        File root = new File("./root", userName);

        client.SetUserName(userName);
        client.SetRoot(root);

        File publicFileDir = new File(root, "public");
        File privateFileDir = new File(root, "private");

        if(!publicFileDir.exists())
        {
            publicFileDir.mkdirs();
        }

        if(!privateFileDir.exists())
        {
            privateFileDir.mkdirs();
        }

        return true;
    }

    public static Server GetServer()
    {
        if(server == null)
        {
            server = new Server();
        }

        return server;
    }

    public synchronized Vector<Client> GetClients()
    {
        return clients;
    }
}
