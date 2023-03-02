/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.cli.*;

public class Startup {
    public static void main(String[] args) {

        // Check OS
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            ConsoleOutput.print("error", StatusMessages.OS_NOT_SUPPORTED);
            System.exit(1);
        }

        // Retrieve CLI Options
        Options cliOptions = Startup.cliOptionsCreate();

        // Parse CLI and sanity-check inputs
        try {

            // CLI Parsing
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(cliOptions, args);

            // Sanity-Check
            Startup.cliOptionsSanityCheck(commandLine, cliOptions);

            // Check context and TARGET if local/remote
            boolean contextIsRemote = Helper.resourceIsRemote("context", commandLine.getOptionValue("context"));
            boolean targetIsRemote = Helper.resourceIsRemote("target", commandLine.getArgs()[0]);

            // Start Job
            String mode = commandLine.getOptionValue("mode");

            if (mode.equals("backup")) {
                if (!contextIsRemote && !targetIsRemote) {
                    Job job = new Job(
                        commandLine.getOptionValue("environment"),
                        commandLine.getOptionValue("context"),
                        Startup.cliOptionsJobTypes(commandLine),
                        Startup.cliOptionsDirectorySpecific(commandLine),
                        Startup.cliOptionsDatabaseSpecific(commandLine),
                        Startup.cliOptionsElasticsearchSpecific(commandLine)
                    );
                }
            }
            else if (mode.equals("restore")) {

            }
        }
        catch (ParseException exception) {
            ConsoleOutput.print("error", StatusMessages.CLI_PARSE_EXCEPTION);
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private static Options cliOptionsCreate() {

        // CLI Parameters: General
        HashMap<String, Option> cliOptionsMap = new HashMap<String, Option>();

        cliOptionsMap.put(
            "optionHelp",
            Option.builder(null).longOpt("help").desc("Display help documentation").hasArg(false).build()
        );
        cliOptionsMap.put(
            "optionDebug",
            Option.builder(null).longOpt("debug").desc("Enable debug mode").hasArg(false).build()
        );
        cliOptionsMap.put(
            "optionMode",
            Option.builder(null).longOpt("mode").desc("The mode to use MPBT with [backup, restore]").hasArg(true).build()
        );
        cliOptionsMap.put(
            "optionEnvironment",
            Option.builder(null).longOpt("environment").desc("The source environment [plain, plesk, lxc]").hasArg(true).build()
        );
        cliOptionsMap.put(
            "optionContext",
            Option.builder(null).longOpt("context").desc("The machine from which hostnames and paths are viewed from").hasArg(true).build()
        );

        // CLI Parameters: Job Types
        cliOptionsMap.put(
            "optionTypeDirectory",
            Option.builder(null).longOpt("directory").desc("Enable backing up/restoring a directory").hasArg(false).build()
        );
        cliOptionsMap.put(
            "optionTypeDatabase",
            Option.builder(null).longOpt("database").desc("Enable backing up/restoring a MySQL database").hasArg(false).build()
        );
        cliOptionsMap.put(
            "optionTypeElasticsearch",
            Option.builder(null).longOpt("elasticsearch").desc("Enable backing up/restoring a Elasticsearch instance").hasArg(false).build()
        );

        // CLI Parameters: Directory specific
        cliOptionsMap.put(
            "optionDirectoryPath",
            Option.builder(null).longOpt("directoryPath").desc("The directory to backup/restore").hasArg(true).build()
        );

        // CLI Parameters: Database specific
        cliOptionsMap.put(
            "optionDatabaseHost",
            Option.builder(null).longOpt("dbHost").desc("The database server host").hasArg(true).build()
        );
        cliOptionsMap.put(
            "optionDatabaseName",
            Option.builder(null).longOpt("dbName").desc("The name of the database").hasArg(true).build()
        );
        cliOptionsMap.put(
            "optionDatabaseUser",
            Option.builder(null).longOpt("dbUser").desc("The name of the database user").hasArg(true).build()
        );
        cliOptionsMap.put(
            "optionDatabasePassword",
            Option.builder(null).longOpt("dbPassword").desc("The password of the database user").hasArg(true).build()
        );

        // CLI Parameters: Elasticsearch specific
        // LOREM

        // Register CLI Options
        Options cliOptions = new Options();

        for (Map.Entry<String, Option> cliOption : cliOptionsMap.entrySet()) {
            cliOptions.addOption(cliOption.getValue());
        }

        // Return Stage
        return cliOptions;
    }

    private static void cliOptionsSanityCheck(CommandLine commandLine, Options cliOptions) {

        // Help: Print out parameter documentation
        if (commandLine.hasOption("help")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.setOptionComparator(null);
            helpFormatter.printHelp("java -jar mpbt.jar [OPTIONS...] TARGET", "\nTool for executing backup/restore tasks\n\n", cliOptions, null, true);
            System.exit(0);
        }

        // Debug: Enable printing out debug messages
        if (commandLine.hasOption("debug")) {
            ConsoleOutput.debugEnabled = true;
        }

        // Target: Sanity-Check if TARGET is present and valid
        if (commandLine.getArgs().length == 0) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_TARGET);
            System.exit(1);
        }

        String target = commandLine.getArgs()[0];
        if (target.isEmpty()) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_TARGET);
            System.exit(1);
        }

        // Mode: Sanity-Check if tasked with backing up or restoring
        if (!commandLine.hasOption("mode")) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_MODE);
            System.exit(1);
        }

        String mode = commandLine.getOptionValue("mode");
        if ( !(mode.equals("backup") || mode.equals("restore")) ) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_MODE);
            System.exit(1);
        }

        // Environment: Sanity-Check which environment to use
        if (!commandLine.hasOption("environment")) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_ENVIRONMENT);
            System.exit(1);
        }

        String environment = commandLine.getOptionValue("environment");
        if ( !(environment.equals("plain") || environment.equals("plesk") || environment.equals("lxc")) ) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_ENVIRONMENT);
            System.exit(1);
        }

        // Context: Check if Context is reachable
        if (commandLine.hasOption("context")) {
            String context = commandLine.getOptionValue("context");
            boolean contextReachable = Helper.resourceRemoteIsReachable(context);

            if (!contextReachable) {
                ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_CONTEXT_UNREACHABLE);
                System.exit(1);
            }
        }

        // Job Types: Sanity-Check types
        if (!commandLine.hasOption("directory") && !commandLine.hasOption("database") && !commandLine.hasOption("elasticsearch")) {
            ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_TYPE);
            System.exit(1);
        }
        if (commandLine.hasOption("directory")) {
            if (!commandLine.hasOption("directoryPath")) {
                ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_DIRECTORY_PATH);
                System.exit(1);
            }
        }
        if (commandLine.hasOption("database")) {
            if (!commandLine.hasOption("dbHost")) {
                ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_DATABASE_HOST);
                System.exit(1);
            }
            if (!commandLine.hasOption("dbName")) {
                ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_DATABASE_NAME);
                System.exit(1);
            }
            if (!commandLine.hasOption("dbUser")) {
                ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_DATABASE_USER);
                System.exit(1);
            }
            if (!commandLine.hasOption("dbPassword")) {
                ConsoleOutput.print("error", StatusMessages.CLI_SPECIFY_DATABASE_PASSWORD);
                System.exit(1);
            }
        }
        if (commandLine.hasOption("elasticsearch")) {
            // LOREM
        }
    }

    private static ArrayList<String> cliOptionsJobTypes(CommandLine commandLine) {
        ArrayList<String> jobTypes = new ArrayList<String>();

        if (commandLine.hasOption("directory")) {
            jobTypes.add("directory");
        }
        if (commandLine.hasOption("database")) {
            jobTypes.add("database");
        }
        if (commandLine.hasOption("elasticsearch")) {
            jobTypes.add("elasticsearch");
        }

        return jobTypes;
    }

    private static HashMap<String, String> cliOptionsDirectorySpecific(CommandLine commandLine) {
        HashMap<String, String> specificsMap = new HashMap<String, String>();

        if (commandLine.hasOption("directory")) {
            if (commandLine.hasOption("directoryPath")) {
                specificsMap.put(
                    "directoryPath",
                    commandLine.getOptionValue("directoryPath")
                );
            }
        }

        return specificsMap;
    }

    private static HashMap<String, String> cliOptionsDatabaseSpecific(CommandLine commandLine) {
        HashMap<String, String> specificsMap = new HashMap<String, String>();

        if (commandLine.hasOption("database")) {
            if (commandLine.hasOption("dbHost")) {
                specificsMap.put(
                    "dbHost",
                    commandLine.getOptionValue("dbHost")
                );
            }
            if (commandLine.hasOption("dbName")) {
                specificsMap.put(
                    "dbName",
                    commandLine.getOptionValue("dbName")
                );
            }
            if (commandLine.hasOption("dbUser")) {
                specificsMap.put(
                    "dbUser",
                    commandLine.getOptionValue("dbUser")
                );
            }
            if (commandLine.hasOption("dbPassword")) {
                specificsMap.put(
                    "dbPassword",
                    commandLine.getOptionValue("dbPassword")
                );
            }
        }

        return specificsMap;
    }

    private static HashMap<String, String> cliOptionsElasticsearchSpecific(CommandLine commandLine) {
        HashMap<String, String> specificsMap = new HashMap<String, String>();

        // LOREM

        return specificsMap;
    }
}