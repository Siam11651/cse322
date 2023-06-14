package Offline_1.Responses;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

import Offline_1.Exception.ReadTimeoutException;

public class LoginResponse extends Response
{
    private String userName;

    public LoginResponse(String userName)
    {
        this.userName = new String(userName);
    }

    public String GetUserName()
    {
        return userName;
    }

    public boolean IsSuccessful()
    {
        return userName.length() > 0;
    }
}
