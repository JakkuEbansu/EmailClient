import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
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

    private static void drawWindow(String[] panes, String[] sections)
    {
        //Draw frame, add information - dimensions, title, icon, etc.
        JFrame mainWindow = new JFrame("Tagging Email Client Test");

        //TODO: Look into window sizes
        //Define the dimensions of the full window
        int window_width = Integer.parseInt(FileOperations.readFileContents("windowData.txt", 0));
        int window_height = Integer.parseInt(FileOperations.readFileContents("windowData.txt", 1));
        mainWindow.setSize(window_width, window_height);

        //Get content pane (Container for other objects)
        Container content = mainWindow.getContentPane();

        //Define layout of container
        content.setLayout(new GridLayout(1, 2));

        //Create container for storing the panes
        JPanel panesPanel = new JPanel();
        panesPanel.setLayout(new GridLayout(0, 2));

        //Add toolbar to container
        //For each pane, we create a panel to add to the panes container
        for (String pane : panes){
            JPanel tempPanel = new JPanel();
            tempPanel.setLayout(new GridLayout(0, 1));

            //Enable scrolling for the temporary pane to be added
            JScrollPane tempScrollPane = new JScrollPane(tempPanel);

            //Load emails based on relevant tag, add to container - Stored in panes[i]

            String[] searchArray = new String[2];
            searchArray[0] = "Tag ";
            searchArray[1] = pane;

            eMailObject[] paneQueryResults = SkeletonClient.searchQuery(searchArray, " ");

            //Loop through email results, adding to scrollable list of emails, each with read/tag/etc. buttons
            for (final eMailObject result : paneQueryResults) //TODO: Need to limit!
            {
                JPanel resultPanel = new JPanel();
                resultPanel.setLayout(new GridLayout(1, 0));

                String emailTitle = result.getSubject().substring(0, 20) + "... ";

                resultPanel.add(new JLabel(emailTitle));

                JButton readButton = new JButton("Read");
                readButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        //Call Read Email function from SkeletonClient
                        readEmail(result);
                    }
                });
                resultPanel.add(readButton);

                JButton tagButton = new JButton("Tag");
                tagButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) { addTagGUI(result); }
                });
                resultPanel.add(tagButton);

                tempPanel.add(resultPanel);
            }

            panesPanel.add(tempScrollPane);
        }

        content.add(panesPanel);
        mainWindow.setVisible(true);
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

        readEmailWindow.setVisible(true);
    }

    //Add method for searchbox - draw searchbox, add actionlistener for search button, load new window with results?
    //Feed to search function in SkeletonClient
    public static void drawSearchBox(Container container)
    {
        final JPanel searchBoxPanel = new JPanel();
        searchBoxPanel.setLayout(new GridLayout(0, 1));

        final JPanel queryBox = new JPanel();
        queryBox.setLayout(new GridLayout());
        final JTextArea query = new JTextArea();
        final JButton search = new JButton("Search");
        final JButton applyTag = new JButton("Apply Tag");
        final JTextField tagName = new JTextField("", 10);

    }
}