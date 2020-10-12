/**
 * 2020 Eero Kaan
 * https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2020 Eero Kaan
 */

package de.eerokaan.mpbt;

import com.google.re2j.*;
import org.apache.commons.cli.*;

public class Startup {
    public static void main(String[] args) {
        //Check OS
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            ConsoleOutput.print("error", StatusMessages.OS_NOT_SUPPORTED);
            System.exit(0);
        }

        //CLI Parsing
        Option optionBackup = Option.builder("b")
            .longOpt("backup")
            .desc("Start a Backup job")
            .hasArg(false)
            .build();
        Option optionDebug = Option.builder("d")
            .longOpt("debug")
            .desc("Enable debug mode")
            .hasArg(false)
            .build();
        Option optionHelp = Option.builder("h")
            .longOpt("help")
            .desc("Display help documentation")
            .hasArg(false)
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
        Option optionDatabaseEnvironment = Option.builder("e")
            .longOpt("databaseEnvironment")
            .desc("The environment of the database [plain, plesk]")
            .hasArg(true)
            .build();
        Option optionDatabaseAddress = Option.builder("a")
            .longOpt("databaseAddress")
            .desc("The address/IP of the database server")
            .hasArg(true)
            .build();
        Option optionDatabaseName = Option.builder("n")
            .longOpt("databaseName")
            .desc("The name of the database")
            .hasArg(true)
            .build();
        Option optionDatabaseUser = Option.builder("u")
            .longOpt("databaseUser")
            .desc("The name of the database user")
            .hasArg(true)
            .build();
        Option optionDatabasePassword = Option.builder("p")
            .longOpt("databasePassword")
            .desc("The password of the database user")
            .hasArg(true)
            .build();

        Options options = new Options();
        options.addOption(optionBackup);
        options.addOption(optionDebug);
        options.addOption(optionHelp);
        options.addOption(optionDirectoryInput);
        options.addOption(optionDirectoryOutput);
        options.addOption(optionDatabaseEnvironment);
        options.addOption(optionDatabaseAddress);
        options.addOption(optionDatabaseName);
        options.addOption(optionDatabaseUser);
        options.addOption(optionDatabasePassword);

        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (commandLine.hasOption("h")) {
                String helpHeader = "Tool for executing Backup tasks on multiple possible Platforms (plain, plesk)\n\n";

                HelpFormatter formatter = new HelpFormatter();
                formatter.setOptionComparator(null);
                formatter.printHelp("java -jar mpbt.jar", helpHeader, options, null, true);
                System.exit(0);
            }

            if (commandLine.hasOption("d")) {
                ConsoleOutput.debugEnabled = true;
            }

            if (commandLine.hasOption("b")) {
                if (!commandLine.hasOption("i") && commandLine.hasOption("o")) {
                    ConsoleOutput.print("error", "No input is specified!");
                }
                else if (commandLine.hasOption("i") && !commandLine.hasOption("o")) {
                    ConsoleOutput.print("error", "No output is specified!");
                }
                else if (commandLine.hasOption("i") && commandLine.hasOption("o")) {
                    //ToDo: Test if Source and Target are accessible in the first place
                    //Check if Source/Target are Remote
                    boolean directoryInputIsRemote = false;
                    boolean directoryOutputIsRemote = false;
                    String directoryInput = commandLine.getOptionValue("input").replaceAll("/$", "");
                    String directoryOutput = commandLine.getOptionValue("output").replaceAll("/$", "");

                    Pattern pattern = Pattern.compile("^(?P<user>.*?)@(?P<host>.*?):(?:(?P<port>.*?)/)?(?P<path>.*?/.*?)$");
                    if (pattern.matcher(directoryInput).find()) { directoryInputIsRemote = true; }
                    if (pattern.matcher(directoryOutput).find()) { directoryOutputIsRemote = true; }

                    //Check if database should also be considered
                    String databaseEnvironment = "";
                    String databaseAddress = "";
                    String databaseName = "";
                    String databaseUser = "";
                    String databasePassword = "";

                    if (commandLine.hasOption("e")) {
                        if ( commandLine.getOptionValue("databaseEnvironment").equals("plain") || commandLine.getOptionValue("databaseEnvironment").equals("plesk") ) {
                            //ToDo: Verify Data before releasing it
                            databaseEnvironment = commandLine.getOptionValue("databaseEnvironment");
                            databaseAddress = commandLine.getOptionValue("databaseAddress");
                            databaseName = commandLine.getOptionValue("databaseName");
                            databaseUser = commandLine.getOptionValue("databaseUser");
                            databasePassword = commandLine.getOptionValue("databasePassword");
                        }
                    }
                    else {
                        ConsoleOutput.print("warning", StatusMessages.NO_DATABASE_BACKUP);
                    }

                    //Start Backup
                    ConsoleOutput.print("message", "Started Backup process");
                    Main.startBackup(
                        directoryInput,
                        directoryOutput,
                        directoryInputIsRemote,
                        directoryOutputIsRemote,
                        databaseEnvironment,
                        databaseAddress,
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
                ConsoleOutput.print("error", StatusMessages.CLI_NOT_OPERATION_SPECIFIED);
            }
        }
        catch (ParseException exception) {
            ConsoleOutput.print("error", StatusMessages.CLI_PARSE_EXCEPTION);
            exception.printStackTrace();
        }
    }
}