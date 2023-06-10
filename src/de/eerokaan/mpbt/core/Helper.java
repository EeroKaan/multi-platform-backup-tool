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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.google.re2j.*;

public class Helper {
    public static String generateRandomString() {

        // Initialize
        int leftLimit = 48; // Number "0"
        int rightLimit = 122; // Letter "z"
        int targetStringLength = 8;
        Random random = new Random();

        // Process
        String generatedString = random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        // Return stage
        return generatedString;
    }

    public static String commandExecute(String command, boolean stdoutToConsole, boolean stdoutToReturn) {
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

    public static String[] commandWrapper(String resource) {

        // Initialize
        HashMap<String, String> resourceStructure = Helper.parseResourceStructure(resource);

        String[] wrapper = new String[2];
        String commandPrefix = "";
        String commandSuffix = "";

        // Wrapper for SSH connection
        if ((resourceStructure.get("user") != null) && (resourceStructure.get("host") != null)) {
            commandPrefix = commandPrefix + "ssh -p " + resourceStructure.get("port") + " " + resourceStructure.get("user") + "@" + resourceStructure.get("host") + " '";
            commandSuffix = "'" + commandSuffix;
        }

        // Wrapper for LXC containers
        if (resourceStructure.get("container") != null) {
            commandPrefix = commandPrefix + "lxc exec " + resourceStructure.get("container") + " -- bash -c \"";
            commandSuffix = "\"" + commandSuffix;
        }

        // Return stage
        wrapper[0] = commandPrefix;
        wrapper[1] = commandSuffix;
        return wrapper;
    }

    public static String parseResourceByKey(String resource, String searchKey) {

        // Initialize
        String[] cmdWrapper = Helper.commandWrapper(resource);
        HashMap<String, String> resourceStructure = Helper.parseResourceStructure(resource);

        // Return stage
        return Helper.commandExecute(cmdWrapper[0] + "grep -oP '(?<=" + searchKey + ").*' " + resourceStructure.get("path") + cmdWrapper[1], false, true);
    }

    public static HashMap<String, String> parseResourceStructure(String resource) {
        HashMap<String, String> returnMap = new HashMap<String, String>();

        // Define RegEx pattern
        Pattern pattern = Pattern.compile("^(?:(?P<user>[\\w.-]+)\\@(?P<host>[\\w.-]+):)?(?P<port>\\d+)?:?(?P<container0>lxc%[\\w.-]+)?:?(?P<path0>\\/\\S+)$|^(?P<container1>lxc%[\\w.-]+):(?P<path1>\\/\\S+)$");
        Matcher matcher = pattern.matcher(resource);
        boolean patternMatches = matcher.find();

        // Filter capture groups digestible for RE2J
        String cgUser = matcher.group("user");
        String cgHost = matcher.group("host");
        String cgPort = matcher.group("port");
        String cgContainer0 = matcher.group("container0");
        String cgContainer1 = matcher.group("container1");
        String cgPath0 = matcher.group("path0");
        String cgPath1 = matcher.group("path1");

        String port = cgPort != null ? cgPort : "22";
        String path = cgPath0 != null ? cgPath0 : cgPath1;
        String container = cgContainer0 != null ? cgContainer0 : cgContainer1;

        // Process path only on String level
        String pathBase = null;
        String pathTail = null;

        if (path != null && !path.isEmpty()) {
            pathBase = path.substring(0, path.lastIndexOf("/"));
            pathTail = path.substring(path.lastIndexOf("/") + 1);
        }

        // Process container
        if (container != null) {
            container = container.replaceAll("lxc%", "");
        }

        // If RegEx matches: Return appropriate data
        if (patternMatches) {
            returnMap.put("user", cgUser);
            returnMap.put("host", cgHost);
            returnMap.put("port", port);
            returnMap.put("container", container);
            returnMap.put("path", path);
            returnMap.put("pathBase", pathBase);
            returnMap.put("pathTail", pathTail);
        }

        return returnMap;
    }

    public static HashMap<String, Boolean> parseResourceProperties(String resource) {
        HashMap<String, Boolean> returnMap = new HashMap<String, Boolean>();

        // Initialize
        String[] cmdWrapper = Helper.commandWrapper(resource);
        HashMap<String, String> resourceStructure = Helper.parseResourceStructure(resource);

        returnMap.put("isFile", false);
        returnMap.put("isReadable", false);

        // Process isFile only on String level
        Pattern pattern = Pattern.compile(".*\\.[a-zA-Z0-9]+$");
        Matcher matcher = pattern.matcher(resourceStructure.get("path"));
        boolean patternMatches = matcher.find();

        if (patternMatches) {
            returnMap.put("isFile", true);
        }

        // Process isReadable
        boolean isReadable = Boolean.parseBoolean(Helper.commandExecute(cmdWrapper[0] + "grep -q . " + resourceStructure.get("path") + " && echo 'true' || echo 'false'" + cmdWrapper[1], false, true));

        if (isReadable) {
            returnMap.put("isReadable", true);
        }

        // Return stage
        return returnMap;
    }
}