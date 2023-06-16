package Offline_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import Offline_1.Requests.DownloadRequest;
import Offline_1.Requests.FileRequest;
import Offline_1.Requests.FilesListRequest;
import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.LogoutRequest;
import Offline_1.Requests.MessagesRequest;
import Offline_1.Requests.MessagesSeenRequest;
import Offline_1.Requests.UploadRequest;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Requests.FilesListRequest.Privacy;
import Offline_1.Responses.FileRequestResponse;
import Offline_1.Responses.FilesListResponse;
import Offline_1.Responses.LoginResponse;
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

    public Client(String host, int port)
    {
        running = true;
        userName = "";

        try
        {
            socket = new Socket(host, port);
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            socket.setSoTimeout(30000);
            start();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
            Close(); // close client on connection error 😭
        }
    }

    @Override
    public void run()
    {
        System.out.println("Connected to server");
        System.out.print("> ");

        while(running)
        {
            try
            {
                int available = System.in.available();

                if(available > 0)
                {
                    byte consoleInputByteArray[] = new byte[available];

                    System.in.read(consoleInputByteArray);

                    String tokenizedCommand[] = (new String(consoleInputByteArray)).trim().split(" ");

                    if(tokenizedCommand[0].equals(Commands.LOGIN))
                    {
                        if(IsLoggedIn())
                        {
                            System.out.println("Already logged in as " + GetUserName());
                        }
                        else
                        {
                            if(tokenizedCommand.length > 1)
                            {
                                objectOutputStream.writeObject(new LoginRequest(tokenizedCommand[1]));

                                LoginResponse loginResponse = (LoginResponse)objectInputStream.readObject();

                                if(loginResponse.IsSuccessful())
                                {
                                    SetUserName(tokenizedCommand[1]);
                                    System.out.println("Logged in as "+ tokenizedCommand[1]);
                                }
                                else
                                {
                                    System.out.println("Login failed");
                                }
                            }
                            else
                            {
                                System.err.println(Commands.LOGIN + " command needs an argument");
                            }
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.USERS_LIST))
                    {
                        if(IsLoggedIn())
                        {
                            objectOutputStream.writeObject(new UsersListRequest());

                            UsersListResponse usersListResponse = (UsersListResponse)objectInputStream.readObject();
                            Vector<UserActivityPair> usersList = usersListResponse.GetUsersList();

                            System.out.println("Lisiting users:");

                            for(int i = 0; i < usersList.size(); ++i)
                            {
                                UserActivityPair userActivityPair = usersList.get(i);
                                String activity = "inactive";

                                if(userActivityPair.IsActive())
                                {
                                    activity = "active";
                                }

                                System.out.println((i + 1) + ". " + userActivityPair.GetUsername() + " (" + activity + ")");
                            }
                        }
                        else
                        {
                            System.out.println("You need to log in to get access to this information");
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.FILES_LIST))
                    {
                        if(IsLoggedIn())
                        {
                            if(tokenizedCommand.length > 1)
                            {
                                Privacy privacyEnum = Privacy.PRIVATE;

                                if(tokenizedCommand.length > 2)
                                {
                                    if(tokenizedCommand[2].equals(Commands.FilePrivacy.PUBLIC))
                                    {
                                        privacyEnum = Privacy.PUBLIC;

                                    }
                                    else if(tokenizedCommand[2].equals(Commands.FilePrivacy.ALL))
                                    {
                                        privacyEnum = Privacy.ALL;
                                    }
                                    else if(tokenizedCommand[2].equals(Commands.FilePrivacy.PRIVATE))
                                    {
                                        privacyEnum = Privacy.PRIVATE;
                                    }
                                }

                                if(!tokenizedCommand[1].equals(userName) && (privacyEnum == Privacy.PRIVATE || privacyEnum == Privacy.ALL))
                                {
                                    System.out.println("Only owner can get access to private files");
                                }
                                else
                                {
                                    objectOutputStream.writeObject(new FilesListRequest(userName, privacyEnum));

                                    FilesListResponse filesListResponse = (FilesListResponse)objectInputStream.readObject();
                                    Vector<String> publicFiles = filesListResponse.GetPublicFilesList();
                                    Vector<String> privateFiles = filesListResponse.GetPrivateFilesList();

                                    if(publicFiles != null)
                                    {
                                        System.out.println("Lisiting public files:");

                                        for(int i = 0; i < publicFiles.size(); ++i)
                                        {
                                            System.out.println((i + 1) + ". " + publicFiles.get(i));
                                        }
                                    }

                                    if(privateFiles != null)
                                    {
                                        System.out.println("Lisiting private files:");

                                        for(int i = 0; i < privateFiles.size(); ++i)
                                        {
                                            System.out.println((i + 1) + ". " + privateFiles.get(i));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                System.out.println("Not enough arguments for this command");
                            }
                        }
                        else
                        {
                            System.out.println("You need to log in to get access to this information");
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.MESSAGES))
                    {
                        if(IsLoggedIn())
                        {
                            boolean getAll = false;

                            if(tokenizedCommand.length > 1 && tokenizedCommand[1].equals(Commands.MessagesArgments.ALL))
                            {
                                getAll = true;
                            }

                            objectOutputStream.writeObject(new MessagesRequest(getAll));

                            MessagesResponse messagesResponse = (MessagesResponse)objectInputStream.readObject();
                            Vector<Message> messages = messagesResponse.GetMessageList();

                            if(messages.size() == 0)
                            {
                                System.out.println("No messages to show");
                                objectOutputStream.writeObject(new MessagesSeenRequest(null));
                            }
                            else
                            {
                                String messageTypes = "unread";
                                Vector<Integer> messageIndicesSeen = new Vector<>();

                                if(getAll)
                                {
                                    messageTypes = "all";
                                }

                                System.out.println("Showing " + messageTypes + " messages:");

                                for(int i = 0; i < messages.size(); ++i)
                                {
                                    System.out.println((i + 1) + ". Message from " + messages.get(i).GetSender());
                                    System.out.println(messages.get(i).GetContent());
                                    messageIndicesSeen.add(messages.get(i).GetId());
                                }

                                objectOutputStream.writeObject(new MessagesSeenRequest(messageIndicesSeen));
                            }
                        }
                        else
                        {
                            System.out.println("You need to log in to get access to this information");
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.FILE_REQUEST))
                    {
                        if(IsLoggedIn())
                        {
                            if(tokenizedCommand.length >= 3)
                            {
                                objectOutputStream.writeObject(new FileRequest(userName, tokenizedCommand[1], tokenizedCommand[2]));
                                
                                FileRequestResponse fileRequestResponse = (FileRequestResponse)objectInputStream.readObject();

                                if(fileRequestResponse.IsSuccessful())
                                {
                                    System.out.println("File request successful");
                                }
                                else
                                {
                                    System.out.println("File request failed: Another request exists with same id");
                                }
                            }
                            else
                            {
                                System.out.println("Not enough arguments for this command");
                            }
                        }
                        else
                        {
                            System.out.println("You must be logged in to post this request");
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.UPLOAD))
                    {
                        if(IsLoggedIn())
                        {
                            String filePath = tokenizedCommand[1];
                            String fileName = tokenizedCommand[2];
                            String privacyString = tokenizedCommand[3];
                            String requestId = "";

                            if(tokenizedCommand.length > 4)
                            {
                                requestId = tokenizedCommand[4];
                            }

                            Privacy privacy = Privacy.ALL;
                            File file = new File(filePath);
                            boolean uploadRequestable = true;

                            if(privacyString.equals(Commands.FilePrivacy.PUBLIC))
                            {
                                privacy = Privacy.PUBLIC;
                            }
                            else if(privacyString.equals(Commands.FilePrivacy.PRIVATE))
                            {
                                privacy = Privacy.PRIVATE;
                            }
                            else 
                            {
                                System.out.println("Invalid privacy argument");

                                uploadRequestable = false;
                            }

                            if(!file.exists())
                            {
                                System.out.println("Path specified does not exist");

                                uploadRequestable = false;
                            }

                            if(!file.isFile())
                            {
                                System.out.println("Path specified in not a file");

                                uploadRequestable = false;
                            }
                            
                            if(requestId.length() > 0 && privacy == Privacy.PRIVATE)
                            {
                                System.out.println("No request for private file");

                                uploadRequestable = false;
                            }

                            if(uploadRequestable)
                            {
                                long fileSize = file.length();

                                objectOutputStream.writeObject(new UploadRequest(fileName, fileSize, privacy, requestId));

                                UploadRespone uploadRespone = (UploadRespone)objectInputStream.readObject();
                                String fileId = uploadRespone.GetFileId();

                                if(fileId.length() > 0)
                                {
                                    FileInputStream fileInputStream = new FileInputStream(file);
                                    long chunkSize = uploadRespone.GetChunkSize();
                                    long chunkCount = fileSize / chunkSize;
                                    long lastChunkSize = fileSize % chunkSize;
                                    UploadAcknowledge uploadAcknowledge = null;
                                    long uploadedSize = 0;
                                    
                                    for(long i = 0; i < chunkCount;  ++i)
                                    {
                                        byte chunk[] = new byte[(int)chunkSize];

                                        fileInputStream.read(chunk);

                                        objectOutputStream.writeObject(new UploadData(fileId, chunk));
                                        uploadAcknowledge = (UploadAcknowledge)objectInputStream.readObject();

                                        if(uploadAcknowledge.IsOk())
                                        {
                                            uploadedSize += chunkSize;

                                            System.out.println("Uploaded " + ((uploadedSize * 100) / fileSize) + "% of file");
                                        }
                                        else
                                        {
                                            break;
                                        }
                                    }

                                    if(uploadAcknowledge != null && uploadAcknowledge.IsOk() && lastChunkSize > 0)
                                    {
                                        byte chunk[] = new byte[(int)lastChunkSize];

                                        fileInputStream.read(chunk);

                                        objectOutputStream.writeObject(new UploadData(fileId, chunk));
                                        uploadAcknowledge = (UploadAcknowledge)objectInputStream.readObject();
                                        uploadedSize += lastChunkSize;
                                    }

                                    fileInputStream.close();

                                    if(uploadAcknowledge != null && uploadAcknowledge.IsOk())
                                    {
                                        System.out.println("Uploaded " + ((uploadedSize * 100) / fileSize) + "% of file");
                                        objectOutputStream.writeObject(new UploadComplete());
                                        objectInputStream.readObject();
                                        System.out.println("Upload completed successfully");
                                    }
                                    else
                                    {
                                        System.out.println("Error occured in file upload");
                                    }
                                }
                                else
                                {
                                    System.out.println("Server cannot keep any more file");
                                }
                            }
                        }
                        else
                        {
                            System.out.println("You must be logged in to post this request");
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.DOWNLOAD))
                    {
                        if(IsLoggedIn())
                        {
                            String userName = tokenizedCommand[1];
                            String fileName = tokenizedCommand[2];
                            String downloadDestination = tokenizedCommand[3];
                            String privacyString = "";
                            Privacy privacy = Privacy.PUBLIC;

                            if(tokenizedCommand.length > 4)
                            {
                                privacyString = tokenizedCommand[4];

                                if(privacyString.equals(Commands.FilePrivacy.PUBLIC))
                                {
                                    privacy = Privacy.PUBLIC;
                                }
                                else if(privacyString.equals(Commands.FilePrivacy.PRIVATE))
                                {
                                    privacy = Privacy.PRIVATE;
                                }
                            }

                            objectOutputStream.writeObject(new DownloadRequest(userName, fileName, privacy));
                            DownloadData downloadData = (DownloadData)objectInputStream.readObject();
                            File file = new File(downloadDestination);

                            file.createNewFile();

                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            boolean successful = true;

                            while(true)
                            {
                                if(downloadData.IsOk())
                                {
                                    try
                                    {
                                        fileOutputStream.write(downloadData.GetChunk());

                                        System.out.println("Downloaded " + (file.length() * 100) / downloadData.GetTotalSize() + "%");

                                        if(downloadData.IsLastChunk())
                                        {
                                            break;
                                        }
                                        
                                        objectOutputStream.writeObject(new DownloadAcknowledge(true));
                                        downloadData = (DownloadData)objectInputStream.readObject();
                                    }
                                    catch(IOException exception)
                                    {
                                        fileOutputStream.close();
                                        file.delete();

                                        throw exception;
                                    }
                                }
                                else
                                {
                                    System.out.println("Cannot download");

                                    successful = false;
                                }
                            }

                            fileOutputStream.close();

                            if(!successful)
                            {
                                file.delete();
                            }
                        }
                        else
                        {
                            System.out.println("You must be logged in to download a file");
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.LOGOUT))
                    {
                        if(IsLoggedIn())
                        {
                            objectOutputStream.writeObject(new LogoutRequest());
                            objectInputStream.readObject();
                            System.out.println("Logging out");
                            Close();

                            userName = "";
                        }
                        else
                        {
                            System.out.println("You must be logged in to logout");
                        }
                    }

                    if(IsLoggedIn())
                    {
                        System.out.print(userName + " > ");
                    }
                    else
                    {
                        System.out.print("> ");
                    }
                }
            }
            catch(IOException exception)
            {
                System.err.println("Server disconnected");
                Close();
                exception.printStackTrace();
            }
            catch(ClassNotFoundException exception)
            {
                exception.printStackTrace();
            }
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
