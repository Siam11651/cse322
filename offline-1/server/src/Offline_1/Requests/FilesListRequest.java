package Offline_1.Requests;

public class FilesListRequest extends Request
{
    public enum Privacy
    {
        PRIVATE, PUBLIC, ALL
    }

    private String userName;
    private Privacy privacy;

    public FilesListRequest(String userName, Privacy privacy)
    {
        this.userName = new String(userName);
        this.privacy = privacy;
    }

    public String GetUserName()
    {
        return userName;
    }

    public Privacy GetPrivacy()
    {
        return privacy;
    }
}
