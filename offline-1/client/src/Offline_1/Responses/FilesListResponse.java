package Offline_1.Responses;

import java.io.File;
import java.util.Vector;

public class FilesListResponse extends Response
{
    private Vector<String> privateFilesList, publicFilesList;

    public FilesListResponse(Vector<File> privateFilesList, Vector<File> publicFilesList)
    {
        if(privateFilesList != null)
        {
            this.privateFilesList = new Vector<>();

            for(int i = 0; i < privateFilesList.size(); ++i)
            {
                this.privateFilesList.add(new String(privateFilesList.get(i).getName()));
            }
        }
        else
        {
            this.privateFilesList = null;
        }

        if(publicFilesList != null)
        {
            this.publicFilesList = new Vector<>();

            for(int i = 0; i < publicFilesList.size(); ++i)
            {
                this.publicFilesList.add(new String(publicFilesList.get(i).getName()));
            }
        }
        else
        {
            this.publicFilesList = null;
        }
    }

    public Vector<String> GetPrivateFilesList()
    {
        return privateFilesList;
    }

    public Vector<String> GetPublicFilesList()
    {
        return publicFilesList;
    }
}
