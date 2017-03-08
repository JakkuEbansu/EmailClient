import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CredentialsPromptGUI
{
    public static void promptGUI()
    {
        final JFrame promptGUIWindow = new JFrame("Please enter Mail Server username, password and hostname");
        Container promptGUIContainer = promptGUIWindow.getContentPane();
        promptGUIContainer.setLayout(new GridLayout(0, 1));
        promptGUIWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel mHPanel = new JPanel();
        mHPanel.setLayout(new GridLayout(1, 0));
        final JLabel mailhostPrompt = new JLabel("Mailhost: ");
        final JTextField mailhostField = new JTextField("", 20);
        mHPanel.add(mailhostPrompt);
        mHPanel.add(mailhostField);
        promptGUIContainer.add(mHPanel);

        final JPanel uNPanel = new JPanel();
        uNPanel.setLayout(new GridLayout(1, 0));
        final JLabel usernamePrompt = new JLabel("Username: ");
        final JTextField usernameField = new JTextField("", 20);
        uNPanel.add(usernamePrompt);
        uNPanel.add(usernameField);
        promptGUIContainer.add(uNPanel);

        final JPanel pWPanel = new JPanel();
        pWPanel.setLayout(new GridLayout(1, 0));
        final JLabel passwordPrompt = new JLabel("Password: ");
        final JTextField passwordField = new JTextField("", 20);
        pWPanel.add(passwordPrompt);
        pWPanel.add(passwordField);
        promptGUIContainer.add(pWPanel);

        //Create text field to edit
        //Add button to add tag to email - close frame once tag update has occurred
        JButton updateButton = new JButton("Add Server");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                File ifExists = new File("mailData.txt");

                if (ifExists.exists())
                {
                    FileOperations.storeCredentials("mailHost", mailhostField.getText(),
                            Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1)));

                    FileOperations.storeCredentials("userName", usernameField.getText(),
                            Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1)));

                    FileOperations.storeCredentials("password", passwordField.getText(),
                            Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1)));

                    FileOperations.storeCredentials("updatedDate", "0",
                            Integer.parseInt(FileOperations.readFileContents("mailData.txt", 1)));
                }
                else
                {
                    FileOperations.storeCredentials("mailHost", mailhostField.getText(), 0);

                    FileOperations.storeCredentials("userName", usernameField.getText(), 0);

                    FileOperations.storeCredentials("password", passwordField.getText(), 0);

                    FileOperations.storeCredentials("updatedDate", "0", 0);
                }

                promptGUIWindow.dispose();
            }
        });
        promptGUIWindow.add(updateButton);
        promptGUIWindow.setVisible(true);
    }
}