import java.util.HashMap;
import java.util.Iterator;
import java.io.Serializable;

public class Ordsannolikhet implements Serializable
{
    private final HashMap mDb;
    private int mAntalOrdFörekomster;
    // int antalUnikaOrd;

    public Ordsannolikhet()
    {
        mDb = new HashMap(2, (float) 1.00);
        mAntalOrdFörekomster = 0;
        // antalUnikaOrd = 0;
    }

    public void addWord(String word)
    {
        word = word.toLowerCase();

        Integer antal = (Integer) mDb.get(word);
        if (antal == null)
        {
            mDb.put(word, new Integer(1));
        }
        else
        {
            Integer newAntal = new Integer(antal.intValue() + 1);
            mDb.put(word, newAntal);
            //  antalUnikaOrd++;
        }

        mAntalOrdFörekomster++;
    }

    public void listContent()
    {
        for (Iterator iterator = mDb.keySet().iterator(); iterator.hasNext();)
        {
            String word = (String)iterator.next();
            Integer antal = (Integer) mDb.get(word);
            System.out.println(word + ": " + antal);
        }
    }

    public String getRandomWord()
    {
        String word;
        int ackValue = 0;
        int treshold = (int) (Math.random() * mAntalOrdFörekomster) + 1;
        for (Iterator iterator = mDb.keySet().iterator(); iterator.hasNext();)
        {
            word = (String) iterator.next();
            ackValue += ((Integer) mDb.get(word)).intValue();
            if (ackValue >= treshold)
            {
                return word;
            }
        }

        return "<<<<INGET ORD FANNS>>>>";
    }

    public int antalUnika()
    {
        return mDb.size();
    }
}
