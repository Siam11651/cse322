package Offline_1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

import Offline_1.Threads.ReadThread;
import Offline_1.Threads.WriteThread;

public class Client
{
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
        readThread.Stop();
        writeThread.Stop();

        try
        {
            socket.close();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
