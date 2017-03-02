//Single additional thread, to add TFIDF information to emails
public class TFIDFThread extends Thread {

    public void run()
    {
        String[] query = new String[1];
        query[0] = "";

        eMailObject[] emailContents = SkeletonClient.searchQuery(query, " ");

        for (eMailObject email : emailContents)
        {
            //if (!email.hasMap())
            //{
            //Unsure how exactly this will occur - surely it means email 1 will only have email 1's data to go by?
            //So for now everything is updated, every time. Which might well be very, very slow.
                SkeletonClient.addTFIDF(email);
            //}
        }
    }

}
