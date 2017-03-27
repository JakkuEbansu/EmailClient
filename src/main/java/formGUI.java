import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class formGUI {

    private boolean applyTags = false;

    //Buttons for Toolbar
    private JButton newButton;
    private JButton syncButton;

    //Searchbox Components
    private JTextArea searchBox;
    private JButton tag;
    private JButton senders;
    private JButton dateRange;
    private JButton contains;
    private JTextField searchTerm;
    private JButton recipients;
    private JPanel searchBoxButtons;
    private JPanel searchBoxPanel;
    private JButton sendQueryButton;

    //The toolbar itself
    private JToolBar toolBar;

    //The entire window
    private JPanel wholeWindow;

    //Panel to display multiple panes (tag displays)
    private JTabbedPane sectionsPanel;

    //Possible to have multiples - multiple tags on display
    private JScrollPane tagPane;
    private JPanel multiTagPane;
    private JButton sentDateButton;
    private JButton receivedDateButton;
    private JButton clearButton;
    private JTextField tagName;
    private JButton tagApplyButton;
    private JPanel applyTagPanel;
    private JButton newServerButton;
    private JPanel updatePanel;

    public formGUI(String[] panes, String[] sections) {

        wholeWindow.setLayout(new GridLayout(0, 1));

        //Buttons for the toolbar

        //Components for the searchbox - these should update the searchbox
        senders.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")) {
                    searchBox.append("Senders " + searchTerm.getText());
                }
                else
                {
                    searchBox.append(" AND Senders " + searchTerm.getText());
                }
            }
        });
        searchBoxButtons.add(senders);

        dateRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")){
                    searchBox.append("Date.Match " + searchTerm.getText());
                }
                else {
                    searchBox.append(" AND Date.Match " + searchTerm.getText());
                }
            }
        });
        searchBoxButtons.add(dateRange);

        contains.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")) {
                    searchBox.append("Contains " + searchTerm.getText());
                }
                else{
                    searchBox.append(" AND Contains " + searchTerm.getText());
                }
            }
        });
        searchBoxButtons.add(contains);

        recipients.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")) {
                    searchBox.append("Recipients " + searchTerm.getText());
                }
                else {
                    searchBox.append(" AND Recipients " + searchTerm.getText());
                }
            }
        });
        searchBoxButtons.add(recipients);

        tag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")) {
                    searchBox.append("Tag " + searchTerm.getText());
                }
                else
                {
                    searchBox.append(" AND Tag " + searchTerm.getText());
                }
            }
        });
        searchBoxButtons.add(tag);

        sentDateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")) {
                    searchBox.append("Sent-Date " + searchTerm.getText());
                }
                else
                {
                    searchBox.append(" AND Sent-Date " + searchTerm.getText());
                }
            }
        });

        receivedDateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (searchBox.getText().equals("")) {
                    searchBox.append("Received-Date " + searchTerm.getText());
                }
                else
                {
                    searchBox.append(" AND Received-Date " + searchTerm.getText());
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                searchBox.setText("");
                tagName.setText("");
            }
        });
        searchBoxButtons.add(clearButton);

        searchBoxButtons.add(searchTerm);

        sendQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String[] searchQuery = searchBox.getText().split(" ");

                eMailObject[] editTag = applyTags? SkeletonClient.searchQuery(searchQuery, tagName.getText()) : SkeletonClient.searchQuery(searchQuery, " ");

                ClientGUI.displayResults(editTag, searchQuery);
            }
        });

        searchBoxPanel.setLayout(new GridLayout(0, 1));
        searchBoxPanel.add(searchBox);
        searchBoxPanel.add(searchBoxButtons);
        searchBoxPanel.add(sendQueryButton);
        searchBoxPanel.add(searchTerm);

        applyTagPanel.setLayout(new GridLayout(0, 1));

        tagApplyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                applyTags = true;
            }
        });

        applyTagPanel.add(tagName);
        applyTagPanel.add(tagApplyButton);

        updatePanel.setLayout(new GridLayout(0, 1));

        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ClientGUI.writeNewEmail();
            }
        });
        updatePanel.add(newButton);

        syncButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SkeletonClient.updateEmails();
            }
        });
        updatePanel.add(syncButton);

        newServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AddServer serverAdd = new AddServer();
                serverAdd.addServerWindow();
            }
        });
        updatePanel.add(newServerButton);

        toolBar.add(updatePanel);
        toolBar.add(searchBoxPanel);
        toolBar.add(applyTagPanel);

        wholeWindow.add(toolBar);

        for (String section : sections) {
            for (String pane : panes) {

                String[] searchArray = new String[2];
                searchArray[0] = "Tag ";
                searchArray[1] = pane;

                eMailObject[] paneQueryResults = SkeletonClient.searchQuery(searchArray, " ");

                JPanel[] singleEmailPanes = new JPanel[paneQueryResults.length];

                for (int i = 0; i < paneQueryResults.length; i++)
                {
                    final eMailObject currentEmail = paneQueryResults[i];

                    singleEmailPanes[i] = new JPanel();
                    singleEmailPanes[i].setLayout(new GridLayout(1, 0));

                    JLabel receivedDate = new JLabel();
                    receivedDate.setText(currentEmail.getReceivedDate().toString());
                    singleEmailPanes[i].add(receivedDate);

                    JLabel subjectLine = new JLabel();
                    subjectLine.setText(currentEmail.getSubject());
                    singleEmailPanes[i].add(subjectLine);

                    JLabel senders = new JLabel();
                    senders.setText(currentEmail.getSenders().toString());
                    singleEmailPanes[i].add(senders);

                    JLabel tags = new JLabel();
                    if (currentEmail.getTags() != null) {
                        tags.setText(currentEmail.getTags().toString());
                    }
                    else
                    {
                        tags.setText("No Tags Added!");
                    }
                    singleEmailPanes[i].add(tags);

                    JPanel buttons = new JPanel();

                    JButton readButton = new JButton();
                    readButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            ClientGUI.readEmail(currentEmail);
                        }
                    });
                    buttons.add(readButton);

                    JButton tagButton = new JButton();
                    tagButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            ClientGUI.addTagGUI(currentEmail);
                        }
                    });
                    buttons.add(tagButton);

                    singleEmailPanes[i].add(buttons);
                    tagPane.add(singleEmailPanes[i]);
                }

                multiTagPane.add(tagPane);
            }

            sectionsPanel.addTab(section, multiTagPane);

            sectionsPanel.setVisible(true);
        }
        wholeWindow.add(sectionsPanel);
    }

    public static void main(String[] args1, String[] args2) {
        JFrame frame = new JFrame("Skeleton Email Client, Jack Evans 2017");
        frame.setContentPane(new formGUI(args1, args2).wholeWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
