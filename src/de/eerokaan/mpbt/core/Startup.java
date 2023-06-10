/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.core;

import de.eerokaan.mpbt.operation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.apache.commons.cli.*;

public class Startup {
    public static void main(String[] args) {

        Helper.parseResourceProperties("root@machine.com:/var/log/my/file/is/here.log");

        // Check OS
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            ConsoleOutput.print("error", Statics.CHECK_OS_ERROR);
            System.exit(1);
        }

        // Retrieve CLI Options
        Options cliOptions = Startup.cliOptionsCreate();

        // Check inputs and OS environment - Start Job if successful
        try {

            // CLI Parsing
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(cliOptions, args);

            // Sanity-Check CLI
            Startup.cliOptionsSanityCheck(commandLine, cliOptions);

            // ToDo: Check if OS binaries are present (mysqldump, rsync, evtl. [bash, zsh, sh], etc.)
            // LOREM

            // Start Job
            String operationClass = Objects.equals(commandLine.getOptionValue("operation"), "backup") ? "Backup" : "Restore";

            Job job = new Job(
                commandLine.getArgs()[0],
                Startup.cliOptionsJobResources(commandLine),
                (Operation)Class
                    .forName("de.eerokaan.mpbt.operation." + operationClass)
                    .getConstructor(
                        HashMap.class,
                        HashMap.class,
                        HashMap.class
                    )
                    .newInstance(
                        Startup.cliOptionsDirectorySpecific(commandLine),
                        Startup.cliOptionsDatabaseSpecific(commandLine),
                        Startup.cliOptionsElasticsearchSpecific(commandLine)
                    )
            );
            job.start();
        }
        catch (ParseException exception) {
            ConsoleOutput.print("error", Statics.CLI_PARSE_EXCEPTION);
            exception.printStackTrace();
            System.exit(1);
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            ConsoleOutput.print("error", Statics.CLI_JOB_EXCEPTION);
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
            Option.builder(null).longOpt("debug").desc("Enable debug output").hasArg(false).build()
        );
        cliOptionsMap.put(
            "optionOperation",
            Option.builder(null).longOpt("operation").desc("The operation to use MPBT with [backup, restore]").hasArg(true).build()
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
            "optiondirPath",
            Option.builder(null).longOpt("dirPath").desc("The directory to backup/restore").hasArg(true).build()
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
        cliOptionsMap.put(
            "optionElasticsearchHost",
            Option.builder(null).longOpt("esHost").desc("The Elasticsearch server host").hasArg(true).build()
        );
        cliOptionsMap.put(
            "optionElasticsearchIndexPrefix",
            Option.builder(null).longOpt("esIndexPrefix").desc("The Elasticsearch index prefix to backup/restore").hasArg(true).build()
        );

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
            helpFormatter.printHelp("java -jar mpbt.jar [OPTIONS...] TARBALL", "\nTool for executing backup/restore tasks\n\n", cliOptions, null, true);
            System.exit(0);
        }

        // Debug: Enable printing out debug messages
        if (commandLine.hasOption("debug")) {
            ConsoleOutput.debugEnabled = true;
        }

        // Tarball: Sanity-Check if TARBALL is present and valid
        if (commandLine.getArgs().length == 0) {
            ConsoleOutput.print("error", Statics.CLI_SPECIFY_TARBALL);
            System.exit(1);
        }

        String tarball = commandLine.getArgs()[0];
        if (tarball.isEmpty()) {
            ConsoleOutput.print("error", Statics.CLI_SPECIFY_TARBALL);
            System.exit(1);
        }

        if (!Helper.pathParseProperties(tarball).get("isFile")) {
            ConsoleOutput.print("error", Statics.CLI_SPECIFY_TARBALL_AS_FILE);
            System.exit(1);
        }

        // Operation: Sanity-Check if tasked with backing up or restoring
        if (!commandLine.hasOption("operation")) {
            ConsoleOutput.print("error", Statics.CLI_SPECIFY_OPERATION);
            System.exit(1);
        }

        String operation = commandLine.getOptionValue("operation");
        if ( !(operation.equals("backup") || operation.equals("restore")) ) {
            ConsoleOutput.print("error", Statics.CLI_SPECIFY_OPERATION);
            System.exit(1);
        }

        // Job Types: Sanity-Check resources
        if (!commandLine.hasOption("directory") && !commandLine.hasOption("database") && !commandLine.hasOption("elasticsearch")) {
            ConsoleOutput.print("error", Statics.CLI_SPECIFY_RESOURCE);
            System.exit(1);
        }
        if (commandLine.hasOption("directory")) {
            if (!commandLine.hasOption("dirPath")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_DIRECTORY_PATH);
                System.exit(1);
            }
        }
        if (commandLine.hasOption("database")) {
            if (!commandLine.hasOption("dbHost")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_DATABASE_HOST);
                System.exit(1);
            }
            if (!commandLine.hasOption("dbName")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_DATABASE_NAME);
                System.exit(1);
            }
            if (!commandLine.hasOption("dbUser")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_DATABASE_USER);
                System.exit(1);
            }
            if (!commandLine.hasOption("dbPassword")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_DATABASE_PASSWORD);
                System.exit(1);
            }
        }
        if (commandLine.hasOption("elasticsearch")) {
            if (operation.equals("restore") && !System.getProperty("user.name").equals("root")) {
                ConsoleOutput.print("error", Statics.CHECK_ELASTICSEARCH_USER);
                System.exit(1);
            }
            if (!Helper.pathParseProperties(Statics.ELASTICSEARCH_CONFIG_PATH).get("isReadable")) {
                ConsoleOutput.print("error", Statics.CHECK_ELASTICSEARCH_CONFIG_ERROR);
                System.exit(1);
            }
            if (Helper.shellSearchLocalFileByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ").isEmpty()) {
                ConsoleOutput.print("error", Statics.CHECK_ELASTICSEARCH_REPO_PATH);
                System.exit(1);
            }
            if (!commandLine.hasOption("esHost")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_ELASTICSEARCH_HOST);
                System.exit(1);
            }
            if (!commandLine.hasOption("esIndexPrefix")) {
                ConsoleOutput.print("error", Statics.CLI_SPECIFY_ELASTICSEARCH_INDEX_PREFIX);
                System.exit(1);
            }
        }
    }

    private static ArrayList<String> cliOptionsJobResources(CommandLine commandLine) {
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
            if (commandLine.hasOption("dirPath")) {
                specificsMap.put(
                    "dirPath",
                    commandLine.getOptionValue("dirPath")
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

        if (commandLine.hasOption("elasticsearch")) {
            if (commandLine.hasOption("esHost")) {
                specificsMap.put(
                    "esHost",
                    commandLine.getOptionValue("esHost")
                );
            }
            if (commandLine.hasOption("esIndexPrefix")) {
                specificsMap.put(
                    "esIndexPrefix",
                    commandLine.getOptionValue("esIndexPrefix").equals("*") ? "*,-.*" : commandLine.getOptionValue("esIndexPrefix") + "*"
                );
            }
        }

        return specificsMap;
    }
}