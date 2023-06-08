package Offline_1.Threads;

import java.io.IOException;
import java.io.ObjectOutputStream;

import Offline_1.Response.Response;
import Offline_1.Response.ResponsePoller;

public class WriteThread extends Thread
{
    final ObjectOutputStream objectOutputStream;
    final ResponsePoller responsePoller;
    private boolean running;

    public WriteThread(ObjectOutputStream objectOutputStream)
    {
        this.objectOutputStream = objectOutputStream;
        responsePoller = new ResponsePoller();
        running = true;
    }

    public ResponsePoller GetResponsePoller()
    {
        return responsePoller;
    }

    public void run()
    {
        while(running)
        {
            Response response = responsePoller.GetRespone();

            // System.out.println(response);

            if(response != null)
            {
                try
                {
                    objectOutputStream.writeObject(response);
                    responsePoller.EndResponse();
                }
                catch(IOException exception)
                {
                    System.err.println(exception.getMessage());
                }
            }
        }
    }

    public void Stop()
    {
        try
        {
            objectOutputStream.close();

            running = false;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
