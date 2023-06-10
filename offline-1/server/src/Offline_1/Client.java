package Offline_1;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import Offline_1.Requests.LoginRequest;
import Offline_1.Requests.Request;
import Offline_1.Requests.UsersListRequest;
import Offline_1.Responses.LoginFailedResponse;
import Offline_1.Responses.LoginSuccessfulResponse;
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
                    boolean loggedIn = false;

                    synchronized(loggedInClients)
                    {
                        for(int i = 0; i < loggedInClients.size(); ++i)
                        {
                            if(loggedInClients.get(i).GetUserName().equals(loginRequest.GetUserName()))
                            {
                                loggedIn = true;

                                break;
                            }
                        }

                        if(loggedIn)
                        {
                            objectOutputStream.writeObject(new LoginFailedResponse());
                        }
                        else
                        {   
                            SetUserName(new String(loginRequest.GetUserName()));
                            loggedInClients.add(this);
                            objectOutputStream.writeObject(new LoginSuccessfulResponse(new String(userName)));
                        }
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
