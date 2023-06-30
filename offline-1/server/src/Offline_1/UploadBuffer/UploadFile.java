package Offline_1.UploadBuffer;

public class UploadFile
{
    private String filePath;
    private byte[] chunk;
    private int complete;

    public UploadFile(String filePath, int fileSize)
    {
        this.filePath = new String(filePath);
        chunk = new byte[fileSize];
        complete = 0;
    }

    public synchronized String GetFilePath()
    {
        return filePath;
    }

    public synchronized byte[] GetChunk()
    {
        return chunk;
    }

    public synchronized void WriteChunk(byte[] chunk)
    {
        for(int i = 0; i < chunk.length; ++i)
        {
            this.chunk[complete + i] = chunk[i];
        }

        complete += chunk.length;
    }
}
