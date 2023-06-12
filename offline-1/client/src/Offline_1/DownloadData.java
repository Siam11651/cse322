package Offline_1;

import java.io.Serializable;

public class DownloadData implements Serializable
{
    private boolean lastChunk;
    private boolean ok;
    private byte chunk[];
    private long totalSize;

    public DownloadData(boolean lastChunk, boolean ok, byte chunk[], long totalSize)
    {
        this.lastChunk = lastChunk;
        this.ok = ok;
        this.totalSize = totalSize;
        this.chunk = new byte[chunk.length];

        for(int i = 0; i < chunk.length; ++i)
        {
            this.chunk[i] = chunk[i];
        }
    }

    public byte[] GetChunk()
    {
        return chunk;
    }

    public boolean IsLastChunk()
    {
        return lastChunk;
    }

    public boolean IsOk()
    {
        return ok;
    }

    public long GetTotalSize()
    {
        return totalSize;
    }
}
