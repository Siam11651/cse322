package Offline_1.Requests;

import Offline_1.Requests.FilesListRequest.Privacy;

public class DownloadRequest extends Request
{
    private String userName;
    private String fileName;
    private Privacy privacy;

    public DownloadRequest(String userName, String fileName, Privacy privacy)
    {
        this.userName = new String(userName);
        this.fileName = new String(fileName);
        this.privacy = privacy;
    }

    public String GetUserName()
    {
        return userName;
    }

    public String GetFileName()
    {
        return fileName;
    }

    public Privacy GetPrivacy()
    {
        return privacy;
    }
}
