package Offline_1.Responses.UsersListResponse;

import java.util.Vector;

import Offline_1.Responses.Response;

public class UsersListResponse extends Response
{
    private Vector<UserActivityPair> usersList;

    public UsersListResponse(Vector<UserActivityPair> usersList)
    {
        this.usersList = new Vector<>();

        for(int i = 0; i < usersList.size(); ++i)
        {
            this.usersList.add(new UserActivityPair(usersList.get(i)));
        }
    }

    public Vector<UserActivityPair> GetUsersList()
    {
        return usersList;
    }
}
