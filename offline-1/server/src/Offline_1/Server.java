package Offline_1;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

public class Server extends Thread
{
    final public static long MAX_BUFFER_SIZE = 1000000000; // 1 GB 🤡
    final public static long MIN_CHUNK_SIZE = 1000;
    final public static long MAX_CHUNK_SIZE = 1000000;
    final public static int PORT = 6969;
    private boolean running;
    private static Server server;
    private ServerSocket serverSocket;
    private Vector<Client> loggedInClients;
    private Hashtable<String, String> fileRequests;
    private Hashtable<String, String> uploadTracker;

    private Server()
    {
        running = true;
        loggedInClients = new Vector<>();
        fileRequests = new Hashtable<>();
        uploadTracker = new Hashtable<>();

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

    public synchronized Vector<File> GetPrivateFilesList(String userName)
    {
        File userRoot = new File("root", userName);

        if(userRoot.exists())
        {
            File privateRoot = new File(userRoot, "private");
            File files[] = privateRoot.listFiles();
            
            return new Vector<>(Arrays.asList(files));
        }
        else
        {
            return null;
        }
    }

    public synchronized Vector<File> GetPublicFilesList(String userName)
    {
        File userRoot = new File("root", userName);

        if(userRoot.exists())
        {
            File publicRoot = new File(userRoot, "public");
            File files[] = publicRoot.listFiles();

            return new Vector<>(Arrays.asList(files));
        }
        else
        {
            return null;
        }
    }

    public synchronized Hashtable<String, String> GetUploadTracker()
    {
        return uploadTracker;
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

    public synchronized Hashtable<String, String> GetFileRequests()
    {
        return fileRequests;
    }

    public synchronized long GetUsedBufferSize(String userName)
    {
        File file = new File("root", userName);

        return file.length();
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
