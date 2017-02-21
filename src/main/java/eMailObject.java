import javax.mail.Address;
import java.util.*;

//Object setup for storage of individual emails - to store in arrays, databases, and the like

public class eMailObject {
    private List<Address> senders; //List of email senders
    private List<Address> recipients; //Receivers of the email
    private Date sentDate; //Date sent
    private String subject; //Email subject
    private Object body; //Email body - not filled when created
    private int message_ID; //Unique identifier for email, generated by sender
    private List<String> tags; //Tags to apply to email - unique semantic identifier
    private HashMap<String, Double> tfidfMap; //For searching, instead of storing body
    //Potential future additions to object - reference, tags, etc.



    public eMailObject(List<Address> senders, List<Address> recipients, Date sentDate, String subject, Object body,
                       int message_ID, List<String> tags) {
        this.senders = senders;
        this.recipients = recipients;
        this.sentDate = sentDate;
        this.subject = subject;
        this.body = body;
        this.message_ID = message_ID;
        this.tags = tags;
        this.tfidfMap = new HashMap<String, Double>();
    }

    public List<Address> getSenders() {
        return senders;
    }

    public void setSenders(List<Address> senders) {
        this.senders = senders;
    }

    public List<Address> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Address> recipients) {
        this.recipients = recipients;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) { this.sentDate = sentDate; }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public int getMessage_ID() { return message_ID; }

    public void setMessage_ID(int message_ID) { this.message_ID = message_ID; }

    public List<String> getTags() { return tags; }

    public void addTag(String tag) { this.tags.add(tag); }

    public HashMap gettfidfMap() { return tfidfMap; }

    public void addToMap(String term, Double value)
    {
        if (tfidfMap.containsKey(term))
        {
            tfidfMap.put(term, tfidfMap.get(term) + value);
        }
        else
        {
            tfidfMap.put(term, value);
        }

    }
}