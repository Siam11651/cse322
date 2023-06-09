package Offline_1;

import java.io.IOException;

class Main
{
    public static void main(String[] args) throws IOException
    {
        Client.InitialiseClient("localhost", 6666);
        Client.GetClient();
    }
}