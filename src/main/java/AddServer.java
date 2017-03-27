import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddServer
{
    private JTextField mailHostField;
    private JTextField userNameField;
    private JPasswordField passwordField;


    public static void addServer(){}

    public void addServerWindow()
    {
        final JFrame addServerWindow = new JFrame("Add new Email Server");
        final Container addServerContainer = addServerWindow.getContentPane();
        addServerContainer.setLayout(new GridLayout(0, 1));

        JLabel titleLabel = new JLabel("Add Email Server");
        addServerContainer.add(titleLabel);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(0, 1));
        JLabel mailHostLabel = new JLabel("Enter mailHost URL (IMAP)");
        labelPanel.add(mailHostLabel);

        JLabel userNameLabel = new JLabel("Enter username");
        labelPanel.add(userNameLabel);

        JLabel passWordLabel = new JLabel("Enter password");
        labelPanel.add(passWordLabel);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridLayout(0, 1));

        mailHostField = new JTextField();
        userNameField = new JTextField();
        passwordField = new JPasswordField();
        fieldPanel.add(mailHostField);
        fieldPanel.add(userNameField);
        fieldPanel.add(passwordField);

        JSplitPane newSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, labelPanel, fieldPanel);

        addServerContainer.add(newSplitPane);

        JPanel clearSubmitPane = new JPanel();
        clearSubmitPane.setLayout(new GridLayout(0, 1));
        JButton submitButton = new JButton("Add Server");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!mailHostField.getText().equals("") && !userNameField.getText().equals("")) {
                    addCredentials(mailHostField.getText(), userNameField.getText(), passwordField.getPassword());
                    //Add credentials to a new file, allowing the mail server to be read in-future
                }
            }
        });
        clearSubmitPane.add(submitButton);

        JButton clearButton = new JButton("Clear fields");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                mailHostField.setText("");
                userNameField.setText("");
                passwordField.setText("");
                //Clear fields of input
            }
        });
        clearSubmitPane.add(clearButton);
        addServerContainer.add(clearSubmitPane);

        addServerWindow.setVisible(true);
        addServerWindow.pack();
    }

    //Write credentials to file
    private void addCredentials(String mailHost, String userName, char[] passWord)
    {
        int serversAdded = Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1));

        FileOperations.storeCredentials("mailHost", mailHost, serversAdded);
        FileOperations.storeCredentials("userName", userName, serversAdded);
        FileOperations.storeCredentials("password", new String(passWord), serversAdded);
        FileOperations.storeCredentials("updatedDate", "0", serversAdded); //To ensure updating

        //Update number of servers registered
        int newServerValue = serversAdded + 1;
        FileOperations.writeFileContents("mailData.txt", 1, "" + newServerValue);
    }

}
