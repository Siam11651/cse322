package Offline_1;

import java.io.Serializable;

public class UploadAcknowledge implements Serializable
{
    private boolean ok;

    public UploadAcknowledge(boolean ok)
    {
        this.ok = ok;
    }

    public boolean IsOk()
    {
        return ok;
    }
}
