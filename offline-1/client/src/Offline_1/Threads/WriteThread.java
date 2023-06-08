package Offline_1.Threads;

import java.io.IOException;
import java.io.ObjectOutputStream;

import Offline_1.Client;
import Offline_1.Request.LoginRequest;

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
                    String command = new String(consoleInputBytes);
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