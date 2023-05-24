/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

import java.util.HashMap;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.re2j.*;
import org.apache.commons.vfs2.*;

public class Helper {
    public static String generateRandomString() {
        int leftLimit = 48; // Number "0"
        int rightLimit = 122; // Letter "z"
        int targetStringLength = 8;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        return generatedString;
    }

    public static void shellExecuteCommand(String command) {
        ConsoleOutput.print("debug", command);

        StringBuilder stringBuilder = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {
            String currentLine;
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine + "\n");
            }

            if (process.waitFor() != 0) {
                ConsoleOutput.print("warning", Statics.EXTERNAL_PROGRAM_UNEXPECTED_CLOSE);
            }

            bufferedReader.close();
        }
        catch (IOException | InterruptedException exception) {
            ConsoleOutput.print("error", Statics.GENERIC_ERROR);
            exception.printStackTrace();
        }

        if (stringBuilder.toString().length() > 0) {
            ConsoleOutput.print("message", stringBuilder.toString());
        }
    }

    public static String shellSearchLocalFileByKey(String filePath, String searchKey) {
        String searchResult = null;

        try {
            String currentLine;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));

            while ((currentLine = bufferedReader.readLine()) != null) {
                if (currentLine.contains(searchKey)) {
                    searchResult = currentLine.substring(searchKey.length());
                }
            }

            bufferedReader.close();
        }
        catch (IOException exception) {
            ConsoleOutput.print("error", Statics.GENERIC_ERROR);
            exception.printStackTrace();
        }

        return searchResult;
    }

    public static HashMap<String, String> pathParseStructure(String pathRaw) {
        HashMap<String, String> returnMap = new HashMap<String, String>();

        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject resource = fsManager.resolveFile(pathRaw);

            if (resource.exists()) {
                returnMap.put("pathBase", new URI(resource.getParent().toString()).getPath());
                returnMap.put("pathLastDir", new URI(resource.getName().toString()).getPath());
            }

            resource.close();
        }
        catch (URISyntaxException | FileSystemException exception) {
            ConsoleOutput.print("error", Statics.GENERIC_ERROR);
            exception.printStackTrace();
        }


        return returnMap;
    }

    public static HashMap<String, Boolean> pathParseProperties(String pathRaw) {
        HashMap<String, Boolean> returnMap = new HashMap<String, Boolean>();

        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject resource = fsManager.resolveFile(pathRaw);

            if (resource.exists()) {

                // Populate Map
                returnMap.put("exists", resource.exists());
                returnMap.put("isReadable", resource.isReadable());
                returnMap.put("isDirectory", (resource.getType() == FileType.FOLDER));
                returnMap.put("isFile", false);

                // Special Check for "isFile" (Also checks for file validity, which don't exist yet)
                if (resource.getName() != null) {
                    String fileName = resource.getName().toString();
                    int dotIndex = fileName.lastIndexOf(".");

                    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                        returnMap.put("isFile", true);
                    }
                }
            }

            resource.close();
        }
        catch (FileSystemException exception) {
            ConsoleOutput.print("error", Statics.GENERIC_ERROR);
            exception.printStackTrace();
        }

        return returnMap;
    }

    public static boolean resourceIsRemote(String resource) {
        boolean returnValue = false;

        Pattern pattern = Pattern.compile("^(?P<user>.*?)@(?P<host>.*?):(?:(?P<port>.*?)/)?(?P<path>.*?/.*?)$");
        if (pattern.matcher(resource).find()) {returnValue = true;}

        return returnValue;
    }


    /*public static String rsyncResilienceWrapper(String rsyncRawCommand) {
        return "MAX_RETRIES=10;iterationCounter=0;false;while [ $? -ne 0 -a $iterationCounter -lt $MAX_RETRIES ];do iterationCounter=$(($iterationCounter+1));" + rsyncRawCommand + ";sleep 30;done;if [ $iterationCounter -eq $MAX_RETRIES ];then echo \"Reached max Retries. Aborting.\";fi";
    }*/

    /*public static String extractFromRemoteResource(String dataType, String remoteResource) {
        String outputString = "";

        Pattern pattern = Pattern.compile("^(?P<user>.*?)@(?P<host>.*?):(?:(?P<port>.*?)/)?(?P<path>.*?/.*?)$");
        Matcher matcher = pattern.matcher(remoteResource);

        String remoteUser = "";
        String remoteHost = "";
        String remotePort = "";
        String remotePath = "";

        if (matcher.find()) {
            remoteUser = matcher.group("user");
            remoteHost = matcher.group("host");
            remotePort = matcher.group("port");
            if (remotePort.equals("")) { remotePort = "22"; }
            remotePath = matcher.group("path");
        }

        if (dataType.equals("user")) { outputString = remoteUser; }
        if (dataType.equals("host")) { outputString = remoteHost; }
        if (dataType.equals("port")) { outputString = remotePort; }
        if (dataType.equals("path")) { outputString = remotePath; }

        return outputString;
    }*/

    /*public static String escapeSpecialCharacters(String rawString) {
        return rawString.replace("!", "\\!")
            .replace("#", "\\#")
            .replace("$", "\\$")
            .replace("&", "\\&")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("*", "\\*")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("<", "\\<")
            .replace(">", "\\>")
            .replace("?", "\\?")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("^", "\\^")
            .replace("`", "\\`")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("|", "\\|")
            .replace("'", "\\'")
            .replace("\"", "\\\"");
    }*/

    /*public static void dumpDatabaseToRespectiveTmp(
        String environment,
        boolean directoryInputIsRemote,
        String sessionString,
        String lxcContainerName,
        String databaseHost,
        String databaseName,
        String databaseUser,
        String databasePassword,
        String remoteUser,
        String remoteHost,
        String remotePort
    ) {
        if (
            !databaseHost.isEmpty() &&
            !databaseName.isEmpty() &&
            !databaseUser.isEmpty() &&
            !databasePassword.isEmpty()
        ) {
            // Remote Machine Command Filters
            String remotePrefixCommand = "";
            String remoteQuotationMarks = "";

            if (directoryInputIsRemote) {
                remotePrefixCommand = "ssh -p " + remotePort + " " + remoteUser + "@" + remoteHost + " ";

                if (environment.equals("lxc")) {remoteQuotationMarks = "'";}
                else {remoteQuotationMarks = "\"";}
            }

            // Create Database Dump
            if (environment.equals("plain")) {
                Helper.shellExecuteCommand(remotePrefixCommand + remoteQuotationMarks + "mysqldump --opt --no-tablespaces -u'" + databaseUser + "' -p'" + databasePassword + "' -h'" + databaseHost + "' " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql" + remoteQuotationMarks);
            }
            else if (environment.equals("plesk")) {
                Helper.shellExecuteCommand(remotePrefixCommand + remoteQuotationMarks + "plesk db dump " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql" + remoteQuotationMarks);
            }
            else if (environment.equals("lxc")) {
                Helper.shellExecuteCommand(remotePrefixCommand + remoteQuotationMarks + "lxc exec " + lxcContainerName + " -- bash -c \"mysqldump --opt --no-tablespaces -u'" + databaseUser + "' -p'" + databasePassword + "' -h'" + databaseHost + "' " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql\"" + remoteQuotationMarks);
            }
        }
    }*/
}