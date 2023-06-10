package Offline_1.Responses;

public class UploadRespone extends Response
{
    private long chunkSize;
    private String fileId;

    public UploadRespone(String fileId, long chunkSize)
    {
        this.fileId = new String(fileId);
        this.chunkSize = chunkSize;
    }

    public String GetFileId()
    {
        return fileId;
    }

    public long GetChunkSize()
    {
        return chunkSize;
    }
}
