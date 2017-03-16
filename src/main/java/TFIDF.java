import javax.persistence.*;
import javax.persistence.metamodel.Type;
import java.util.*;

public class TFIDF
{
    public TFIDF(){}

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

        return termFrequencies;
    }

    //Work out TFIDF, update map in database
    public void tfidf(eMailObject email)
    {
        String emailBody = SkeletonClient.retrieveBody(email);

        //Reduce email body to all lower case and alphanumeric
        emailBody = emailBody.toLowerCase();
        emailBody = emailBody.replaceAll("[^A-Za-z0-9 ]", " ");
        emailBody = emailBody.replaceAll("\\s{2,}", " ");
        emailBody = emailBody.replaceAll("^\\s|\\s$", "");

        //TODO: Apply stopword removal to emailBody
        //TODO: Apply Stemming to emailBody

        //Call augmented term frequency method
        HashMap<String, Double> atfs = augmentedTermFrequency(emailBody);

        //Update
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("emailStorage.odb");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        for (Map.Entry<String, Double> entry : atfs.entrySet()) {
            TypedQuery<eMailObject> atfUpdate = em.createQuery("UPDATE eMail SET eMail.atfMap.put(\"" +
                    entry.getKey() + "\"," + entry.getValue() + ") WHERE " + "eMail.message_ID = " + email.getMessage_ID(), eMailObject.class);
        }
        em.getTransaction().commit();

        em.getTransaction().begin();
        for (Map.Entry<String, Double> entry : atfs.entrySet())
        {
            //Calculate TFIDF for each value in the map
            entry.setValue(entry.getValue() * findIDF(entry.getKey()));

            //Create query, to update database - add key, value to TFIDF hashmap
            TypedQuery<eMailObject>updateQuery = em.createQuery("UPDATE eMail " +
                    "SET eMail.tfidfMap.put(\"" + entry.getKey() + "\", " + entry.getValue() + ") WHERE " +
                    "eMail.message_ID = " + email.getMessage_ID(), eMailObject.class);
        }
        em.getTransaction().commit();

        em.close();
        emf.close();
    }

    public static Double findIDF(String term)
    {
        long totalNumberOfEmails = SkeletonClient.countEmails();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("emailStorage.odb");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query countTerm = em.createQuery("SELECT COUNT(eMail) FROM eMailObject email WHERE eMail.atfMap.containsKey(\"" + term + "\")", eMailObject.class);
        em.getTransaction().commit();

        //TODO: Fix!
        //Double idf = Math.log(totalNumberOfEmails / (Long)countTerm.getSingleResult());
        Double idf = Math.log(totalNumberOfEmails / 1.0);

        return idf;
    }
}