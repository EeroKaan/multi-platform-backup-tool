/**
 * 2021 Eero Kaan
 * https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2021 Eero Kaan
 */

package de.eerokaan.mpbt;

import com.google.re2j.*;
import org.apache.commons.cli.*;

import java.util.Objects;

public class Startup {
    public static void main(String[] args) {

        // Check OS
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            ConsoleOutput.print("error", StatusMessages.OS_NOT_SUPPORTED);
            System.exit(1);
        }

        // CLI Parsing
        Option optionHelp = Option.builder("h")
            .longOpt("help")
            .desc("Display help documentation")
            .hasArg(false)
            .build();
        Option optionDebug = Option.builder("d")
            .longOpt("debug")
            .desc("Enable debug mode")
            .hasArg(false)
            .build();
        Option optionBackup = Option.builder("b")
            .longOpt("backup")
            .desc("Start a Backup job")
            .hasArg(false)
            .build();
        Option optionEnvironment = Option.builder("e")
            .longOpt("environment")
            .desc("The source environment [plain, plesk, lxc]")
            .hasArg(true)
            .build();
        Option optionDirectoryInput = Option.builder("i")
            .longOpt("input")
            .desc("The source directory to be backed up")
            .hasArg(true)
            .build();
        Option optionDirectoryOutput = Option.builder("o")
            .longOpt("output")
            .desc("The output directory for the backup to be placed in")
            .hasArg(true)
            .build();
        Option optionLxcContainerName = Option.builder("lxcn")
            .longOpt("lxcContainerName")
            .desc("The name of the LXC Container containing the source files")
            .hasArg(true)
            .build();
        Option optionDatabaseHost = Option.builder("dbh")
            .longOpt("databaseHost")
            .desc("The database server host (Source Machine / Container is Reference Point)")
            .hasArg(true)
            .build();
        Option optionDatabaseName = Option.builder("dbn")
            .longOpt("databaseName")
            .desc("The name of the database")
            .hasArg(true)
            .build();
        Option optionDatabaseUser = Option.builder("dbu")
            .longOpt("databaseUser")
            .desc("The name of the database user")
            .hasArg(true)
            .build();
        Option optionDatabasePassword = Option.builder("dbp")
            .longOpt("databasePassword")
            .desc("The password of the database user")
            .hasArg(true)
            .build();

        Options options = new Options();
        options.addOption(optionHelp);
        options.addOption(optionDebug);
        options.addOption(optionBackup);
        options.addOption(optionEnvironment);
        options.addOption(optionDirectoryInput);
        options.addOption(optionDirectoryOutput);
        options.addOption(optionLxcContainerName);
        options.addOption(optionDatabaseHost);
        options.addOption(optionDatabaseName);
        options.addOption(optionDatabaseUser);
        options.addOption(optionDatabasePassword);

        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (commandLine.hasOption("h")) {
                String helpHeader = "\nTool for executing Backup tasks on multiple possible Platforms:\nPlain, Plesk, LXC Containers\n\n";

                HelpFormatter formatter = new HelpFormatter();
                formatter.setOptionComparator(null);
                formatter.printHelp("java -jar mpbt.jar", helpHeader, options, null, true);
                System.exit(0);
            }

            if (commandLine.hasOption("d")) {
                ConsoleOutput.debugEnabled = true;
            }

            if (commandLine.hasOption("b")) {
                if (!commandLine.hasOption("e")) {
                    ConsoleOutput.print("error", "No Environment is specified!");
                }
                else if (!commandLine.hasOption("i") && commandLine.hasOption("o")) {
                    ConsoleOutput.print("error", "No input is specified!");
                }
                else if (commandLine.hasOption("i") && !commandLine.hasOption("o")) {
                    ConsoleOutput.print("error", "No output is specified!");
                }
                else if (commandLine.hasOption("i") && commandLine.hasOption("o")) {

                    // Check Environment Value
                    String environment = commandLine.getOptionValue("environment");
                    if ( !(environment.equals("plain") || environment.equals("plesk") || environment.equals("lxc")) ) {
                        ConsoleOutput.print("error", "Specified Environment is not supported!");
                        System.exit(1);
                    }

                    // ToDo: Test if Source and Target are accessible in the first place

                    // Check if Source/Target are Remote
                    boolean directoryInputIsRemote = false;
                    boolean directoryOutputIsRemote = false;
                    String directoryInput = commandLine.getOptionValue("input").replaceAll("/$", "");
                    String directoryOutput = commandLine.getOptionValue("output").replaceAll("/$", "");

                    Pattern pattern = Pattern.compile("^(?P<user>.*?)@(?P<host>.*?):(?:(?P<port>.*?)/)?(?P<path>.*?/.*?)$");
                    if (pattern.matcher(directoryInput).find()) { directoryInputIsRemote = true; }
                    if (pattern.matcher(directoryOutput).find()) { directoryOutputIsRemote = true; }

                    // Get LXC Container Name
                    String lxcContainerName = commandLine.getOptionValue("lxcContainerName");

                    // Check if database should also be considered
                    String databaseHost = commandLine.getOptionValue("databaseHost") != null ? commandLine.getOptionValue("databaseHost") : "";
                    String databaseName = commandLine.getOptionValue("databaseName") != null ? commandLine.getOptionValue("databaseName") : "";
                    String databaseUser = commandLine.getOptionValue("databaseUser") != null ? Helper.escapeSpecialCharacters(commandLine.getOptionValue("databaseUser")) : "";
                    String databasePassword = commandLine.getOptionValue("databasePassword") != null ? Helper.escapeSpecialCharacters(commandLine.getOptionValue("databasePassword")) : "";

                    if (
                        !Objects.equals(databaseHost, "") &&
                        !Objects.equals(databaseName, "") &&
                        !Objects.equals(databaseUser, "") &&
                        !Objects.equals(databasePassword, "")
                    ) {
                        // ToDo: Verify Data before releasing it
                        // ToDo: Check if Environment has mysqldump/plesk Binary (for Plain/Plesk) to work with - For LXC look into specified Container
                    }
                    else {
                        ConsoleOutput.print("warning", StatusMessages.NO_DATABASE_BACKUP);
                    }

                    // Start Backup
                    ConsoleOutput.print("message", "Started Backup process");
                    Main.startBackup(
                        environment,
                        directoryInput,
                        directoryOutput,
                        directoryInputIsRemote,
                        directoryOutputIsRemote,
                        lxcContainerName,
                        databaseHost,
                        databaseName,
                        databaseUser,
                        databasePassword
                    );
                }
                else {
                    ConsoleOutput.print("error", StatusMessages.CLI_NOT_ENOUGH_ARGUMENTS);
                }
            }
            else {
                ConsoleOutput.print("error", StatusMessages.CLI_OPERATION_NOT_SPECIFIED);
            }
        }
        catch (ParseException exception) {
            ConsoleOutput.print("error", StatusMessages.CLI_PARSE_EXCEPTION);
            exception.printStackTrace();
        }
    }
}