package Offline_1.UploadBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;;

public class UploadBuffer extends Hashtable<String, UploadFile>
{
    public synchronized int GetBufferSize()
    {
        Iterator<UploadFile> uploadFiles = values().iterator();
        int sum = 0;

        while(uploadFiles.hasNext())
        {
            UploadFile uploadFile = uploadFiles.next();
            sum += uploadFile.GetChunk().length;
        }

        return sum;
    }

    public void Write(String fileId)
    {
        UploadFile uploadFile = get(fileId);
        String filePath = uploadFile.GetFilePath();
        byte[] chunk = uploadFile.GetChunk();
        File file = new File(filePath);

        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            synchronized(chunk)
            {
                fileOutputStream.write(chunk);
            }

            fileOutputStream.close();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
