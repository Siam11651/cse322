package Offline_1;

public class UploadData
{
    private String fileId;
    private byte[] chunk;

    public UploadData(String fileId, byte chunk[])
    {
        this.fileId = new String(fileId);
        this.chunk = new byte[chunk.length];

        for(int i = 0; i < chunk.length; ++i)
        {
            this.chunk[i] = chunk[i];
        }
    }

    public String GetFileId()
    {
        return fileId;
    }

    public byte[] GetChunk()
    {
        return chunk;
    }
}
