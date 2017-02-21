import java.util.*;

public class TFIDF
{
    public void augmentedTermFrequency(String emailBody)
    {
        //Reduce email body to all lower case and alphanumeric
        emailBody = emailBody.toLowerCase();
        emailBody = emailBody.replaceAll("[^A-Za-z0-9]", "");

        //Split email body into separate words, for term frequency calculations
        String[] email = emailBody.split(" ");

        //Confirm unique terms to create TFIDF values for
        HashMap<String, Double> termFrequencies = new HashMap<String, Double>();

        Double maxFrequency = 1.0;

        //For each word in the email, add to the map or add
        for (String word : email)
        {
            if (termFrequencies.containsKey(word))
            {
                //Update term frequency
                Double newFrequency = termFrequencies.get(word) + 1.0;
                termFrequencies.put(word, newFrequency);

                //Update maximum frequency
                if (newFrequency > maxFrequency)
                {
                    maxFrequency = newFrequency;
                }
            }
            else
            {
                termFrequencies.put(word, 1.0);
            }
        }

        //Update term frequency to augmented term frequency, related to maximum frequency
        for (Map.Entry<String, Double> entry : termFrequencies.entrySet())
        {
            entry.setValue(0.5 + 0.5 * ( entry.getValue() / maxFrequency) );
        }
    }

    public void inverseDocumentFrequency
}
