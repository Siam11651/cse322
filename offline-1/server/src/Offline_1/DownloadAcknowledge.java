package Offline_1;

import java.io.Serializable;

public class DownloadAcknowledge implements Serializable
{
    boolean ok;

    public DownloadAcknowledge(boolean ok)
    {
        this.ok = ok;
    }

    public boolean IsOK()
    {
        return ok;
    }
}
