package Offline_1.Threads;

import java.io.File;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.Vector;

import Offline_1.Response.ClientsListResponse;
import Offline_1.Response.ListFilesResponse;
import Offline_1.Response.Response;
import Offline_1.Client;

public class ReadThread extends Thread
{
    final private ObjectInputStream objectInputStream;
    boolean running;

    public ReadThread(ObjectInputStream objectInputStream) throws IOException
    {
        this.objectInputStream = objectInputStream;
        running = true;
    }

    @Override
    public void run()
    {
        super.run();

        while(running)
        {
            try
            {
                Response response = (Response)objectInputStream.readObject();
                Client client = Client.GetClient();

                if(response instanceof ClientsListResponse)
                {
                    System.out.println("List of clients:");

                    Vector<String> clientsList = ((ClientsListResponse)response).GetClientsList();

                    for(int i = 0; i < clientsList.size(); ++i)
                    {
                        System.out.println((i + 1) + ". " + clientsList.get(i));
                    }
                }
                else if(response instanceof ListFilesResponse)
                {
                    ListFilesResponse listFilesResponse = (ListFilesResponse)response;
                    Vector<File> publicFiles = listFilesResponse.GetPublicFiles();
                    Vector<File> privateFiles = listFilesResponse.GetPrivateFiles();

                    if(publicFiles != null)
                    {
                        System.out.println("Listing public files:");

                        for(int i = 0; i < publicFiles.size(); ++i)
                        {
                            System.out.println((i + 1) + ". " + publicFiles.get(i).getName());
                        }
                    }

                    if(privateFiles != null)
                    {
                        System.out.println("Listing private files:");

                        for(int i = 0; i < privateFiles.size(); ++i)
                        {
                            System.out.println((i + 1) + ". " + privateFiles.get(i).getName());
                        }
                    }
                }
                else
                {
                    if(response.GetData().equals("connection-established"))
                    {
                        System.out.println("Connected to server");
                    }
                    else if(response.GetData().equals("login-successfull"))
                    {
                        client.SetLoggedIn(true);
                        System.out.println("Login successful");
                    }
                    else if(response.GetData().equals("login-failed"))
                    {
                        client.Close();
                        System.out.println("Login failed");
                    }
                }
            }
            catch(IOException exception)
            {
                if(exception instanceof EOFException)
                {
                    System.err.println("Diconnected from server");
                    Client.GetClient().Close();
                }
                else if(exception instanceof SocketException)
                {
                    System.err.println("Diconnected from server");
                    Client.GetClient().Close();
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

        System.out.println("Closing read thread");
    }

    public synchronized void Stop()
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
