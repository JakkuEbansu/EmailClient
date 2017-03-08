import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.swing.*;

public class ClientGUI
{
    private int desiredNoPanes;
    private int desiredNoSections;

    public ClientGUI(){
        desiredNoPanes = Integer.parseInt(FileOperations.readFileContents("panesData.txt", 0));
        desiredNoSections = Integer.parseInt(FileOperations.readFileContents("sectionsData.txt", 0));
        setup();
    }

    //Updates pane/sections data, re-draws GUI
    public void setup()
    {
        String[] panes = new String[desiredNoPanes];
        String[] sections = new String[desiredNoSections];

        //Read line by line into the data files - for each line, the corresponding pane stores its' associated tag
        //Or section-label
        for(int i = 1; i <= desiredNoPanes; i++)
        {
            //Read desired tag(s) from panes data file
            panes[i] = FileOperations.readFileContents("panesData.txt", i);
        }

        for(int i = 1; i <= desiredNoSections; i++)
        {
            //Read desired section(s) from panes data file
            sections[i] = FileOperations.readFileContents("sectionsData.txt", i);
        }

        //Initialise - draw window, basic components
        drawWindow(panes, sections);
    }

    //Intention : Simple function that springs up a nice little window, which you can use to add a tag to an email
    public static void addTagGUI(eMailObject targetEmail)
    {
        final JFrame tagGUIWindow = new JFrame("Add a Tag to this email!");
        Container tagGUIContainer = tagGUIWindow.getContentPane();
        tagGUIWindow.setLayout(new GridLayout(0, 1));
        tagGUIWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final java.util.List<eMailObject> emailsToUpdate = new ArrayList<eMailObject>();
        emailsToUpdate.add(targetEmail);

        final JLabel title = new JLabel(targetEmail.getSubject() + "\nReceived On: " + targetEmail.getReceivedDate());
        tagGUIContainer.add(title);

        //Add input field to outline the tag itself
        final JTextField input = new JTextField("", 10);
        tagGUIContainer.add(input);

        //Create text field to edit
        //Add button to add tag to email - close frame once tag update has occurred
        JButton updateButton = new JButton("Add Tag");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SkeletonClient.updateTags(emailsToUpdate, input.toString());
                tagGUIWindow.dispose();}
        });
        tagGUIContainer.add(updateButton);
        tagGUIWindow.setVisible(true);
    }

    //Intention : Simple function that springs up a nice little window, which you can use to write a reply to an email
    public static void writeEmailReply(final eMailObject replyEmail)
    {
        final JFrame writeEmailWindow = new JFrame("Reply To " + replyEmail.getSenders());

        Container writeEmailContainer = writeEmailWindow.getContentPane();
        writeEmailWindow.setLayout(new GridLayout(0, 1));
        writeEmailWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JLabel title = new JLabel(replyEmail.getSubject() + "\nReceived On: " + replyEmail.getReceivedDate());
        writeEmailContainer.add(title);

        String emailBody = SkeletonClient.retrieveBody(replyEmail);
        final String[] splitBody = emailBody.split("[?]");

        JPanel inlineReplies = new JPanel();

        final String[] emailQuestions = new String[splitBody.length];
        final JTextField[] inlineReply = new JTextField[splitBody.length];
        int currentArrayIndex = 0;

        for (String component : splitBody)
        {
            final JLabel componentLabel = new JLabel(component);
            inlineReplies.add(componentLabel);
            emailQuestions[currentArrayIndex] = component;

            inlineReply[currentArrayIndex] = new JTextField("", 80);
            inlineReplies.add(inlineReply[currentArrayIndex]);

            currentArrayIndex = currentArrayIndex++;
        }

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            {
                String emailToSend = "";

                for (int currentIndex = 0; currentIndex <= splitBody.length; currentIndex++)
                {
                    emailToSend = emailToSend.concat(emailQuestions[currentIndex]);
                    emailToSend = emailToSend.concat("\n" + inlineReply[currentIndex].getText());
                }

                SkeletonClient.writeReply(emailToSend, replyEmail);
            }
        });
        writeEmailContainer.add(sendButton);
        writeEmailWindow.setVisible(true);
    }

    //Intention : Simple function that springs up a nice little window, which you can use to write a reply to an email
    public static void writeNewEmail()
    {
        final JFrame writeEmailWindow = new JFrame("New Email");

        final Container writeEmailContainer = writeEmailWindow.getContentPane();
        writeEmailWindow.setLayout(new GridLayout(0, 1));
        writeEmailWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JLabel title = new JLabel("Subject: ");
        writeEmailContainer.add(title);

        final JTextField desiredTitle = new JTextField();
        writeEmailContainer.add(desiredTitle);

        final JTextField recipient = new JTextField();
        writeEmailContainer.add(recipient);

        int numberOfServers = Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1));

        JPanel mailServerButtonPanel = new JPanel();

        final String userName = "";
        final String mailHost = "";
        int serverNumber;

        for (int i = 0; i <= numberOfServers; i++)
        {
            JButton mailServerButton = new JButton(FileOperations.retrieveCredentials("userName", i) + ", " +
                    FileOperations.retrieveCredentials("mailHost", i));

            final int currentServer = i;

            mailServerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    userName = FileOperations.retrieveCredentials("userName", currentServer);
                    mailHost = FileOperations.retrieveCredentials("mailHost", currentServer);
                }
            });

            mailServerButtonPanel.add(mailServerButton);
        }

        writeEmailContainer.add(mailServerButtonPanel);

        final JTextField emailBody = new JTextField();

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            {
                JLabel alertLabel;

                if (userName.equals(""))
                {
                    alertLabel = new JLabel("Assign Email Host/username!");
                    writeEmailContainer.add(alertLabel);
                }
                else if (desiredTitle.equals(""))
                {
                    alertLabel = new JLabel("Add Email subject!");
                    writeEmailContainer.add(alertLabel);
                }
                else if (emailBody.getText().equals(""))
                {
                    alertLabel = new JLabel("Add Email body!");
                    writeEmailContainer.add(alertLabel);
                }
                else if (recipient.getText().equals(""))
                {
                    alertLabel = new JLabel("Add Email recipient!");
                    writeEmailContainer.add(alertLabel);
                }
                else
                {
                    SkeletonClient.writeNew(recipient.getText(),
                            desiredTitle.getText(), emailBody.getText(), currentServer);
                }
            }
        });
        writeEmailContainer.add(sendButton);
        writeEmailWindow.setVisible(true);
    }

    //Opens a 'read email' dialogue, with more detailed information about said email including retrieving the body of
    //Said email
    public static void readEmail(final eMailObject targetEmail)
    {
        final JFrame readEmailWindow = new JFrame("Reading " + targetEmail.getSubject());
        Container readEmailGUIContainer = readEmailWindow.getContentPane();
        readEmailWindow.setLayout(new GridLayout(0, 1));
        readEmailWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JLabel senders = new JLabel("From: " + targetEmail.getSenders().toString());
        readEmailGUIContainer.add(senders);

        final JLabel recipients = new JLabel("To: " + targetEmail.getRecipients().toString());
        readEmailGUIContainer.add(recipients);

        final JLabel tags = new JLabel("Tags: " + targetEmail.getTags().toString());
        readEmailGUIContainer.add(tags);

        final JLabel sentDate = new JLabel("Sent: " + targetEmail.getSentDate().toString());
        readEmailGUIContainer.add(sentDate);

        final JLabel receivedDate = new JLabel("Received " + targetEmail.getReceivedDate().toString());
        readEmailGUIContainer.add(receivedDate);

        final JButton addTag = new JButton("Add tag to this email");
        addTag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) { addTagGUI(targetEmail); }
        });
        readEmailGUIContainer.add(addTag);

        final JTextArea email = new JTextArea(SkeletonClient.retrieveBody(targetEmail));
        readEmailGUIContainer.add(email);

        final JButton addReply = new JButton("Reply to this email");
        addTag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) { writeEmailReply(targetEmail); }
        });
        readEmailGUIContainer.add(addReply);

        readEmailWindow.setVisible(true);
    }
}