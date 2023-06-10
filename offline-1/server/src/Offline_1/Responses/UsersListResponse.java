package Offline_1.Responses;

import java.util.Vector;

public class UsersListResponse extends Response
{
    private Vector<String> usersList;

    public UsersListResponse(Vector<String> usersList)
    {
        this.usersList = new Vector<>();

        for(int i = 0; i < usersList.size(); ++i)
        {
            this.usersList.add(new String(usersList.get(i)));
        }
    }

    public Vector<String> GetUsersList()
    {
        return usersList;
    }
}
