package Offline_1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Responses.LoginSuccessfulResponse;
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

                    if(tokenizedCommand[0].equals(Commands.Login.TEXT))
                    {
                        if(GetUserName().length() == 0)
                        {
                            if(tokenizedCommand.length > 1)
                            {
                                objectOutputStream.writeObject(new LoginRequest(tokenizedCommand[1]));

                                waitForResponse = true;
                            }
                            else
                            {
                                System.err.println(Commands.Login.TEXT + " command needs an argument");
                            }
                        }
                        else
                        {
                            System.out.println("Already logged in as " + GetUserName());
                        }
                    }
                    else if(tokenizedCommand[0].equals(Commands.UsersList.TEXT))
                    {
                        if(GetUserName().length() > 0)
                        {
                            objectOutputStream.writeObject(new UsersListRequest());

                            waitForResponse = true;
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

                    if(response instanceof LoginSuccessfulResponse)
                    {
                        LoginSuccessfulResponse loginSuccessfulResponse = (LoginSuccessfulResponse)response;

                        SetUserName(loginSuccessfulResponse.GetUserName());
                        System.out.println("Logged in as "+ loginSuccessfulResponse.GetUserName());
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

    public synchronized void SetUserName(String userName)
    {
        this.userName = userName;
    }

    public synchronized String GetUserName()
    {
        return userName;
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
