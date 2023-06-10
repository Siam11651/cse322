package Offline_1.Requests;

import Offline_1.Requests.FilesListRequest.Privacy;

public class UploadRequest extends Request
{
    private long fileSize;
    private String fileName;
    private Privacy privacy;
    private String requestId;

    public UploadRequest(String fileName, long fileSize, Privacy privacy, String requestId)
    {
        this.fileName = new String(fileName);
        this.fileSize = fileSize;
        this.privacy = privacy;
        this.requestId = new String(requestId);
    }

    public String GetFileName()
    {
        return fileName;
    }

    public long GetFileSize()
    {
        return fileSize;
    }

    public Privacy GetPrivacy()
    {
        return privacy;
    }

    public String GetRequestId()
    {
        return requestId;
    }
}
