//Multiple threads, designed to retrieve emails from the corresponding IMAP server and add to my database
public class ImapThread extends Thread {

    private int serverNumber;

    ImapThread(int serverNum)
    {
        serverNumber = serverNum;
    }

    public void run()
    {
        //Read from secure file - username, mailhost, password
        SkeletonClient.retrieveEmail(FileOperations.retrieveCredentials("mailHost", serverNumber),
                FileOperations.retrieveCredentials("userName", serverNumber),
                FileOperations.retrieveCredentials("password", serverNumber), serverNumber);
    }
}