import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExpandedReplies
{
    public ExpandedReplies() {
    }

    //Takes in a message, displays in a textfield - defines reply-points, to expand upon
    public void defineReplyPoints(eMailObject email)
    {
        final eMailObject emailInput = email;
        String emailBody = SkeletonClient.retrieveBody(email);

        JFrame replyPointsWindow = new JFrame("Add Reply Points to \"" + email.getSubject() + "\"");
        Container rpCon = replyPointsWindow.getContentPane();
        rpCon.setLayout(new GridLayout(0, 1));

        JLabel replyPointAdvice = new JLabel("Add Reply-Points -->  *  <-- to the message");
        rpCon.add(replyPointAdvice);

        String[] emailSplitBody = emailBody.split("\n");
        final JTextField[] lines = new JTextField[emailSplitBody.length];
        int currentLine = 0;

        for (String element : emailSplitBody)
        {
            lines[currentLine] = new JTextField(element);
            rpCon.add(lines[currentLine]);
            currentLine++;
        }

        JButton replyButton = new JButton("Reply using added reply-points");

        replyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String replyEmail = "";

                for (JTextField line : lines)
                {
                    replyEmail = replyEmail.concat(line.getText());
                }

                customReply(replyEmail, emailInput);
            }
        });

        rpCon.add(replyButton);

        replyPointsWindow.setVisible(true);
        replyPointsWindow.pack();
    }

    //Takes in custom reply points, displays said email, utilising custom reply points
    public void customReply(String emailBody, final eMailObject emailToReplyTo)
    {
        final String[] bodySplit = emailBody.split("\\*");

        JFrame customReply = new JFrame("Reply using Custom Reply-Points");
        Container crCon = customReply.getContentPane();
        crCon.setLayout(new GridLayout(0, 1));

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new GridLayout(0, 1));

        final JTextField[] fieldsArray = new JTextField[bodySplit.length];
        int currentArrayValue = 0;

        for (String component : bodySplit)
        {
            JPanel componentPanel = new JPanel();
            componentPanel.setLayout(new GridLayout(1, 0));

            final JLabel componentLabel = new JLabel(component);
            componentPanel.add(componentLabel);

            final JTextField componentField = new JTextField("");
            fieldsArray[currentArrayValue] = componentField;
            componentPanel.add(componentField);

            bodyPanel.add(componentPanel);
            currentArrayValue++;
        }

        crCon.add(bodyPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 0));

        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String completeEmail= "";
                int currentIndex = 0;

                for (String component : bodySplit)
                {
                    completeEmail = completeEmail.concat(component + "\n" + fieldsArray[currentIndex]);
                    currentIndex++;
                }

                SkeletonClient.writeReply(completeEmail, emailToReplyTo);
            }
        });

        crCon.add(sendButton);

        customReply.setVisible(true);
        customReply.pack();
    }

    //Some manner of sending an email with custom reply-points
    public void customSend(eMailObject email)
    {

    }

    //Some manner to read into an email sent with custom reply points, calls customReply()
    public void customSendRead()
    {

    }
}