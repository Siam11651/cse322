package Offline_1;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import Offline_1.Threads.ReadThread;
import Offline_1.Threads.WriteThread;
import Offline_1.Response.Response;
import Offline_1.Response.ResponsePoller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Client
{
    private String userName;
    final private Socket socket;
    final private ReadThread readThread;
    final private WriteThread writeThread;

    public Client(Socket socket) throws IOException
    {
        userName = "";
        this.socket = socket;
        readThread = new ReadThread(this, new ObjectInputStream(socket.getInputStream()));
        writeThread = new WriteThread(new ObjectOutputStream(socket.getOutputStream()));

        readThread.start();
        writeThread.start();

        System.out.println("Connected to remote socket address: " + socket.getRemoteSocketAddress());
    }

    public void SetUserName(String userName)
    {
        this.userName = new String(userName);
    }

    public String GetUserName()
    {
        return userName;
    }

    public Socket GetSocket()
    {
        return socket;
    }

    public synchronized void SendResponse(Response response)
    {
        ResponsePoller responsePoller = writeThread.GetResponsePoller();

        responsePoller.SetResponse(response);
    }

    public synchronized void Close()
    {
        readThread.Stop();
        writeThread.Stop();

        SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();

        try
        {
            socket.close();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }

        Server.GetServer().RemoveClient(this);
        System.out.println("Client with remote socket address " + remoteSocketAddress + " disconnected");
    }
}
