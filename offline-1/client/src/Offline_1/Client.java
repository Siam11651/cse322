package Offline_1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import Offline_1.Requests.FilesListRequest;
import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Requests.FilesListRequest.Privacy;
import Offline_1.Responses.FilesListResponse;
import Offline_1.Responses.LoginResponse;
import Offline_1.Responses.Response;
import Offline_1.Responses.UsersListResponse;

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

        while(running)
        {
            try
            {
                int available = System.in.available();
                boolean waitForResponse = false;

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

                                waitForResponse = true;
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

                            waitForResponse = true;
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
                            if(tokenizedCommand.length >= 3)
                            {
                                if(tokenizedCommand[1].equals(Commands.FilesListArguments.OWN))
                                {
                                    waitForResponse = HandleFilesListOnPrivacy(userName, tokenizedCommand[2]);
                                }
                                else
                                {
                                    waitForResponse = HandleFilesListOnPrivacy(tokenizedCommand[1], tokenizedCommand[2]);
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
                }

                if(waitForResponse)
                {
                    Response response = (Response)objectInputStream.readObject();

                    if(response instanceof LoginResponse)
                    {
                        LoginResponse loginResponse = (LoginResponse)response;

                        if(loginResponse.IsSuccessful())
                        {
                            SetUserName(loginResponse.GetUserName());
                            System.out.println("Logged in as "+ loginResponse.GetUserName());
                        }
                        else
                        {
                            System.out.println("Login failed");
                        }
                    }
                    else if(response instanceof UsersListResponse)
                    {
                        UsersListResponse usersListResponse = (UsersListResponse)response;
                        Vector<String> usersList = usersListResponse.GetUsersList();

                        System.out.println("Lisiting users:");

                        for(int i = 0; i < usersList.size(); ++i)
                        {
                            System.out.println((i + 1) + ". " + usersList.get(i));
                        }
                    }
                    else if(response instanceof FilesListResponse)
                    {
                        FilesListResponse filesListResponse = (FilesListResponse)response;
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
            }
            catch(IOException exception)
            {
                if(exception instanceof SocketException)
                {
                    System.err.println("Server disconnected");
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

    private boolean HandleFilesListOnPrivacy(String userName, String privacy) throws IOException
    {
        Privacy privacyEnum;

        if(privacy.equals(Commands.FilesListArguments.PUBLIC))
        {
            privacyEnum = Privacy.PUBLIC;

        }
        else if(privacy.equals(Commands.FilesListArguments.ALL))
        {
            privacyEnum = Privacy.ALL;
        }
        else if(privacy.equals(Commands.FilesListArguments.PRIVATE))
        {
            privacyEnum = Privacy.PRIVATE;
        }
        else
        {
            System.out.println("Invalid privacy option");

            return false;
        }

        if(privacyEnum == Privacy.PUBLIC)
        {
            objectOutputStream.writeObject(new FilesListRequest(userName, Privacy.PUBLIC));
        }
        else
        {
            if(userName.equals(this.userName))
            {
                objectOutputStream.writeObject(new FilesListRequest(userName, privacyEnum));
            }
            else
            {
                System.out.println("Cannot access private files of other users");

                return false;
            }
        }

        return true;
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
