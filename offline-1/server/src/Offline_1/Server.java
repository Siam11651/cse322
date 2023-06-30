package Offline_1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import Offline_1.UploadBuffer.UploadBuffer;

public class Server extends Thread
{
    final public static String ROOT_DIR_NAME = "root";
    final public static String USER_DIR_NAME = "users";
    final public static String FILE_REQUESTS_FILE_NAME = "file-requests";
    final public static long MAX_BUFFER_SIZE = 1000000000L; // 1 GB 🤡
    final public static long MIN_CHUNK_SIZE = 1000000;
    final public static long MAX_CHUNK_SIZE = 5000000;
    final public static int PORT = 6969;
    private static int fileId;
    private boolean running;
    private static Server server;
    private ServerSocket serverSocket;
    private Hashtable<String, Client> loggedInClients;
    private UploadBuffer uploadBuffer;

    private Server()
    {
        running = true;
        fileId = 0;
        loggedInClients = new Hashtable<>();
        uploadBuffer = new UploadBuffer();
        File fileRequestsFile = new File(ROOT_DIR_NAME, FILE_REQUESTS_FILE_NAME);

        try
        {
            if(!fileRequestsFile.exists())
            {
                File rootDir = new File(ROOT_DIR_NAME);

                rootDir.mkdirs();
                fileRequestsFile.createNewFile();

                FileOutputStream fileRequestsFileOutputStream = new FileOutputStream(fileRequestsFile);
                ObjectOutputStream fileRequestsObjectOutputStream = new ObjectOutputStream(fileRequestsFileOutputStream);

                fileRequestsObjectOutputStream.writeObject(new FileRequests());

                fileRequestsObjectOutputStream.close();
            }

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

    public synchronized static int GenerateFileId()
    {
        return ++fileId;
    }

    public synchronized Vector<File> GetPrivateFilesList(String userName)
    {
        File userRoot = new File(ROOT_DIR_NAME + "/" + USER_DIR_NAME + "/" + userName);
        File privateRoot = new File(userRoot, Client.USER_PRIVATE_DIR_NAME);
        File files[] = privateRoot.listFiles();

        if(files.length > 0)
        {            
            return new Vector<>(Arrays.asList(files));
        }
        else
        {
            return null;
        }
    }

    public synchronized Vector<File> GetPublicFilesList(String userName)
    {
        File userRoot = new File(ROOT_DIR_NAME + "/" + USER_DIR_NAME + "/" + userName);
        File publicRoot = new File(userRoot, Client.USER_PUBLIC_DIR_NAME);
        File files[] = publicRoot.listFiles();

        if(files.length > 0)
        {
            return new Vector<>(Arrays.asList(files));
        }
        else
        {
            return null;
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

    public synchronized Hashtable<String, Client> GetLoggedInClients()
    {
        return loggedInClients;
    }

    public synchronized UploadBuffer GetUploadBuffer()
    {
        return uploadBuffer;
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
