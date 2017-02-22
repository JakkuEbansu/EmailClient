import java.io.*;

//Essentially is just a box for holding file operations, reading and writing values
public class FileOperations
{
    //Allows reading of particular line in a data file
    public static String readFileContents(String fileName, int dataLine)
    {
        try {
            //Set filebuffer to read desired file filename
            FileReader fr = new FileReader(fileName);
            BufferedReader bf = new BufferedReader(fr);

            int currentLine = 0;

            //Loop forward to desired line in file
            while (currentLine < dataLine) {
                currentLine++;
                bf.readLine();
            }

            return bf.readLine();
            //Returns value of outlined line
        }
        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return "-1";
    }

    //Allows writing to particular line in a data file
    public static int writeFileContents(String fileName, int line, String valueToWrite)
    {
        //Write to specific line of data text file, outlining desired value
        try {
            FileWriter fw = new FileWriter("tempWrite.txt");
            BufferedWriter bw = new BufferedWriter(fw);

            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            String currentLine;
            int lineBeingRead = 1;

            //Read line, up until desired line and afterwards, copying and pasting to new temp file
            //When desired line to change is found, new data is changed for old
            while ((currentLine = br.readLine()) != null)
            {
                if(line == lineBeingRead)
                {
                    currentLine = valueToWrite;
                }
                bw.write(currentLine, 0, currentLine.length());

                lineBeingRead++;
            }

            File desiredFile = new File(fileName);
            desiredFile.delete();
            new File("tempWrite.txt").renameTo(desiredFile);

            return 0;
        }

        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return -1;
    }

    //Easy way to retrieve credentials and allow future unsalting
    public static String retrieveCredentials(String mode)
    {
        if (mode.equals("mailHost")) {
            return readFileContents("secure.txt", 1);
        }
        else if (mode.equals("userName")) {
            return readFileContents("secure.txt", 2);
        }
        else if (mode.equals("password")) {
            return readFileContents("secure.txt", 3);
        }
        return "";
    }

    //Easy way to store credentials and eventually salt
    public static void storeCredentials(String mode, String toStore)
    {
        if (mode.equals("mailHost")) {
            writeFileContents("secure.txt", 1, toStore);
        }
        else if (mode.equals("userName")) {
            writeFileContents("secure.txt", 2, toStore);
        }
        else if (mode.equals("password")) {
            writeFileContents("secure.txt", 3, toStore);
        }
    }
}