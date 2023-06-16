package Offline_1.Responses.UsersListResponse;

import java.io.Serializable;

public class UserActivityPair implements Serializable
{
    private String username;
    private boolean active;

    public UserActivityPair(String username, boolean active)
    {
        this.username = new String(username);
        this.active = active;
    }

    public UserActivityPair(UserActivityPair other)
    {
        this.username = new String(other.username);
        this.active = other.active;
    }

    public String GetUsername()
    {
        return username;
    }

    public boolean IsActive()
    {
        return active;
    }
}
