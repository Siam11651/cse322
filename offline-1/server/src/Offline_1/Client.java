package Offline_1;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import Offline_1.Requests.DownloadRequest;
import Offline_1.Requests.FileRequest;
import Offline_1.Requests.FilesListRequest;
import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.LogoutRequest;
import Offline_1.Requests.MessagesRequest;
import Offline_1.Requests.Request;
import Offline_1.Requests.UploadRequest;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Requests.FilesListRequest.Privacy;
import Offline_1.Responses.FilesListResponse;
import Offline_1.Responses.LoginResponse;
import Offline_1.Responses.LogoutResponse;
import Offline_1.Responses.MessagesResponse;
import Offline_1.Responses.UploadRespone;
import Offline_1.Responses.UsersListResponse.UserActivityPair;
import Offline_1.Responses.UsersListResponse.UsersListResponse;

public class Client extends Thread
{
    private boolean running;
    private String userName;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public Client(Socket socket)
    {
        running = true;
        this.socket = socket;

        try
        {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            start();
            System.out.println("Connected to client with remote socket address: " + socket.getRemoteSocketAddress());
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
            Close(); // stop server on error 😭
        }
    }

    @Override
    public void run()
    {
        while(running)
        {
            try
            {
                Request request = (Request)objectInputStream.readObject();

                if(request instanceof LoginRequest)
                {
                    LoginRequest loginRequest = (LoginRequest)request;
                    Hashtable<String, Client> loggedInClients = Server.GetServer().GetLoggedInClients();
                    boolean successful;
                    String newUsername = loginRequest.GetUserName();

                    synchronized(loggedInClients)
                    {
                        successful = !loggedInClients.containsKey(newUsername);

                        if(successful)
                        {
                            loggedInClients.put(newUsername, this);
                        }
                    }

                    if(successful)
                    {
                        SetUserName(newUsername);

                        File userRoot = new File("root", userName);
                        File userPrivate = new File(userRoot, "private");
                        File userPublic = new File(userRoot, "public");
                        File userInbox = new File(userRoot, "inbox");

                        if(!userPrivate.exists())
                        {
                            userPrivate.mkdirs();
                        }

                        if(!userPublic.exists())
                        {
                            userPublic.mkdirs();
                        }

                        if(userInbox.createNewFile())
                        {
                            FileOutputStream messageListFileOutputStream = new FileOutputStream(userInbox);
                            ObjectOutputStream messageListObjectOutputStream = new ObjectOutputStream(messageListFileOutputStream);

                            messageListObjectOutputStream.writeObject(new MessageList());
                            messageListObjectOutputStream.close();
                            messageListFileOutputStream.close();
                        }

                        objectOutputStream.writeObject(new LoginResponse(loginRequest.GetUserName()));
                    }
                    else
                    {
                        objectOutputStream.writeObject(new LoginResponse(""));
                    }
                }
                else if(request instanceof UsersListRequest)
                {
                    Vector<UserActivityPair> usersList = new Vector<>();
                    Hashtable<String, Client> loggedInClients = Server.GetServer().GetLoggedInClients();
                    File file = new File("root");
                    File userDirectories[] = file.listFiles();

                    for(int i = 0; i < userDirectories.length; ++i)
                    {
                        String directoryName = userDirectories[i].getName();
                        boolean active;

                        synchronized(loggedInClients)
                        {
                            active = loggedInClients.containsKey(directoryName);
                        }

                        usersList.add(new UserActivityPair(directoryName, active));;
                    }

                    objectOutputStream.writeObject(new UsersListResponse(usersList));
                }
                else if(request instanceof FilesListRequest)
                {
                    FilesListRequest filesListRequest = (FilesListRequest)request;
                    String requestUserName = filesListRequest.GetUserName();
                    Privacy privacy = filesListRequest.GetPrivacy();

                    if(privacy == Privacy.PUBLIC)
                    {
                        Vector<File> publicFiles = Server.GetServer().GetPublicFilesList(requestUserName);

                        objectOutputStream.writeObject(new FilesListResponse(null, publicFiles));
                    }
                    else
                    {
                        Vector<File> privateFiles = null;
                        Vector<File> publicFiles = null;

                        if(requestUserName.equals(userName))
                        {
                            privateFiles = Server.GetServer().GetPrivateFilesList(requestUserName);

                            if(privacy == Privacy.ALL)
                            {
                                publicFiles = Server.GetServer().GetPublicFilesList(requestUserName);
                            }
                        }

                        objectOutputStream.writeObject(new FilesListResponse(privateFiles, publicFiles));
                    }
                }
                else if(request instanceof MessagesRequest)
                {
                    File file = new File("root/" + userName + "/inbox");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    ObjectInputStream messagesObjectInputStream = new ObjectInputStream(fileInputStream);
                    MessageList messageList = (MessageList)messagesObjectInputStream.readObject();
                    MessagesRequest messagesRequest = (MessagesRequest)request;
                    int latestCount = messagesRequest.GetLatestCount();

                    if(messagesRequest.GetLatestCount() > 0)
                    {
                        latestCount = messagesRequest.GetLatestCount();
                    }

                    Vector<Message> latestMessages = new Vector<>();

                    if(latestCount == 0)
                    {
                        latestCount = messageList.size();
                    }

                    for(int i = messageList.size() - 1; i >= messageList.size() - latestCount; --i)
                    {
                        latestMessages.add(messageList.get(i));
                    }

                    messagesObjectInputStream.close();
                    fileInputStream.close();
                    objectOutputStream.writeObject(new MessagesResponse(latestMessages));
                }
                else if(request instanceof FileRequest)
                {
                    FileRequest fileRequest = (FileRequest)request;
                    Hashtable<String, String> fileRequests = Server.GetServer().GetFileRequests();

                    synchronized(fileRequests)
                    {
                        fileRequests.put(fileRequest.GetRequestId(), fileRequest.GetDescription());
                    }

                    Hashtable<String, Client> loggedInClients = Server.GetServer().GetLoggedInClients();
                    Iterator<Client> clients;
                    
                    synchronized(loggedInClients)
                    {
                        clients = loggedInClients.elements().asIterator();
                    }

                    while(clients.hasNext())
                    {
                        Client client = clients.next();

                        if(!client.GetUserName().equals(GetUserName()))
                        {
                            String loggedInUserName = client.GetUserName();
                            File messageListFile = new File("root/" + loggedInUserName + "/inbox");
                            FileInputStream messageListFileInputStream = new FileInputStream(messageListFile);
                            ObjectInputStream messageListObjectInputStream = new ObjectInputStream(messageListFileInputStream);
                            MessageList messageList = (MessageList)messageListObjectInputStream.readObject();

                            messageListObjectInputStream.close();
                            messageListFileInputStream.close();

                            String messageText = "New file request from " + GetUserName() + "\n";
                            messageText += "Request ID: " + fileRequest.GetRequestId() + "\n";
                            messageText += "Description: " + fileRequest.GetDescription() + "\n";

                            messageList.add(new Message("", messageText));

                            FileOutputStream messageListFileOutputStream = new FileOutputStream(messageListFile);
                            ObjectOutputStream messageListObjectOutputStream = new ObjectOutputStream(messageListFileOutputStream);

                            messageListObjectOutputStream.writeObject(messageList);
                            messageListObjectOutputStream.close();
                            messageListFileOutputStream.close();
                        }
                    }
                }
                else if(request instanceof UploadRequest)
                {
                    UploadRequest uploadRequest = (UploadRequest)request;
                    long fileSize = uploadRequest.GetFileSize();
                    long usedBufferSize = Server.GetServer().GetUsedBufferSize(userName);
                    long chunkSize = 0;

                    if(usedBufferSize + fileSize <= Server.MAX_BUFFER_SIZE)
                    {
                        Random random = new Random();
                        chunkSize = Server.MIN_CHUNK_SIZE + random.nextLong() % (Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE);
                    }

                    if(chunkSize > 0)
                    {
                        objectOutputStream.writeObject(new UploadRespone(userName, chunkSize));

                        long chunkCount = fileSize / chunkSize;
                        long lastChunkSize = fileSize % chunkSize;

                        File file = null;

                        if(uploadRequest.GetPrivacy() == Privacy.PRIVATE)
                        {
                            file = new File("root/" + userName + "/private", uploadRequest.GetFileName());
                        }
                        else
                        {
                            file = new File("root/" + userName + "/public", uploadRequest.GetFileName());
                        }
                        
                        file.delete();
                        file.createNewFile();
                        socket.setSoTimeout(30000);

                        FileOutputStream fileOutputStream = new FileOutputStream(file);

                        try
                        {
                            for(long i = 0; i < chunkCount; ++i)
                            {
                                UploadData uploadData = (UploadData)objectInputStream.readObject();

                                fileOutputStream.write(uploadData.GetChunk());
                                objectOutputStream.writeObject(new UploadAcknowledge(true));
                            }

                            if(lastChunkSize > 0)
                            {
                                UploadData uploadData = (UploadData)objectInputStream.readObject();

                                fileOutputStream.write(uploadData.GetChunk());
                                objectOutputStream.writeObject(new UploadAcknowledge(true));
                            }

                            objectInputStream.readObject();

                            if(file.length() == fileSize)
                            {
                                objectOutputStream.writeObject(new UploadSuccess());
                            }
                            else
                            {
                                file.delete();
                            }
                        }
                        catch(IOException exception)
                        {
                            if(exception instanceof SocketTimeoutException)
                            {
                                file.delete();
                            }

                            exception.printStackTrace();
                        }

                        socket.setSoTimeout(0);
                        fileOutputStream.close();
                    }
                    else
                    {
                        objectOutputStream.writeObject(new UploadRespone("", chunkSize));
                    }
                }
                else if(request instanceof DownloadRequest)
                {
                    DownloadRequest downloadRequest = (DownloadRequest)request;
                    String fileName = downloadRequest.GetFileName();
                    String userName = downloadRequest.GetUserName();
                    File file;

                    if(userName.equals(GetUserName()) && downloadRequest.GetPrivacy() == Privacy.PRIVATE)
                    {
                        file = new File("root/" + userName + "/private/" + fileName);
                    }
                    else
                    {
                        file = new File("root/" + userName + "/public/" + fileName);
                    }

                    if(file.exists())
                    {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Random random = new Random();
                        long nextLong = random.nextLong() % (Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE);
                        long chunkSize = Server.MIN_CHUNK_SIZE + nextLong;
                        long chunkCount = file.length() / chunkSize;
                        long lastChunkSize = file.length() % chunkSize;

                        for(int i = 0; i < chunkCount; ++i)
                        {
                            byte chunk[] = new byte[(int)chunkSize];

                            fileInputStream.read(chunk);

                            if(lastChunkSize == 0 && i == chunkCount - 1)
                            {
                                objectOutputStream.writeObject(new DownloadData(true, true, chunk, file.length()));
                            }
                            else
                            {
                                objectOutputStream.writeObject(new DownloadData(false, true, chunk, file.length()));
                            }

                            DownloadAcknowledge downloadAcknowledge = (DownloadAcknowledge)objectInputStream.readObject();

                            if(!downloadAcknowledge.IsOK())
                            {
                                lastChunkSize = 0;

                                break;
                            }
                        }

                        if(lastChunkSize > 0)
                        {
                            byte chunk[] = new byte[(int)lastChunkSize];

                            fileInputStream.read(chunk);
                            objectOutputStream.writeObject(new DownloadData(true, true, chunk, file.length()));
                        }

                        fileInputStream.close();
                    }
                    else
                    {
                        objectOutputStream.writeObject(new DownloadData(false, false, null, 0));
                    }
                }
                else if(request instanceof LogoutRequest)
                {
                    Logout();
                    objectOutputStream.writeObject(new LogoutResponse());
                }
            }
            catch(IOException exception)
            {
                if(exception instanceof EOFException)
                {
                    Close();
                }
                else
                {
                    exception.printStackTrace();
                }
            }
            catch(ClassNotFoundException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public synchronized Socket GetSocket()
    {
        return socket;
    }

    public synchronized String GetUserName()
    {
        return userName;
    }

    public synchronized void SetUserName(String userName)
    {
        this.userName = userName;
    }

    public synchronized boolean IsLoggedIn()
    {
        if(userName.length() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public synchronized void Logout()
    {
        System.out.println("User " + userName + " attempting to logout");

        Hashtable<String, Client> loggedInClients = Server.GetServer().GetLoggedInClients();

        synchronized(loggedInClients)
        {
            loggedInClients.remove(userName);

            userName = "";
        }
    }

    public synchronized void Stop()
    {
        running = false;
    }

    public synchronized void Close()
    {
        Logout();
        Stop();
        
        try
        {
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
            System.out.println("Remote socket address " + socket.getRemoteSocketAddress() + " disconnected");
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
        catch(NullPointerException exception)
        {
            exception.printStackTrace();
        }
    }
}
