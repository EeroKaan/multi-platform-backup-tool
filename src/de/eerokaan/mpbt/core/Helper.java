/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.core;

import java.util.HashMap;
import java.util.Random;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.re2j.*;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

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

    public static String shellExecuteCommand(String command, boolean stdoutToConsole, boolean stdoutToReturn) {
        ConsoleOutput.print("debug", command);

        // Initialize
        StringBuilder stringBuilder = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        // Interface with external program
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

        // Return stage
        String returnValue = null;

        if (stringBuilder.toString().length() > 0) {
            if (stdoutToConsole) {
                ConsoleOutput.print("message", stringBuilder.toString());
            }
            if (stdoutToReturn) {
                returnValue = stringBuilder.toString();
            }
        }

        return returnValue;
    }

    /*public static String shellSearchLocalFileByKey(String filePath, String searchKey) {
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
    }*/

    public static String parseFileByKey(String filePath, String searchKey) {

        // ToDo: Check if file is remote -> Issue SSH Login before if true

        Helper.shellExecuteCommand("grep -oP '(?<=" + searchKey + ").*' " + filePath, false, true);
    }

    public static HashMap<String, String> parseResourceProperties(String resource) {
        HashMap<String, String> returnMap = new HashMap<String, String>();

        // Define RegEx pattern
        Pattern pattern = Pattern.compile("^(?:(?P<user>[\\w.-]+)\\@(?P<host>[\\w.-]+):)?(?P<port>\\d+)?:?(?P<container0>lxc%[\\w.-]+)?:?(?P<path0>\\/\\S+)$|^(?P<container1>lxc%[\\w.-]+):(?P<path1>\\/\\S+)$");
        Matcher matcher = pattern.matcher(resource);
        boolean patternMatches = matcher.find();

        // Filter capture groups digestible for RE2J
        String user = matcher.group("user");
        String host = matcher.group("host");
        String port = matcher.group("port");
        String container0 = matcher.group("container0");
        String container1 = matcher.group("container1");
        String path0 = matcher.group("path0");
        String path1 = matcher.group("path1");

        String container = (container0 != null ? container0 : container1).replaceAll("lxc%", "");
        String path = path0 != null ? path0 : path1;

        // Process path only on String level
        String pathBase = null;
        String pathTail = null;

        if (path != null && !path.isEmpty()) {
            pathBase = path.substring(0, path.lastIndexOf("/"));
            pathTail = path.substring(path.lastIndexOf("/") + 1);
        }

        // If RegEx matches: Return appropriate data
        if (patternMatches) {
            returnMap.put("user", user);
            returnMap.put("host", host);
            returnMap.put("port", port);
            returnMap.put("container", container);
            returnMap.put("path", path);
            returnMap.put("pathBase", pathBase);
            returnMap.put("pathTail", pathTail);
        }

        return returnMap;
    }

    /*public static HashMap<String, String> pathParseStructure(String pathRaw) {
        HashMap<String, String> returnMap = new HashMap<String, String>();

        // Precondition pathRaw if remote
        if (Helper.resourceIsRemote(pathRaw)) {
            pathRaw = "sftp://" + pathRaw.replace(":/", "/");
        }

        // Process resource
        try {
            FileSystemOptions fsOptions = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
            SftpFileSystemConfigBuilder.getInstance().setIdentityInfo(fsOptions, new IdentityInfo(new File(System.getProperty("user.home") + "/.ssh/id_ed25519")));
            SftpFileSystemConfigBuilder.getInstance().setKnownHosts(fsOptions, new File(System.getProperty("user.home") + "/.ssh/known_hosts"));

            FileSystemManager fsManager = VFS.getManager();
            FileObject resource = fsManager.resolveFile(pathRaw, fsOptions);

            if (resource.exists()) {
                String pathBase = new URI(resource.getParent().toString()).getPath(); // /etc
                String pathLastDir = new URI(resource.getName().toString()).getPath().replace(pathBase + "/", ""); // hosts

                returnMap.put("pathBase", pathBase);
                returnMap.put("pathLastDir", pathLastDir);
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

        // Precondition pathRaw if remote
        if (Helper.resourceIsRemote(pathRaw)) {
            pathRaw = "sftp://" + pathRaw.replace(":/", "/");
        }

        // Process resource
        try {
            // ToDo: Wie oben "SftpFileSystemConfigBuilder" einbauen

            FileSystemManager fsManager = VFS.getManager();
            FileObject resource = fsManager.resolveFile(pathRaw);

            // Update attributes if resource exists
            if (resource.exists()) {
                returnMap.put("exists", resource.exists());
                returnMap.put("isReadable", resource.isReadable());
            }
            else {
                returnMap.put("exists", false);
                returnMap.put("isReadable", false);
            }

            // Process isDirectory/isFile (even for yet non-existent resources)
            String resourceName = pathRaw.substring(pathRaw.lastIndexOf("/") + 1);
            int resourceExtensionIndex = resourceName.lastIndexOf(".");

            if (resourceExtensionIndex > 0 && resourceExtensionIndex < resourceName.length() - 1) {
                returnMap.put("isDirectory", false);
                returnMap.put("isFile", true);
            }
            else {
                returnMap.put("isDirectory", true);
                returnMap.put("isFile", false);
            }

            resource.close();
        }
        catch (FileSystemException exception) {
            ConsoleOutput.print("error", Statics.GENERIC_ERROR);
            exception.printStackTrace();
        }

        return returnMap;
    }*/
}