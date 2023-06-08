package Offline_1;

import java.io.IOException;

public class Main
{    
    public static void main(String[] args) throws IOException
    {
        Server server = Server.GetServer();

        while(true)
        {
            server.Connect();
        }
    }
}
