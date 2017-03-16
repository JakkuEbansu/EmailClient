import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

        JSONArray map = new JSONArray();

        //Write this to file
        for (Map.Entry<String, Integer> entry : idfMap.entrySet())
        {
            JSONObject mapEntry = new JSONObject();
            mapEntry.put("Key", entry.getKey());
            mapEntry.put("Value", entry.getValue());
            map.add(mapEntry);
        }

        try {
            FileWriter file = new FileWriter("idfMapContents.txt");

            file.write(map.toJSONString());
            file.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    //Method to read IDF values into IDFmap, at startup of program
    public void setup()
    {
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader("idfMapContents.txt"));

            JSONArray arr = (JSONArray) obj;
            Iterator<JSONObject> iter = arr.iterator();

            while (iter.hasNext())
            {
                JSONObject next = iter.next();
                System.out.println(next.toJSONString());
                idfMap.put(next.get("Key").toString(),
                        Integer.parseInt(next.get("Value").toString()));
            }

        }
        catch (Exception ioException)
        {
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