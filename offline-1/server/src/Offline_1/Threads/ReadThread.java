package Offline_1.Threads;

import java.io.File;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.Cleaner.Cleanable;
import java.net.SocketAddress;
import java.util.Vector;

import Offline_1.Request.ListFilesRequest;
import Offline_1.Request.LoginRequest;
import Offline_1.Request.Request;
import Offline_1.Request.ListFilesRequest.RequestType;
import Offline_1.Response.Response;
import Offline_1.Response.ClientsListResponse;
import Offline_1.Response.ListFilesResponse;
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
                else if(request instanceof ListFilesRequest)
                {
                    ListFilesRequest listFilesRequest = (ListFilesRequest)request;
                    String userName = listFilesRequest.GetUserName();
                    Client targetClient = null;
                    Vector<Client> clients = Server.GetServer().GetClients();

                    for(int i = 0; i < clients.size(); ++i)
                    {
                        if(clients.get(i).GetUserName().equals(userName))
                        {
                            targetClient = clients.get(i);

                            break;
                        }
                    }

                    Vector<File> publicFiles = null;
                    Vector<File> privateFiles = null;

                    if(targetClient != null)
                    {
                        if(listFilesRequest.GetRequestType() == RequestType.ALL)
                        {
                            publicFiles = client.GetPublicFiles();

                            if(targetClient.GetUserName().equals(client.GetUserName()))
                            {
                                privateFiles = client.GetPrivateFiles();
                            }
                        }
                        else if(listFilesRequest.GetRequestType() == RequestType.PUBLIC)
                        {
                            publicFiles = client.GetPublicFiles();
                        }
                        else
                        {
                            if(targetClient.GetUserName().equals(client.GetUserName()))
                            {
                                privateFiles = client.GetPrivateFiles();
                            }
                        }
                    }

                    client.SendResponse(new ListFilesResponse(publicFiles, privateFiles));
                }
                else
                {
                    if(request.GetData().equals("logout-request"))
                    {
                        String userName = client.GetUserName();
                        SocketAddress remoteSocketAddress = client.GetSocket().getRemoteSocketAddress();

                        System.out.println("User: " + userName + "; Remote socket address: " + remoteSocketAddress + " logging out");
                        client.Close();
                    }
                    else if(request.GetData().equals("client-list"))
                    {
                        Server server = Server.GetServer();
                        Vector<Client> clients = server.GetClients();
                        Vector<String> clientUserNames = new Vector<>();

                        for(int i = 0; i < clients.size(); ++i)
                        {
                            clientUserNames.add(clients.get(i).GetUserName());
                        }

                        client.SendResponse(new ClientsListResponse(clientUserNames));
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
