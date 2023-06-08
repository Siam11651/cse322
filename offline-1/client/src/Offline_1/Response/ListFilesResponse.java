package Offline_1.Response;

import java.util.Vector;
import java.io.File;

public class ListFilesResponse extends Response
{
    final private Vector<File> publicFiles, privateFiles;

    public ListFilesResponse(Vector<File> publicFiles, Vector<File> privateFiles)
    {
        if(publicFiles == null)
        {
            this.publicFiles = null;
        }
        else
        {
            this.publicFiles = new Vector<>();

            for(int i = 0; i < publicFiles.size(); ++i)
            {
                this.publicFiles.add(new File(publicFiles.get(i).getAbsolutePath()));
            }
        }

        if(privateFiles == null)
        {
            this.privateFiles = null;
        }
        else
        {
            this.privateFiles = new Vector<>();

            for(int i = 0; i < privateFiles.size(); ++i)
            {
                this.privateFiles.add(new File(privateFiles.get(i).getAbsolutePath()));
            }
        }
    }

    public Vector<File> GetPublicFiles()
    {
        return publicFiles;
    }

    public Vector<File> GetPrivateFiles()
    {
        return privateFiles;
    }
}
