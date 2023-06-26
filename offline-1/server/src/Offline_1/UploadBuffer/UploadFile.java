package Offline_1.UploadBuffer;

import java.util.Arrays;

public class UploadFile
{
    private String filePath;
    private byte[] chunk;

    public UploadFile(String filePath, byte[] chunk)
    {
        this.filePath = new String(filePath);

        if(chunk != null)
        {
            this.chunk = Arrays.copyOf(chunk, chunk.length);
        }
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
        if(this.chunk == null)
        {
            this.chunk = new byte[0];
        }

        byte[] tempChunk = this.chunk;
        this.chunk = new byte[tempChunk.length + chunk.length];

        for(int i = 0; i < tempChunk.length; ++i)
        {
            this.chunk[i] = tempChunk[i];
        }

        for(int i = 0; i < chunk.length; ++i)
        {
            this.chunk[tempChunk.length + i] = chunk[i];
        }
    }
}
