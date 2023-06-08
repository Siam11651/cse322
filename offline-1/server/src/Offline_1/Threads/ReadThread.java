package Offline_1.Threads;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

import Offline_1.Request.LoginRequest;
import Offline_1.Request.Request;
import Offline_1.Response.Response;
import Offline_1.Client;
import Offline_1.Server;

public class ReadThread extends Thread
{
    final private ObjectInputStream objectInputStream;
    private Client client;
    private boolean running;

    public ReadThread(Client client, ObjectInputStream objectInputStream)
    {
        this.objectInputStream = objectInputStream;
        this.client = client;
        running = true;
    }

    public void run()
    {
        while(running)
        {
            try
            {
                Request request = (Request)objectInputStream.readObject();

                if(request instanceof LoginRequest)
                {
                    System.out.println("Login request from " + client.GetSocket().getRemoteSocketAddress());

                    LoginRequest loginRequest = (LoginRequest)request;

                    if(Server.GetServer().LoginClient(loginRequest.GetData(), client))
                    {
                        client.SendResponse(new Response("login-successfull"));
                    }
                    else
                    {
                        client.SendResponse(new Response("login-failed"));
                        client.Close();
                    }
                }
                else
                {
                    if(request.GetData().equals("logout-request"))
                    {
                        client.Close();
                    }
                }
            }
            catch(IOException exception)
            {
                if(exception instanceof EOFException)
                {
                    client.Close();
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

    public void Stop()
    {
        try
        {
            objectInputStream.close();

            running = false;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
