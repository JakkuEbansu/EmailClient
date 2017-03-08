import java.io.*;

//Essentially is just a box for holding file operations, reading and writing values
public class FileOperations
{
    //Allows reading of particular line in a data file
    public static String readFileContents(String fileName, int dataLine)
    {
        try {
            if (new File(fileName).exists()) {
                //Set filebuffer to read desired file filename
                FileReader fr = new FileReader(fileName);
                BufferedReader bf = new BufferedReader(fr);

                String lineValue = "0";

                for (int i = 0; i < dataLine; i++)
                {
                    lineValue = bf.readLine();
                }

                bf.close();
                return lineValue;
            }
            else
            {
                return "1";
            }
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
            File desiredFile = new File(fileName);

            if (desiredFile.exists()) {
                FileWriter fw = new FileWriter("tempWrite.txt");
                BufferedWriter bw = new BufferedWriter(fw);

                FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr);

                String lineValue;
                int currentLine = 1;

                while ((lineValue = br.readLine()) != null)
                {
                    if (currentLine == line)
                    {
                        bw.write(valueToWrite);
                    }
                    else
                    {
                        bw.write(lineValue);
                    }
                    bw.newLine();
                    currentLine ++;
                }

                desiredFile.delete();
                new File("tempWrite.txt").renameTo(desiredFile);

                bw.close();
                br.close();
            }
            else
            {
                //If the file does not exist, write value to first line in the file
                FileWriter fw = new FileWriter(desiredFile);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(valueToWrite);
                bw.newLine();
                bw.newLine();
                bw.newLine();
                bw.newLine();
                bw.close();
            }

            return 0;
        }

        catch (Exception ioException){
            ioException.printStackTrace();
        }

        return -1;
    }

    //Easy way to retrieve credentials and allow future unsalting
    public static String retrieveCredentials(String mode, int serverNumber)
    {
        if (mode.equals("mailHost")) {
            return readFileContents("secure" + serverNumber + ".txt", 1);
        }
        else if (mode.equals("userName")) {
            return readFileContents("secure" + serverNumber + ".txt", 2);
        }
        else if (mode.equals("password")) {
            return readFileContents("secure" + serverNumber + ".txt", 3);
        }
        else if (mode.equals("updatedDate"))    {
            return readFileContents("secure" + serverNumber + ".txt", 4);
        }
        return "";
    }

    //TODO: Salt!
    //Easy way to store credentials and eventually salt
    public static void storeCredentials(String mode, String toStore, int serverNumber)
    {
        if (mode.equals("mailHost")) {
            writeFileContents("secure" + serverNumber + ".txt", 1, toStore);
        }
        else if (mode.equals("userName")) {
            writeFileContents("secure" + serverNumber + ".txt", 2, toStore);
        }
        else if (mode.equals("password")) {
            writeFileContents("secure" + serverNumber + ".txt", 3, toStore);
        }
        else if (mode.equals("updatedDate")) {
            writeFileContents("secure" + serverNumber + ".txt", 4, toStore);
        }
    }
}