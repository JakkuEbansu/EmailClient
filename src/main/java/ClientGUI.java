import java.awt.*;
import javax.swing.*;

public class ClientGUI
{
    public static void main(String[] args)
    {
        int gui_panes_line = 0;

        int desiredNoPanes;

        //Read from GUI data for desired number of panes, and pane content (desired tags to be displayed)
        desiredNoPanes = Integer.parseInt(SkeletonClient.readFileContents("gUIData.txt", gui_panes_line));

        String[] panes = new String[desiredNoPanes];

        for(int i = 1; i <= desiredNoPanes; i++)
        {
            //Read desired tag(s) from GUI data file
            panes[i] = SkeletonClient.readFileContents("gUIData.txt", i);
        }

        //Initialise - draw window, basic components
        drawWindow(panes);
    }

    private static int drawWindow(String[] panes, String[] sections)
    {
        //Draw frame, add information - dimensions, title, icon, etc.
        JFrame mainWindow = new JFrame("Making Gnocchi, Version 0.1");
        //TODO : Add icon image for program
        //mainWindow.setIconImage(Image image);

        //Define the dimensions of the full window
        int window_width = Integer.parseInt(SkeletonClient.readFileContents("windowData.txt", 0));
        int window_height = Integer.parseInt(SkeletonClient.readFileContents("windowData.txt", 1));
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
        for (int i = 0; i < panes.length; i++) {
            JPanel tempPanel = new JPanel();
            tempPanel.setLayout(new GridLayout(0, 1));

            //Enable scrolling for the temporary pane to be added
            JScrollPane tempScrollPane = new JScrollPane(tempPanel);

            //Load emails based on relevant tag, add to container -
            //Stored in panes[i]
            //TODO: Load based on saved tags
            SkeletonClient.searchRetrieve(panes[i]);

            panesPanel.add(tempScrollPane);
        }

        content.add(panesPanel);

        return 0;
    }

    //Add method for searchbox - draw searchbox, add actionlistener for search button, load new window with results?
    //Feed to search function in SkeletonClient
}
