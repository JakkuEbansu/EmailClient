import javax.jdo.JDOHelper;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.*;
import javax.persistence.*;
import java.io.File;
import java.text.*;
import java.util.*;

public class SkeletonClient
{
    public static void main(String [] args)
    {
        //On first boot: (Does secure file exist..?)
        File ifMailDataExists = new File("mailData.txt");

        if (!ifMailDataExists.exists())
        {
            //If the program has not run before, create server data file - number of servers added = 0
            FileOperations.writeFileContents("mailData.txt", 1, "0");
        }

        //Retrieve emails from IMAP, store to database
        updateEmails();
        updateTFIDF();

        //Call GUI setup
        ClientGUI gui = new ClientGUI();
    }

    //Retrieves emails from IMAP mailstore
    static void updateEmails()
    {
        int numberOfServers = Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1));

        ImapThread[] threads = new ImapThread[numberOfServers];

        for (int i = 0; i < numberOfServers; i++)
        {
            threads[i] = new ImapThread(i);
            threads[i].start();
        }
    }

    //Updates TFIDF scores for emails, allowing easy search
    //TODO: Surely this means emails with earlier added TFIDFs have worse information? Look into! Might well require frequent updates.
    public static void updateTFIDF()
    {
        TFIDFThread tfidfThread = new TFIDFThread();
        tfidfThread.start();
    }

    /*Enables checking of emails from an IMAP server*/
    static void retrieveEmail(String mailHost, String username, String password, int currentServer)
    {
        try
        {
            //ReadFileValue to find last updated date/time
            //Only read in emails since last updated date

                //long last_updated = Long.parseLong(FileOperations.retrieveCredentials("updatedDate", currentServer));

                Properties sessionProperties = new Properties();

                sessionProperties.put("mail.store.protocol", "imaps");
                Session mailSession = Session.getInstance(sessionProperties);
                Store mailStore = mailSession.getStore();
                mailStore.connect(mailHost, username, password);

                /*Retrieves just data from folder inbox for now*/
                Folder inbox = mailStore.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                /*Creating email array of email objects, length of email inbox size*/
                //TODO: Change Array Size, this is un-necessarily large- should be as large as
                // the number of emails we receive..?
                eMailObject[] emailArray = new eMailObject[inbox.getMessageCount()];

                for (int i = inbox.getMessageCount(); i >= 1; i--) {
                    Message email = inbox.getMessage(i);

                    //TODO : Fix!
                    //End loop if the email has already been read before, based on the sending date of said email and the
                    //last updated date
                    //if (email.getSentDate().getTime() <= last_updated) {
                    //    break;
                    //}

                    //Convert senders + recipients to list, prior to object storage
                    int senderCount = 0;
                    List<String> listSenders = new ArrayList<String>();
                    while (senderCount < email.getFrom().length) {
                        listSenders.add(email.getFrom()[senderCount].toString());
                        senderCount++;
                    }

                    int recipientCount = 0;
                    List<String> listRecipients = new ArrayList<String>();
                    while (recipientCount < email.getAllRecipients().length) {
                        listRecipients.add(email.getAllRecipients()[recipientCount].toString());
                        recipientCount++;
                    }

                    //Add to array of objects for now, prior to creating database
                    //Adds email unique identifier, and leaves current tags null for now
                    emailArray[i - 1] = new eMailObject(listSenders, listRecipients, email.getSentDate(), email.getReceivedDate(),
                            email.getSubject(), email.getMessageNumber(), currentServer);
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
        FileOperations.storeCredentials("updatedDate", currentDate.getTime() + "", currentServer);
    }

    //Need to store emails to database (uses ObjectDB)
    private static int storeToDatabase(eMailObject[] emailsToStore)
    {
        //Management of entities, prior to storage - essentially, creating a transaction
        //to push to the database
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("emailStorage.odb");
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
    static int updateTags(List<eMailObject> emailsToUpdate, String tagToAdd)
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("emailStorage.odb");
        EntityManager em = emf.createEntityManager();

        //Query tagQuery;

        for (eMailObject email : emailsToUpdate)
        {
            //tagQuery = em.createQuery("UPDATE eMail " +
                   // "SET eMail.tags.add(\"" + tagToAdd + "\") WHERE " +
                    //"eMail.message_ID = " + email.getMessage_ID(), eMailObject.class);
            eMailObject emailToFind = em.find(eMailObject.class, email.getMessage_ID());

            em.getTransaction().begin();
            emailToFind.getTags().add(tagToAdd);
            JDOHelper.makeDirty(emailToFind, "tags");
            em.getTransaction().commit();

            TypedQuery<eMailObject> outputQuery = em.createQuery("SELECT eMail FROM eMailObject email WHERE eMail.message_ID = " + email.getMessage_ID(), eMailObject.class);
        }
        return 0;
    }

    //Retrieve emails from database, dependant on query
    static eMailObject[] searchQuery(String[] providedQuery, String tagName)
    {
        System.out.println(Arrays.toString(providedQuery));
        //Set up connection to database
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("emailStorage.odb");
        EntityManager em = emf.createEntityManager();

        TypedQuery<eMailObject> tagQuery;
        int counter = 0;

        String completeQuery;
        String order = "";

        if (providedQuery[0].equals(""))
        {
            completeQuery = "SELECT eMail FROM eMailObject email";
        }
        else {
            completeQuery = "SELECT eMail FROM eMailObject email WHERE ";

            while (counter < providedQuery.length) {
                //If there is a logical operator, concatenate to the SQL query and add a space
                if (providedQuery[counter].equalsIgnoreCase("AND") || providedQuery[counter].equalsIgnoreCase("OR") ||
                        providedQuery[counter].equalsIgnoreCase("NOT")) {
                    completeQuery = completeQuery.concat(providedQuery[counter].toUpperCase() + " ");
                    counter++;
                }

                //Contains - check if email contains the search term
                else if (providedQuery[counter].equalsIgnoreCase("Contains")) {

                    //WHERE email body contains search term
                    completeQuery = completeQuery.concat("eMail.getTfidfMap().get(\"" + providedQuery[counter + 1] + "\") IS NOT NULL ");

                    //OR
                    completeQuery = completeQuery.concat("OR ");

                    //Email subject contains search term
                    completeQuery = completeQuery.concat("eMail.getSubject() LIKE '%" + providedQuery[counter + 1] + "%' ");

                    //Organise query by TFIDF values - orders may stack, if more than one is applied
                    if (order.equals("")) {
                        order = "ORDER BY eMail.getTfidfMap().get(\"" + providedQuery[counter + 1] + "\") ";
                    }
                    else {
                        order = order.concat(", eMail.getTfidfMap().get(\"" + providedQuery[counter + 1] + "\") ");
                    }

                    //Adds two to counter, skipping past search-term
                    counter = counter + 2;

                } else if (providedQuery[counter].equalsIgnoreCase("Sender ")) {
                    //WHERE email sender contains search term
                    completeQuery = completeQuery.concat(" '" + providedQuery[counter + 1] + "' MEMBER OF eMail.getSenders()");
                    counter = counter + 2;

                } else if (providedQuery[counter].equalsIgnoreCase("Sent-Date ")) {
                    //WHERE email sent date fits query's
                    completeQuery = completeQuery.concat("eMail.sentDate.toString() LIKE \"%" + providedQuery[counter + 1] + "%\" ");
                    counter = counter + 2;

                } else if (providedQuery[counter].equalsIgnoreCase("Received-Date ")) {
                    //WHERE email received date fits query's
                    completeQuery = completeQuery.concat("eMail.receivedDate.toString() LIKE \"%" + providedQuery[counter + 1] + "%\" ");
                    counter = counter + 2;

                } else if (providedQuery[counter].equalsIgnoreCase("Date-Range ")) {
                    //WHERE email is within a range of two dates
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                    //TODO: Handle exceptions here
                    try {
                        Date date1 = df.parse(providedQuery[counter + 1]);
                        Date date2 = df.parse(providedQuery[counter + 2]);

                        completeQuery = completeQuery.concat("eMail.receivedDate >= "
                                + date1 + " AND email.receivedDate <= " + date2 + " ");
                        counter = counter + 3;
                    }
                    catch(ParseException pex)
                    {
                        pex.printStackTrace();
                    }
                } else if (providedQuery[counter].equalsIgnoreCase("Recipients ")) {
                    //WHERE email recipients contain particular String
                    completeQuery = completeQuery.concat(" '" + providedQuery[counter + 1] + "' MEMBER OF eMail.getRecipients()");
                    counter = counter + 2;

                } else if (providedQuery[counter].equalsIgnoreCase("Tag ")) {
                    //WHERE email is already tagged with another tag
                    completeQuery = completeQuery.concat(" '" + providedQuery[counter + 1] + "' MEMBER OF eMail.tags");
                    counter = counter + 2;

                } else if (providedQuery[counter].equalsIgnoreCase("Message_ID ")) {
                    //WHERE email matches message ID - probably just used for behind the scenes search
                    completeQuery = completeQuery.concat("eMail.getMessage_ID() = " + providedQuery[counter + 1] + " ");
                    counter = counter + 2;
                }
            }
        }

        completeQuery = completeQuery.concat(" " + order);
        System.out.println(completeQuery);

        tagQuery = em.createQuery(completeQuery, eMailObject.class);

        //Return results as List
        List<eMailObject> results = tagQuery.getResultList();

        //Updates tags for emails, unless tag is empty
        if (!tagName.equals(" ")) {   updateTags(results, tagName);   }

        //Convert list into array of eMailObjects
        eMailObject[] resultsAsArray = new eMailObject[results.size()];
        results.toArray(resultsAsArray);

        return resultsAsArray;
    }

    //Return body contents from mail server - only used for TFIDF, not stored
    static String retrieveBody(eMailObject email)
    {
        try {
            String mailHost = FileOperations.retrieveCredentials("mailHost", email.getMailServer());
            String username = FileOperations.retrieveCredentials("userName", email.getMailServer());
            String password = FileOperations.retrieveCredentials("password", email.getMailServer());
            final eMailObject input = email;

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
            SearchTerm searchID = new SearchTerm(){
                @Override
                //Borrowed re-creating a search criterion from
                //http://www.codejava.net/java-ee/javamail/using-javamail-for-searching-e-mail-messages
                //as BOTH Javamail's and GmailMSGIdNum's I could not get to play ball
                //Actually altered a little bit as IntelliJ recommended simplifying it -
                //it now returns true or false from the condition itself! Very fancy.

                public boolean match(Message message){
                        return (message.getMessageNumber() == input.getMessage_ID());
                }
            };

            Message[] results = inbox.search(searchID);
            return getTextFromMessage(results[0]);
        }
        catch (Exception mailEx) {
            mailEx.printStackTrace();}

        return "";
    }

    //Taken from http://stackoverflow.com/questions/11240368/how-to-read-text-inside-body-of-mail-using-javax-mail
    private static String getTextFromMessage(Message message) throws Exception {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws Exception{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    static long countEmails()
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("emailStorage.odb");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query countQuery;

        countQuery = em.createQuery("SELECT COUNT(email) FROM eMailObject email", eMailObject.class);
        em.getTransaction().commit();

        long result = Long.parseLong(countQuery.getSingleResult().toString());

        return result;
    }

    //Creates new instance of TFIDF calculation, adds TFIDF information to the database
    static void addTFIDF(eMailObject email)
    {
        TFIDF tfidfCalculator = new TFIDF();
        tfidfCalculator.tfidf(email);
    }

    //TODO: Handle messaging exceptions more nicely
    //Create connection to server, send message through desired server in reply to desired email
    static void writeReply(String emailToSend, eMailObject emailToReply)
    {
        String mailHost = FileOperations.retrieveCredentials("mailHost", emailToReply.getMailServer());
        String userName = FileOperations.retrieveCredentials("userName", emailToReply.getMailServer());
        String password = FileOperations.retrieveCredentials("password", emailToReply.getMailServer());

        Properties props = new Properties();
        props.put("mail.host", mailHost);
        props.put("mail.user", userName);
        props.put("mail.password", password);
        Session session = Session.getInstance(props, null);

        try{
            Message replyMessage = new MimeMessage(session);

            //TODO: Is this gonna be the right address?
            replyMessage.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(emailToReply.getRecipients().get(0)));

            replyMessage.setSubject("Re: " + emailToReply.getSubject());

            replyMessage.setText(emailToSend);

            replyMessage.setSentDate(new Date());

            Transport.send(replyMessage);
        }
        catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    static void writeNew(String to, String subject, String body, int currentServer)
    {
        String mailHost = FileOperations.retrieveCredentials("mailHost", currentServer);
        String userName = FileOperations.retrieveCredentials("userName", currentServer);
        String password = FileOperations.retrieveCredentials("password", currentServer);

        Properties props = new Properties();
        props.put("mail.host", mailHost);
        props.put("mail.user", userName);
        props.put("mail.password", password);
        Session session = Session.getInstance(props, null);

        try{
            Message newMessage = new MimeMessage(session);

            //TODO: Is this gonna be the right address?
            newMessage.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            newMessage.setSubject(subject);

            newMessage.setText(body);

            newMessage.setSentDate(new Date());

            Transport.send(newMessage);
        }
        catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}