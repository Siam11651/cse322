package Offline_1;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Vector;

import Offline_1.Threads.ReadThread;
import Offline_1.Threads.WriteThread;
import Offline_1.Response.Response;
import Offline_1.Response.ResponsePoller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

public class Client
{
    private boolean closed;
    private String userName;
    final private Socket socket;
    final private ReadThread readThread;
    final private WriteThread writeThread;
    private File root;

    public Client(Socket socket) throws IOException
    {
        closed = false;
        userName = "";
        this.socket = socket;
        readThread = new ReadThread(this, new ObjectInputStream(socket.getInputStream()));
        writeThread = new WriteThread(new ObjectOutputStream(socket.getOutputStream()));

        readThread.start();
        writeThread.start();

        System.out.println("Connected to remote socket address: " + socket.getRemoteSocketAddress());
    }

    public synchronized void SetRoot(File root)
    {
        this.root = root;
    }

    public synchronized File GetRoot()
    {
        return root;
    }

    public synchronized Vector<File> GetPublicFiles()
    {
        File publicFileDir = new File(root, "public");
        File publicFiles[] = publicFileDir.listFiles();
        
        return new Vector<>(Arrays.asList(publicFiles));
    }

    public synchronized Vector<File> GetPrivateFiles()
    {
        File publicFileDir = new File(root, "private");
        File publicFiles[] = publicFileDir.listFiles();
        
        return new Vector<>(Arrays.asList(publicFiles));
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
        if(!closed)
        {
            closed = true;

            readThread.Stop();
            writeThread.Stop();

            SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();

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

            Server.GetServer().RemoveClient(this);
            System.out.println("Client with remote socket address " + remoteSocketAddress + " disconnected");
        }
    }
}
