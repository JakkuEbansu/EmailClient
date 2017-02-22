import java.io.*;
import java.util.*;

public class IDFFile
{
    private Map<String, Integer> idfMap;

    public IDFFile(Map<String, Integer> idfMap){
        this.idfMap = idfMap;
        setup();
    }

    //Method to write values to a file
    public void updateIDF(String[] emailBody)
    {
        Set<String> uniqueTerms = new HashSet<String>();

        //TODO : May need to add stopword/alphanumeric/etc. features, throw into their own method :3
        //Loop through email to find only unique terms
        for (String term : emailBody)
        {  if (!uniqueTerms.contains(term))
            { uniqueTerms.add(term); }  }

        //Loop through unique terms, adding to map
        for (String term : uniqueTerms)
        {
            if (idfMap.containsKey(term))
            { idfMap.put(term, idfMap.get(term) + 1); }
            else
            { idfMap.put(term, 1); }
        }

        int lineCounter = 1;

        //Write this to file
        for (Map.Entry<String, Integer> entry : idfMap.entrySet())
        {
            FileOperations.writeFileContents("idfMapContents.txt", lineCounter, entry.getKey() + " " + entry.getValue());
            lineCounter++;
        }
    }

    //Method to read IDF values into IDFmap, at startup of program
    public void setup()
    {
        try {
            //Set filebuffer to read desired file filename
            FileReader fr = new FileReader("idfMapContents.txt");
            BufferedReader bf = new BufferedReader(fr);

            String currentLine;

            //Loop through file, adding key/value pairs to map
            while ((currentLine = bf.readLine()) != null) {
                String[] lineContents = currentLine.split(" ");
                idfMap.put(lineContents[0], Integer.parseInt(lineContents[1]));
            }
        }
        catch (Exception ioException){
            ioException.printStackTrace();
        }
    }

    //IDF for each term is log ( number of documents / number of documents this term occurs in )
    public Double idfCalculate(String term)
    {
        int termDocIndex = 0;

        //Get number of documents this term occurs in
        if (idfMap.get(term) != null)
        { termDocIndex = idfMap.get(term); }

        return Math.log(SkeletonClient.countEmails() / termDocIndex);
    }
}