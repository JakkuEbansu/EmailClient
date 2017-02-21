import javax.mail.*;
import java.io.*;
import javax.mail.search.MessageIDTerm;
import javax.persistence.*;
import java.util.*;

public class SkeletonClient
{
    public static void main(String [] args)
    {
        //Read from secure file - username, mailhost, password
        String mailHost = readFileContents("secure.txt", 0);
        String userName = readFileContents("secure.txt", 1);
        String password = readFileContents("secure.txt", 2);

        //Retrieve email stores emails in array - update to remote server later
        retrieveEmail(mailHost, userName, password);

        //Call GUI
        ClientGUI.setup();
    }

    //Allows reading of particular line in a data file
    public static String readFileContents(String fileName, int dataLine)
    {
        try {
            //Set filebuffer to read desired file filename
            FileReader fr = new FileReader(fileName);
            BufferedReader bf = new BufferedReader(fr);

            int currentLine = 0;

            //Loop forward to desired line in file
            while (currentLine < dataLine) {
                currentLine++;
                bf.readLine();
            }

            return bf.readLine();
            //Returns value of outlined line
        }
        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return "-1";
    }

    //Allows writing to particular line in a data file
    public static int writeFileContents(String fileName, int line, String valueToWrite)
    {
        //Write to specific line of data text file, outlining desired value
        try {
            FileWriter fw = new FileWriter("tempWrite.txt");
            BufferedWriter bw = new BufferedWriter(fw);

            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            String currentLine;
            int lineBeingRead = 0;

            //Read line, up until desired line and afterwards, copying and pasting to new temp file
            //When desired line to change is found, new data is changed for old
            while ((currentLine = br.readLine()) != null)
            {
                if(line == lineBeingRead)
                {
                    currentLine = valueToWrite;
                }
                bw.write(currentLine, 0, currentLine.length());

                lineBeingRead++;
            }

            File desiredFile = new File(fileName);
            desiredFile.delete();
            new File("tempWrite.txt").renameTo(desiredFile);

            return 0;
        }

        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return -1;
    }

    /*Enables checking of emails from an IMAP server*/
    public static int retrieveEmail(String mailHost, String username, String password)
    {
        int updated_date_line = 0;

        try
        {
            //ReadFileValue to find last updated date/time
            //Only read in emails since last updated date
            int last_updated = Integer.parseInt(readFileContents("data.txt", updated_date_line));

            Properties sessionProperties = new Properties();

            sessionProperties.put("mail.store.protocol", "imaps");

            Session mailSession = Session.getInstance(sessionProperties);
            Store mailStore = mailSession.getStore();
            mailStore.connect(mailHost, username, password);

            /*Retrieves just data from folder inbox for now*/
            Folder inbox = mailStore.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            /*Creating email array of email objects, length of email inbox size*/
            //TODO: Change Array Size, this is un-necessarily large
            eMailObject[] emailArray = new eMailObject[inbox.getMessageCount()];

            for (int i = inbox.getMessageCount(); i >= 1; i--)
            {
                Message email = inbox.getMessage(i);

                //End loop if the email has already been read before, based on the sending date of said email and the
                //last updated date
                if (email.getSentDate().getTime() <= last_updated) { break; }

                //Convert senders + recipients to list, prior to object storage
                int senderCount = 0;
                List<Address> listSenders = new ArrayList<Address>();
                while (senderCount < email.getFrom().length)
                {
                    listSenders.add(email.getFrom()[senderCount]);
                    senderCount++;
                }

                int recipientCount = 0;
                List<Address> listRecipients = new ArrayList<Address>();
                while (recipientCount < email.getAllRecipients().length)
                {
                    listRecipients.add(email.getAllRecipients()[recipientCount]);
                    recipientCount++;
                }

                //Add to array of objects for now, prior to creating database
                //Adds email unique identifier, and leaves current tags null for now
                emailArray[i] = new eMailObject(listSenders, listRecipients, email.getSentDate(),
                        email.getSubject(), null, email.getMessageNumber(),
                        null);
            }

            storeToDatabase(emailArray);
        }
        catch (Exception mailEx) {
            mailEx.printStackTrace();
        }

        Date currentDate = new Date();

        //Write to file, updating last updated date/time
        //Use getTime from Date, aka amount of milliseconds since 1970 - cast to int
        //Write to data file, on line UDL
        writeFileContents("data.txt", updated_date_line, (int) currentDate.getTime());

        return 0;
    }

    //Need to store emails to database (uses ObjectDB)
    public static int storeToDatabase(eMailObject[] emailsToStore)
    {
        //Management of entities, prior to storage - essentially, creating a transaction
        //to push to the database
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("$objectdb/db/emailStorage.odb");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        //Loop through supplied array of emails, add to EntityManager
        for (eMailObject e : emailsToStore)
        {
            em.persist(e);
        }

        //Push to database
        em.getTransaction().commit();

        //Close connections to database
        em.close();
        emf.close();

        return 0;
    }

    //Update tags in database, based on search-satisfying results
    public static int updateTags(List<eMailObject> emailsToUpdate, String tagToAdd)
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/emailStorage.odb");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        TypedQuery<eMailObject> tagQuery;

        for (eMailObject email : emailsToUpdate)
        {
            email.addTag(tagToAdd);

            tagQuery = em.createQuery("UPDATE eMail " +
                    "SET eMail.addTag(" + tagToAdd + ") WHERE " +
                    "eMail.message_ID = " + email.getMessage_ID() + ";", eMailObject.class);

            if (!tagQuery.getResultList().isEmpty())
            {
                return 0;
            }
        }
        return -1;
    }

    //Retrieve emails from database, dependant on query
    public static eMailObject[] searchQuery(String[] providedQuery, String tagName)
    {
        //Set up connection to database
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("$objectdb/db/emailStorage.odb");
        EntityManager em = emf.createEntityManager();

        TypedQuery<eMailObject> tagQuery;

        int counter = 0;

        String completeQuery = "SELECT eMail FROM eMailObject email WHERE ";

        while (counter < providedQuery.length)
        {
            //If there is a logical operator, concatenate to the SQL query and add a space
            if (providedQuery[counter].equalsIgnoreCase("AND") || providedQuery[counter].equalsIgnoreCase("OR") ||
                    providedQuery[counter].equalsIgnoreCase("NOT"))
            {
                completeQuery = completeQuery.concat(providedQuery[counter].toUpperCase() + " ");
                counter++;
            }

            //Contains - check if email contains the search term
            else if (providedQuery[counter].equalsIgnoreCase("Contains"))
            {
                //WHERE email body contains search term
                completeQuery = completeQuery.concat("eMail.getBody() LIKE %" + providedQuery[counter + 1] + "% ");

                //OR
                completeQuery = completeQuery.concat("OR ");

                //Email subject contains search term
                completeQuery = completeQuery.concat("eMail.getSubject() LIKE %" + providedQuery[counter + 1] + "% ");

                //Adds two to counter, skipping past search-term
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Sender "))
            {
                //WHERE email sender contains search term
                completeQuery = completeQuery.concat("eMail.getSenders() LIKE %" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Date-Match "))
            {
                //WHERE email sent date matches query
                completeQuery = completeQuery.concat("eMail.getSentDate() LIKE %" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Date-Range "))
            {
                //WHERE email sent date fits range of query
                //completeQuery = completeQuery.concat("eMail.sentDate.getTime()")
            }

            else if (providedQuery[counter].equalsIgnoreCase("Recipients "))
            {
                //WHERE email recipients contain particular String
                completeQuery = completeQuery.concat("eMail.getRecipients() LIKE %" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Tag "))
            {
                //WHERE email is already tagged with another tag
                completeQuery = completeQuery.concat("eMail.getTags() LIKE %" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }

            else if (providedQuery[counter].equalsIgnoreCase("Message_ID "))
            {
                //WHERE email matches message ID - probably just used for behind the scenes search
                completeQuery = completeQuery.concat("eMail.getMessage_ID()" + providedQuery[counter + 1] + "% ");
                counter = counter + 2;
            }

            else
            {
                //In theory, this should never be called, but I need an 'else' condition here regardless
                counter++;
            }

            //TODO: Alter previous code to reflect the fact that SQL dates are not the same format as Java's
        }

        completeQuery = completeQuery + ";";
        tagQuery = em.createQuery(completeQuery, eMailObject.class);

        //Return results as List
        List<eMailObject> results = tagQuery.getResultList();

        //Updates tags for emails, unless tag is null
        if (!tagName.equals(" "))
        {
            updateTags(results, tagName);
        }

        //Convert list into array of eMailObjects
        eMailObject[] resultsAsArray = new eMailObject[results.size()];
        results.toArray(resultsAsArray);
        return resultsAsArray;
    }

    //Return body contents from mail server
    public static String retrieveBody(eMailObject email, String mailHost, String username, String password)
    {
        try {
            //Create mail session to poll mail server
            Properties sessionProperties = new Properties();
            sessionProperties.put("mail.store.protocol", "imaps");
            Session mailSession = Session.getInstance(sessionProperties);
            Store mailStore = mailSession.getStore();
            mailStore.connect(mailHost, username, password);

            /*Retrieves just data from folder inbox for now*/
            Folder inbox = mailStore.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            //Search for emails that match message ID
            MessageIDTerm searchID = new MessageIDTerm("" + email.getMessage_ID());

            Message[] results = inbox.search(searchID);
            return results[0].getContent().toString();
        }
        catch (Exception mailEx) {
            mailEx.printStackTrace();}
    }

    //TODO : Look into how actually TypedQuery works
    public static int countEmails()
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/emailStorage.odb");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        TypedQuery<eMailObject> countQuery;

        countQuery = em.createQuery("SELECT COUNT(eMail) FROM eMailObject", eMailObject.class);
        return Integer.parseInt(countQuery.getSingleResult().toString());
    }
}