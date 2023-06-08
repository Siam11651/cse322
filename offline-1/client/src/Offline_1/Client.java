package Offline_1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import Offline_1.Threads.ReadThread;
import Offline_1.Threads.WriteThread;

public class Client
{
    private String userName;
    private boolean closed;
    private boolean loggedIn;
    private static String host;
    private static  int port;
    private Socket socket;
    private static Client client;
    private ReadThread readThread;
    private WriteThread writeThread;

    private Client()
    {
        loggedIn = false;
        closed = false;
        socket = null;

        try
        {
            socket = new Socket(host, port);
            writeThread = new WriteThread(new ObjectOutputStream(socket.getOutputStream()));
            readThread = new ReadThread(new ObjectInputStream(socket.getInputStream()));

            readThread.start();
            writeThread.start();
        }
        catch(IOException exception)
        {
            if(exception instanceof ConnectException)
            {
                System.err.println("Connection to server refused, try again");
            }
            else
            {
                exception.printStackTrace();
            }
        }
    }

    public static Client GetClient()
    {
        if(client == null)
        {
            client = new Client();
        }

        return client;
    }

    public static void InitialiseClient(String host, int port)
    {
        if(client == null)
        {
            Client.host = host;
            Client.port = port;
        }
        else
        {
            // throw exception
        }
    }

    public synchronized void SetUserName(String userName)
    {
        this.userName = userName;
    }

    public synchronized String GetUserName()
    {
        return userName;
    }

    public synchronized void SetLoggedIn(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    public boolean IsLoggedIn()
    {
        return loggedIn;
    }

    public synchronized void Close()
    {
        if(!closed)
        {
            closed = true;

            readThread.Stop();
            writeThread.Stop();

            try
            {
                if(!socket.isClosed())
                {
                    socket.close();
                }
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
