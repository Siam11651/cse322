package Offline_1;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

public class Server extends Thread
{
    final private int PORT = 6969;
    private boolean running;
    private static Server server;
    private ServerSocket serverSocket;
    private Vector<Client> loggedInClients;

    private Server()
    {
        running = true;
        loggedInClients = new Vector<>();

        try
        {
            serverSocket = new ServerSocket(PORT);

            System.out.println("Launched server with local socket address: " + serverSocket.getLocalSocketAddress());
            start();
        }
        catch(IOException exception)
        {
            Stop();
            exception.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        while(running)
        {
            try
            {
                new Client(serverSocket.accept());
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public synchronized static Server GetServer()
    {
        if(server == null)
        {
            server = new Server();
        }

        return server;
    }

    public synchronized Vector<Client> GetLoggedInClients()
    {
        return loggedInClients;
    }

    public synchronized void Stop()
    {
        running = false;
    }

    public synchronized void Close()
    {
        Stop();

        try
        {
            serverSocket.close();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
