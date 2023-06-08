package Offline_1.Threads;

import java.io.IOException;
import java.io.ObjectOutputStream;

import Offline_1.Client;
import Offline_1.Request.ListFilesRequest;
import Offline_1.Request.LoginRequest;
import Offline_1.Request.Request;
import Offline_1.Request.ListFilesRequest.RequestType;

public class WriteThread extends Thread
{
    private boolean running;
    final private ObjectOutputStream objectOutputStream;
    // final private Scanner scanner;

    public WriteThread(ObjectOutputStream objectOutputStream)
    {
        this.objectOutputStream = objectOutputStream;
        // scanner = new Scanner(System.in);
        running = true;
    }

    @Override
    public void run()
    {
        super.run();

        while(running)
        {
            int available = 0;

            try
            {
                available = System.in.available();
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }

            if(available > 0)
            {
                byte consoleInputBytes[] = new byte[available];

                try
                {
                    System.in.read(consoleInputBytes);
                    String command = (new String(consoleInputBytes)).trim();
                    String tokenizedCommand[] = command.split(" ");

                    if(tokenizedCommand[0].equals("login"))
                    {
                        Client client = Client.GetClient();

                        if(client.IsLoggedIn())
                        {
                            System.out.println("Already logged in, log out first");
                        }
                        else
                        {
                            String userName = tokenizedCommand[1];

                            objectOutputStream.writeObject(new LoginRequest(userName));
                        }
                    }
                    else if(tokenizedCommand[0].equals("client-list"))
                    {
                        if(Client.GetClient().IsLoggedIn())
                        {
                            objectOutputStream.writeObject(new Request("client-list"));
                        }
                        else
                        {
                            System.out.println("You need to login to get access to this information");
                        }
                    }
                    else if(tokenizedCommand[0].equals("logout"))
                    {
                        objectOutputStream.writeObject(new Request("logout-request"));
                        Client.GetClient().Close();
                    }
                    else if(tokenizedCommand[0].equals("list-files"))
                    {
                        Client client = Client.GetClient();

                        if(client.IsLoggedIn())
                        {
                            if(tokenizedCommand.length == 1 || tokenizedCommand[1].equals("all"))
                            {
                                objectOutputStream.writeObject(new ListFilesRequest(client.GetUserName(), RequestType.ALL));
                            }
                            else
                            {   
                                if(tokenizedCommand[1].equals("public"))
                                {
                                    objectOutputStream.writeObject(new ListFilesRequest(client.GetUserName(), RequestType.PUBLIC));
                                }
                                else if(tokenizedCommand[1].equals("private"))
                                {
                                    objectOutputStream.writeObject(new ListFilesRequest(client.GetUserName(), RequestType.PRIVATE));
                                }
                                else
                                {
                                    // exception
                                }
                            }
                        }
                    }
                }
                catch(IOException exception)
                {
                    exception.printStackTrace();
                }
            }
        }

        System.out.println("Closing write thread");
    }

    public synchronized void Stop()
    {
        try
        {
            objectOutputStream.close();
            System.in.close();

            running = false;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}