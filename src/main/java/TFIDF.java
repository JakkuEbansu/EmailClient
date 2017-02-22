import javax.persistence.*;
import java.util.*;

public class TFIDF
{
    //Work out term frequency, occurences of term in each email
    public HashMap<String, Double> augmentedTermFrequency(String emailBody)
    {
        //Split email body into separate words, for term frequency calculations
        String[] email = emailBody.split(" ");

        //Confirm unique terms to create TFIDF values for
        HashMap<String, Double> termFrequencies = new HashMap<String, Double>();

        Double maxFrequency = 1.0;

        //For each word in the email, add to the map or add
        for (String word : email)
        {//If in the map, update max frequency and increment value in the map, otherwise just add the new term
            if (termFrequencies.containsKey(word))
            {
                //Update term frequency
                Double newFrequency = termFrequencies.get(word) + 1.0;
                termFrequencies.put(word, newFrequency);

                //Update maximum frequency
                if (newFrequency > maxFrequency) {   maxFrequency = newFrequency; }
            }
            else
            {   termFrequencies.put(word, 1.0); }
        }

        //Update term frequency to augmented term frequency, related to maximum frequency
        for (Map.Entry<String, Double> entry : termFrequencies.entrySet())
        {
            entry.setValue(0.5 + 0.5 * ( entry.getValue() / maxFrequency) );
        }
    }

    public void tfidf(eMailObject email)
    {
        String emailBody = SkeletonClient.retrieveBody(email, mailhost, username, password);

        //Reduce email body to all lower case and alphanumeric
        emailBody = emailBody.toLowerCase();
        emailBody = emailBody.replaceAll("[^A-Za-z0-9]", "");

        //TODO: Apply stopword removal to emailBody

        //TODO: Apply Stemming to emailBody

        //Call augmented term frequency method
        HashMap<String, Double> tfidfs = augmentedTermFrequency(emailBody);

        //Add emailBody contents to IDF statistics
        HashMap<String, Integer> idfMap = new HashMap<String, Integer>();
        IDFFile idfStorage = new IDFFile(idfMap);
        idfStorage.updateIDF(emailBody.split(" "));

        //Update
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/emailStorage.odb");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        for (Map.Entry<String, Double> entry : tfidfs.entrySet())
        {
            //Calculate TFIDF for each value in the map
            entry.setValue(entry.getValue() * idfStorage.idfCalculate(entry.getKey()));

            //Create query, to update database - add key, value to TFIDF hashmap
            TypedQuery<eMailObject>tagQuery = em.createQuery("UPDATE eMail " +
                    "SET eMail.addToMap(" + entry.getKey() + "," + entry.getValue() + ") WHERE " +
                    "eMail.message_ID = " + email.getMessage_ID() + ";", eMailObject.class);
        }

        em.close();
        emf.close();
    }
}