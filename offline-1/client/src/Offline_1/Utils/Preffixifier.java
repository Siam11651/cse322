package Offline_1.Utils;

public class Preffixifier
{
    private char prefix;
    private long shortenedValue;

    public Preffixifier(long value)
    {
        prefix = '\0';
        shortenedValue = value;
        char[] prefixArray = {'K', 'M', 'T', 'P', 'E'};

        for(int i = 0; i < prefixArray.length; ++i)
        {
            if(shortenedValue / 1000 > 0)
            {
                shortenedValue /= 1000;
                prefix = prefixArray[i];
            }
            else
            {
                break;
            }
        }
    }

    public char GetPrefix()
    {
        return prefix;
    }

    public long GetShortenedValue()
    {
        return shortenedValue;
    }
}
