import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class TextGen
{
    private final static boolean WRITEDB = false;
    private final static int LEVEL = 3;
    private static Hashtable mH;

    public static void main(String[] args)
    {

        if (args.length < 2)
        {
            printUsage();
            System.exit(1);
        }

        if (args[0].equalsIgnoreCase("db"))
        {
            try
            {
                FileInputStream istream = new FileInputStream(args[1]);
                ObjectInputStream p = new ObjectInputStream(istream);
                System.out.print("Reading database...");
                mH = (Hashtable) p.readObject();
                istream.close();
                System.out.println("done.");
            }
            catch (IOException ioe)
            {
                System.err.println(ioe);
                System.exit(1);
            }
            catch (ClassNotFoundException cnfe)
            {
                System.err.println(cnfe);
                System.exit(1);
            }
        }
        else if (args[0].equalsIgnoreCase("file"))
        {
            mH = new Hashtable(70000, (float) 0.98);
            readText(args[1]);
            cleanDB();
            if (WRITEDB)
            {
                try
                {
                    FileOutputStream ostream = new FileOutputStream("ord.db");
                    ObjectOutputStream p = new ObjectOutputStream(ostream);
                    p.writeObject(mH);
                    p.flush();
                    ostream.close();
                }
                catch (IOException ioe)
                {
                    System.err.println(ioe);
                }
            }
        }
        else if (args[0].equalsIgnoreCase("File2DB"))
        {
            if (args.length < 3)
            {
                printUsage();
                System.exit(1);
            }
            try
            {
                FileInputStream istream = new FileInputStream(args[2]);
                ObjectInputStream p = new ObjectInputStream(istream);
                System.out.print("Reading database...");
                mH = (Hashtable) p.readObject();
                istream.close();
                System.out.println("done.");
            }
            catch (IOException ioe)
            {
                System.err.println(ioe);
                System.exit(1);
            }
            catch (ClassNotFoundException cnfe)
            {
                System.err.println(cnfe);
                System.exit(1);
            }
            readText(args[1]);
            cleanDB();
            if (WRITEDB)
            {
                try
                {
                    FileOutputStream ostream = new FileOutputStream("ord.db");
                    ObjectOutputStream p = new ObjectOutputStream(ostream);
                    p.writeObject(mH);
                    p.flush();
                    ostream.close();
                }
                catch (IOException ioe)
                {
                    System.err.println(ioe);
                }
            }

        }
        else
        {
            printUsage();
            System.exit(1);
        }

        generateText();

    }

    private static void printUsage()
    {
        System.out.println("Usage: java TextGen <file|db|file2db> <filename> [database file]");
    }


    private static void readText(String infil)
    {
        Runtime runtime = Runtime.getRuntime();

        int wordcount = 0;
        String[] latestWords = new String[LEVEL];
        for (int i = 0; i < LEVEL; i++) latestWords[i] = "";


        BufferedReader bin = null;
        try
        {
            bin = new BufferedReader(new InputStreamReader(new FileInputStream(new File(infil))));

            StringTokenizer strtok;
            String wordstring;
            while (true && mH.size() < 9000)
            {
                String str = bin.readLine();
                if (str == null) break;
                strtok = new StringTokenizer(str);
                while (strtok.hasMoreTokens())
                {
                    wordcount++;
                    String word = strtok.nextToken().trim();
                    shiftWords(latestWords);
                    latestWords[0] = word;
                    for (int level = LEVEL - 1; level >= 0; level--)
                    {
                        wordstring = makeString(latestWords, level);
                        Ordsannolikhet os = (Ordsannolikhet) mH.get(wordstring);
                        if (os == null)
                        {
                            os = new Ordsannolikhet();
                            mH.put(wordstring, os);
                        }
                        os.addWord(word);

                    }

                    //	  System.out.println(word);
                    if ((wordcount % 2500) == 0)
                    {
                        System.out.println(wordcount + " ord. Hashtabellstorlek: " + mH.size() + ". Använt minne: " + (runtime.totalMemory() - runtime.freeMemory()) + " (" + (runtime.totalMemory() - runtime.freeMemory()) / mH.size() + ")");
                    }
                }
                //	System.out.println(str);
            }

            System.out.println("\nh.size() == " + mH.size() + ". " + wordcount + " ord. Använt minne: " + (runtime.totalMemory() - runtime.freeMemory()));

        }
        catch (IOException ioe)
        {
            System.out.println(ioe);
            try
            {
                if (bin != null)
                    bin.close();
            }
            catch (Exception e)
            {
            }
        }
    }


    /* Removes entrys with only one alternative. */
    private static void cleanDB()
    {
        System.out.println("Cleaning database");
        String wordkey;
        Ordsannolikhet os;
        for (Enumeration e = mH.keys(); e.hasMoreElements();)
        {
            wordkey = (String) e.nextElement();
            os = (Ordsannolikhet) mH.get(wordkey);
            if (os.antalUnika() == 1)
            {
                mH.remove(wordkey);
            }
        }
        /*
          System.out.println("Rehashing database");
          mH.rehash();
          */
        System.gc();
        Runtime rt = Runtime.getRuntime();
        System.out.println("\nh.size() == " + mH.size() + ". Minnesanvändning: " + (rt.totalMemory() - rt.freeMemory()));
    }


    private static void generateText()
    {
        System.out.println("Generating text");
        try
        {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("ut.txt")));

            String[] latestWords = new String[LEVEL];
            String wordstring = null;
            int i, count, level;
            Ordsannolikhet os;
            for (i = 0; i < LEVEL; i++) latestWords[i] = "";
            //    latestWords[0] = ordsannolikhet.getRandomWord();
            //    shiftWords(latestWords);

            for (count = 0; count < 2500; count++)
            {
                boolean foundWord = false;
                level = LEVEL - 1;
                while (!foundWord)
                {
                    wordstring = makeString(latestWords, level);
                    if (mH.containsKey(wordstring))
                    {
                        //	    System.out.println(wordstring + " fanns");
                        os = (Ordsannolikhet) mH.get(wordstring);
                        latestWords[0] = os.getRandomWord();
                        foundWord = true;
                    }
                    else
                    {
                        level--;
                    }
                }
                System.out.println(latestWords[0] + "\t\t(" + level + ": '" + wordstring + "')");
                out.print(latestWords[0] + " ");
                shiftWords(latestWords);

            }
            out.close();
        }
        catch (IOException ioe)
        {
            System.out.println(ioe);
        }

    }

    /* Gör en sträng av alla ord utom det första (det vill säga nyaste). */
    private static String makeString(String[] words, int nrOfWords)
    {
        StringBuffer strbuf = new StringBuffer();
        for (int i = nrOfWords; i > 0; i--)
        {
            strbuf.append(words[i]);
            strbuf.append(" ");
        }

        return strbuf.toString().trim();
    }

    private static void shiftWords(String[] words)
    {
        for (int i = words.length - 1; i > 0; i--)
        {
            words[i] = words[i - 1];
        }
    }

}
