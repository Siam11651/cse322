package Offline_1;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import Offline_1.Requests.FileRequest;
import Offline_1.Requests.FilesListRequest;
import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.MessagesRequest;
import Offline_1.Requests.Request;
import Offline_1.Requests.UploadRequest;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Requests.FilesListRequest.Privacy;
import Offline_1.Responses.FilesListResponse;
import Offline_1.Responses.LoginResponse;
import Offline_1.Responses.UploadRespone;
import Offline_1.Responses.UsersListResponse;

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
                    Vector<Client> loggedInClients = Server.GetServer().GetLoggedInClients();
                    boolean successful = true;

                    synchronized(loggedInClients)
                    {
                        for(int i = 0; i < loggedInClients.size(); ++i)
                        {
                            if(loggedInClients.get(i).GetUserName().equals(userName))
                            {
                                successful = false;

                                break;
                            }
                        }

                        if(successful)
                        {
                            loggedInClients.add(this);
                        }
                    }

                    if(successful)
                    {
                        SetUserName(loginRequest.GetUserName());

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
                    Vector<String> usersList = new Vector<>();
                    Vector<Client> loggedInClients = Server.GetServer().GetLoggedInClients();

                    synchronized(loggedInClients)
                    {
                        for(int i = 0; i < loggedInClients.size(); ++i)
                        {
                            usersList.add(loggedInClients.get(i).GetUserName());
                        }

                        objectOutputStream.writeObject(new UsersListResponse(usersList));
                    }
                }
                else if(request instanceof FilesListRequest)
                {
                    if(IsLoggedIn())
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
                    else
                    {

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

                    for(int i = messageList.size() - 1; i >= messageList.size() - latestCount; --i)
                    {
                        latestMessages.add(messageList.get(i));
                    }

                    messagesObjectInputStream.close();
                    fileInputStream.close();
                }
                else if(request instanceof FileRequest)
                {
                    FileRequest fileRequest = (FileRequest)request;
                    Hashtable<String, String> fileRequests = Server.GetServer().GetFileRequests();

                    synchronized(fileRequests)
                    {
                        fileRequests.put(fileRequest.GetRequestId(), fileRequest.GetDescription());
                    }

                    Vector<Client> loggedInClients = Server.GetServer().GetLoggedInClients();
                    
                    synchronized(loggedInClients)
                    {
                        for(int i = 0; i < loggedInClients.size(); ++i)
                        {
                            if(!loggedInClients.get(i).GetUserName().equals(GetUserName()))
                            {
                                String loggedInUserName = loggedInClients.get(i).GetUserName();
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
                        Hashtable<String, String> uploadTracker = Server.GetServer().GetUploadTracker();

                        synchronized(uploadTracker)
                        {
                            uploadTracker.put(userName, uploadRequest.GetFileName());
                        }

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

                        file.createNewFile();

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

                            fileOutputStream.close();

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
                            fileOutputStream.close();
                            file.delete();
                        }

                        synchronized(uploadTracker)
                        {
                            uploadTracker.remove(userName);
                        }
                    }
                    else
                    {
                        objectOutputStream.writeObject(new UploadRespone("", chunkSize));
                    }
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

    public synchronized void Stop()
    {
        running = false;
    }

    public synchronized void Close()
    {
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
