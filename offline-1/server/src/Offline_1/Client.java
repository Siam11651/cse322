package Offline_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import Offline_1.Requests.DownloadRequest;
import Offline_1.Requests.FileRequest;
import Offline_1.Requests.FilesListRequest;
import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.LogoutRequest;
import Offline_1.Requests.MessagesRequest;
import Offline_1.Requests.MessagesSeenRequest;
import Offline_1.Requests.Request;
import Offline_1.Requests.UploadRequest;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Requests.FilesListRequest.Privacy;
import Offline_1.Responses.FileRequestResponse;
import Offline_1.Responses.FilesListResponse;
import Offline_1.Responses.LoginResponse;
import Offline_1.Responses.LogoutResponse;
import Offline_1.Responses.MessagesResponse;
import Offline_1.Responses.UploadRespone;
import Offline_1.Responses.UsersListResponse.UserActivityPair;
import Offline_1.Responses.UsersListResponse.UsersListResponse;
import Offline_1.UploadBuffer.UploadBuffer;
import Offline_1.UploadBuffer.UploadFile;

public class Client extends Thread
{
    public static String USER_INBOX_FILE_NAME = "inbox";
    public static String USER_PUBLIC_DIR_NAME = "public";
    public static String USER_PRIVATE_DIR_NAME = "private";
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
    public boolean equals(Object other)
    {
        if(other instanceof Client)
        {
            return ((Client)other).userName.equals(userName);
        }
        else
        {
            return false;
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
                    successful = !loggedInClients.containsKey(newUsername);

                    if(successful)
                    {
                        loggedInClients.put(newUsername, this);
                        
                        SetUserName(newUsername);

                        File userRoot = new File(Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + userName);

                        if(!userRoot.exists())
                        {
                            System.out.println("New user " + userName + " logged in");

                            File userPrivate = new File(userRoot, USER_PRIVATE_DIR_NAME);
                            File userPublic = new File(userRoot, USER_PUBLIC_DIR_NAME);
                            File userInbox = new File(userRoot, USER_INBOX_FILE_NAME);

                            userPrivate.mkdirs();
                            userPublic.mkdirs();
                            userInbox.createNewFile();

                            FileOutputStream messageListFileOutputStream = new FileOutputStream(userInbox);
                            ObjectOutputStream messageListObjectOutputStream = new ObjectOutputStream(messageListFileOutputStream);

                            messageListObjectOutputStream.writeObject(new MessageList());
                            messageListObjectOutputStream.close();
                        }
                        else
                        {
                            System.out.println("Existing user " + userName + " logged in");
                        }

                        objectOutputStream.writeObject(new LoginResponse(true));
                    }
                    else
                    {
                        objectOutputStream.writeObject(new LoginResponse(false));
                    }
                }
                else if(request instanceof UsersListRequest)
                {
                    Vector<UserActivityPair> usersList = new Vector<>();
                    Hashtable<String, Client> loggedInClients = Server.GetServer().GetLoggedInClients();
                    File file = new File(Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME);
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
                    MessagesRequest messagesRequest = (MessagesRequest)request;
                    boolean getAll = messagesRequest.ShallGetAll();
                    File messageListFile = new File(Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + userName + "/" + USER_INBOX_FILE_NAME);
                    FileInputStream messageListFileInputStream = new FileInputStream(messageListFile);
                    ObjectInputStream messageListObjectInputStream = new ObjectInputStream(messageListFileInputStream);
                    MessageList messageList = (MessageList)messageListObjectInputStream.readObject();

                    messageListObjectInputStream.close();

                    Vector<Message> messagesVector = new Vector<>();

                    for(int i = 0; i < messageList.size(); ++i)
                    {
                        if(getAll || !messageList.get(i).IsSeen())
                        {
                            messagesVector.add(messageList.get(i));
                        }
                    }

                    Collections.sort(messagesVector);
                    objectOutputStream.writeObject(new MessagesResponse(messagesVector));

                    MessagesSeenRequest messagesSeenRequest = (MessagesSeenRequest)objectInputStream.readObject();
                    Vector<Integer> seenIndices = messagesSeenRequest.GetMessageIndices();

                    if(seenIndices != null)
                    {
                        for(int i = 0; i < seenIndices.size(); ++i)
                        {
                            messageList.get(i).SetSeen(true);
                        }
                    }

                    FileOutputStream messageListFileOutputStream = new FileOutputStream(messageListFile);
                    ObjectOutputStream messageListObjectOutputStream = new ObjectOutputStream(messageListFileOutputStream);

                    messageListObjectOutputStream.writeObject(messageList);
                    messageListObjectOutputStream.close();
                }
                else if(request instanceof FileRequest)
                {
                    FileRequest fileRequest = (FileRequest)request;
                    File fileRequestsFile = new File(Server.ROOT_DIR_NAME, Server.FILE_REQUESTS_FILE_NAME);
                    FileInputStream fileRequestsFileInputStream = new FileInputStream(fileRequestsFile);
                    ObjectInputStream fileRequestsObjectInputStream = new ObjectInputStream(fileRequestsFileInputStream);
                    FileRequests fileRequests = (FileRequests)fileRequestsObjectInputStream.readObject();
                    
                    fileRequestsObjectInputStream.close();

                    if(fileRequests.contains(fileRequest.GetRequestId()))
                    {
                        objectOutputStream.writeObject(new FileRequestResponse(false));
                    }
                    else
                    {
                        fileRequests.put(fileRequest.GetRequestId(), new FileRequest(fileRequest));

                        FileOutputStream fileRequestsFileOutputStream = new FileOutputStream(fileRequestsFile);
                        ObjectOutputStream fileRequesObjectOutputStream = new ObjectOutputStream(fileRequestsFileOutputStream);

                        fileRequesObjectOutputStream.writeObject(fileRequests);

                        fileRequesObjectOutputStream.close();

                        File userDirectory = new File(Server.ROOT_DIR_NAME, Server.USER_DIR_NAME);
                        File userDirectories[] = userDirectory.listFiles();

                        for(int i = 0; i < userDirectories.length; ++i)
                        {
                            if(userDirectories[i].getName().equals(userName))
                            {
                                continue;
                            }

                            File messageListFile = new File(userDirectories[i], USER_INBOX_FILE_NAME);
                            FileInputStream messageListFileInputStream = new FileInputStream(messageListFile);
                            ObjectInputStream messageListObjectInputStream = new ObjectInputStream(messageListFileInputStream);
                            MessageList messageList = (MessageList)messageListObjectInputStream.readObject();

                            messageListObjectInputStream.close();

                            String messageText = "Can you upload this file for me?\n";
                            messageText += "Request ID: " + fileRequest.GetRequestId() + "\n";
                            messageText += "Description: " + fileRequest.GetDescription() + "\n";

                            messageList.add(new Message(messageList.size(), fileRequest.GetSender(), messageText));

                            FileOutputStream messageListFileOutputStream = new FileOutputStream(messageListFile);
                            ObjectOutputStream messageListObjectOutputStream = new ObjectOutputStream(messageListFileOutputStream);

                            messageListObjectOutputStream.writeObject(messageList);
                            messageListObjectOutputStream.close();
                        }

                        objectOutputStream.writeObject(new FileRequestResponse(true));
                    }
                }
                else if(request instanceof UploadRequest)
                {
                    UploadRequest uploadRequest = (UploadRequest)request;
                    long fileSize = uploadRequest.GetFileSize();
                    UploadBuffer uploadBuffer = Server.GetServer().GetUploadBuffer();
                    long usedBufferSize = uploadBuffer.GetBufferSize();
                    long chunkSize = 0;

                    if(usedBufferSize + fileSize <= Server.MAX_BUFFER_SIZE)
                    {
                        Random random = new Random();
                        chunkSize = Server.MIN_CHUNK_SIZE + Math.abs(random.nextLong()) % (Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE);
                    }

                    if(chunkSize > 0)
                    {
                        int fileId = Server.GenerateFileId();

                        objectOutputStream.writeObject(new UploadRespone(Integer.toString(fileId), chunkSize));

                        long chunkCount = fileSize / chunkSize;
                        long lastChunkSize = fileSize % chunkSize;
                        String filePath;

                        if(uploadRequest.GetPrivacy() == Privacy.PRIVATE)
                        {
                            filePath = Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + userName + "/" + USER_PRIVATE_DIR_NAME + "/" + uploadRequest.GetFileName();
                        }
                        else
                        {
                            filePath = Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + userName + "/" + USER_PUBLIC_DIR_NAME + "/" + uploadRequest.GetFileName();
                        }

                        UploadFile uploadFile = new UploadFile(filePath, (int)fileSize);

                        uploadBuffer.put(Integer.toString(fileId), uploadFile);
                        socket.setSoTimeout(30000);

                        for(long i = 0; i < chunkCount; ++i)
                        {
                            try
                            {
                                UploadData uploadData = (UploadData)objectInputStream.readObject();

                                uploadFile.WriteChunk(uploadData.GetChunk());
                                objectOutputStream.writeObject(new UploadAcknowledge(true));
                            }
                            catch(IOException exception)
                            {
                                socket.setSoTimeout(0);
                                
                                throw exception;
                            }
                        }

                        if(lastChunkSize > 0)
                        {
                            try
                            {
                                UploadData uploadData = (UploadData)objectInputStream.readObject();

                                uploadFile.WriteChunk(uploadData.GetChunk());
                                objectOutputStream.writeObject(new UploadAcknowledge(true));
                            }
                            catch(IOException exception)
                            {
                                socket.setSoTimeout(0);
                                
                                throw exception;
                            }
                        }

                        objectInputStream.readObject();

                        if(uploadFile.GetChunk().length == fileSize)
                        {
                            objectOutputStream.writeObject(new UploadSuccess());
                            uploadBuffer.Write(Integer.toString(fileId));

                            if(!uploadRequest.GetRequestId().isEmpty())
                            {
                                FileInputStream fileRequestsFileInputStream = new FileInputStream(Server.ROOT_DIR_NAME + "/" + Server.FILE_REQUESTS_FILE_NAME);
                                ObjectInputStream fileRequestsObjectInputStream = new ObjectInputStream(fileRequestsFileInputStream);
                                FileRequests fileRequests = (FileRequests)fileRequestsObjectInputStream.readObject();

                                fileRequestsObjectInputStream.close();

                                String requestId = uploadRequest.GetRequestId();
                                FileRequest fileRequest = fileRequests.get(requestId);

                                if(fileRequest != null)
                                {
                                    fileRequests.remove(requestId);

                                    FileOutputStream fileRequestsFileOutputStream = new FileOutputStream(Server.ROOT_DIR_NAME + "/" + Server.FILE_REQUESTS_FILE_NAME);
                                    ObjectOutputStream fileRequestsFileObjectOutputStream = new ObjectOutputStream(fileRequestsFileOutputStream);

                                    fileRequestsFileObjectOutputStream.writeObject(fileRequests);

                                    fileRequestsFileObjectOutputStream.close();

                                    File messageListFile = new File(Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + fileRequest.GetSender(), USER_INBOX_FILE_NAME);
                                    FileInputStream messageListFileInputStream = new FileInputStream(messageListFile);
                                    ObjectInputStream messageListObjectInputStream = new ObjectInputStream(messageListFileInputStream);
                                    MessageList messageList = (MessageList)messageListObjectInputStream.readObject();

                                    messageListObjectInputStream.close();

                                    String messageText = "File request: " + requestId + " has been accepted\n";

                                    messageList.add(new Message(messageList.size(), fileRequest.GetSender(), messageText));

                                    FileOutputStream messageListFileOutputStream = new FileOutputStream(messageListFile);
                                    ObjectOutputStream messageListObjectOutputStream = new ObjectOutputStream(messageListFileOutputStream);

                                    messageListObjectOutputStream.writeObject(messageList);
                                    messageListObjectOutputStream.close();
                                }
                            }
                        }

                        uploadBuffer.remove(Integer.toString(fileId));
                        socket.setSoTimeout(0);
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
                        file = new File(Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + userName + "/" + USER_PRIVATE_DIR_NAME + "/" + fileName);
                    }
                    else
                    {
                        file = new File(Server.ROOT_DIR_NAME + "/" + Server.USER_DIR_NAME + "/" + userName + "/" + USER_PUBLIC_DIR_NAME + "/" + fileName);
                    }

                    if(file.exists())
                    {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        Random random = new Random();
                        long chunkSize = Server.MIN_CHUNK_SIZE + Math.abs(random.nextLong()) % (Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE);
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
                Close();
                exception.printStackTrace();
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
        if(IsLoggedIn())
        {
            System.out.println("User " + userName + " attempting to logout");

            Hashtable<String, Client> loggedInClients = Server.GetServer().GetLoggedInClients();

            synchronized(loggedInClients)
            {
                loggedInClients.remove(userName);

                userName = "";
            }
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
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
        catch(NullPointerException exception)
        {
            exception.printStackTrace();
        }

        try
        {
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
