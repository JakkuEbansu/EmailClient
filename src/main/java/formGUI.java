import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class formGUI {

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

    //Panel for display of a single email
    private JPanel singleEmailPane;
    private JLabel emailHeader;
    private JLabel senderLabel;
    private JLabel timeSent;
    private JButton readButton;
    private JButton tagButton;
    private JButton deleteButton;

    //Panel to display multiple panes (tag displays)
    private JTabbedPane sectionsPanel;

    //Possible to have multiples - multiple tags on display
    private JScrollPane tagPane;
    private JLabel tagLabel;
    private JPanel singleEmailButtons;
    private JPanel multiTagPane;
    private JButton sentDateButton;
    private JButton receivedDateButton;
    private JButton clearButton;

    public formGUI(String[] panes, String[] sections) {
        //Buttons for the toolbar
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ClientGUI.writeNewEmail();
            }
        });
        toolBar.add(newButton);

        syncButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SkeletonClient.updateEmails();
            }
        });
        toolBar.add(syncButton);

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
            }
        });
        searchBoxButtons.add(clearButton);

        searchBoxButtons.add(searchTerm);

        sendQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String[] searchQuery = searchBox.getText().split(" ");
                eMailObject[] results = SkeletonClient.searchQuery(searchQuery, " ");
                ClientGUI.displayResults(results, searchQuery);
            }
        });

        searchBoxPanel.setLayout(new GridLayout(0, 1));
        searchBoxPanel.add(searchBox);
        searchBoxPanel.add(searchBoxButtons);
        searchBoxPanel.add(sendQueryButton);
        searchBoxPanel.add(searchTerm);

        for (String section : sections) {

            for (String pane : panes) {

                String[] searchArray = new String[2];
                searchArray[0] = "Tag ";
                searchArray[1] = pane;

                eMailObject[] paneQueryResults = SkeletonClient.searchQuery(searchArray, " ");

                int forParameter;

                //TODO: Fix this!
                if (paneQueryResults.length * singleEmailPane.getHeight() > tagPane.getHeight()) {
                    forParameter = tagPane.getHeight() / singleEmailPane.getHeight();
                } else {
                    forParameter = paneQueryResults.length;
                }


                for (int i = 1; i <= forParameter; i++) {

                    final eMailObject currentEmail = paneQueryResults[i];

                    timeSent.setText(currentEmail.getReceivedDate().toString());
                    singleEmailPane.add(timeSent);

                    emailHeader.setText(currentEmail.getSubject());
                    singleEmailPane.add(emailHeader);

                    senderLabel.setText(currentEmail.getSenders().toString());
                    singleEmailPane.add(senderLabel);

                    if (currentEmail.getTags() != null) {
                        tagLabel.setText(currentEmail.getTags().toString());
                    }
                    singleEmailPane.add(tagLabel);

                    readButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            ClientGUI.readEmail(currentEmail);
                        }
                    });
                    singleEmailButtons.add(readButton);

                    tagButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            ClientGUI.addTagGUI(currentEmail);
                        }
                    });
                    singleEmailButtons.add(tagButton);

                    //TODO : Implement email deletion, eventually
                /*deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        ClientGUI.removeEmail(currentEmail);
                    }
                });
                singleEmailButtons.add(deleteButton);*/

                    singleEmailPane.add(singleEmailButtons);

                    tagPane.add(singleEmailPane);
                }

                multiTagPane.add(tagPane);
            }

            sectionsPanel.addTab(section, multiTagPane);
        }
    }

    public static void main(String[] args1, String[] args2) {
        JFrame frame = new JFrame("formGUI");
        frame.setContentPane(new formGUI(args1, args2).wholeWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
