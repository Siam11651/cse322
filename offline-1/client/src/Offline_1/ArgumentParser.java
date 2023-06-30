package Offline_1;

import java.util.Vector;

public class ArgumentParser
{
    Vector<String> tokens;

    public ArgumentParser(String input)
    {
        tokens = new Vector<>();
        StringBuffer stringBuffer = new StringBuffer();
        boolean inQuote = false;

        for(int i = 0; i < input.length(); ++i)
        {
            char c = input.charAt(i);

            if(c == '\"')
            {
                inQuote = !inQuote;
            }
            else if(c == ' ')
            {
                if(!inQuote)
                {
                    if(stringBuffer.length() > 0)
                    {
                        tokens.add(stringBuffer.toString());

                        stringBuffer = new StringBuffer();
                    }
                }
                else
                {
                    stringBuffer.append(c);
                }
            }
            else
            {
                stringBuffer.append(c);
            }
        }

        if(stringBuffer.length() > 0)
        {
            tokens.add(stringBuffer.toString());
        }
    }

    String[] GetTokens()
    {
        String[] tokensArray = new String[tokens.size()];

        tokens.toArray(tokensArray);

        return tokensArray;
    }
}