/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.core;

public class Statics {

    // Paths
    public static String ELASTICSEARCH_CONFIG_PATH = "/etc/elasticsearch/elasticsearch.yml";

    // Status Messages: General
    public static String CHECK_OS_ERROR = "Your Operating System is not supported!";
    public static String CHECK_ELASTICSEARCH_USER = "Please make sure that you run Elasticsearch Restores as user \"root\".";
    public static String CHECK_ELASTICSEARCH_CONFIG_ERROR = "Please make sure that the Elasticsearch config file (" + Statics.ELASTICSEARCH_CONFIG_PATH + ") is readable! Consider adding your user to the \"elasticsearch\" group.";
    public static String CHECK_ELASTICSEARCH_REPO_PATH = "Please make sure to specify the \"path.repo\" option in your Elasticsearch config file (" + Statics.ELASTICSEARCH_CONFIG_PATH + ")! Skipping Elasticsearch backup/restore.";
    public static String EXTERNAL_PROGRAM_UNEXPECTED_CLOSE = "An external program closed unexpectedly!";
    public static String GENERIC_ERROR = "A unexpected error occurred!";

    // Status Messages: CLI Sanity-Check
    public static String CLI_PARSE_EXCEPTION = "An error occurred while parsing CLI parameters!";
    public static String CLI_JOB_EXCEPTION = "An error occurred while creating the Job!";
    public static String CLI_SPECIFY_TARBALL = "Please specify a valid tarball where to write to (backup) or where to read from (restore)!";
    public static String CLI_SPECIFY_TARBALL_AS_FILE = "Please specify a valid tarball (A file, not a directory)!";
    public static String CLI_SPECIFY_OPERATION = "Please specify a valid operation!";
    public static String CLI_SPECIFY_ENVIRONMENT = "Please supply a valid environment!";
    public static String CLI_SPECIFY_TYPE = "Please specify at least one backup/restore type!";
    public static String CLI_SPECIFY_DIRECTORY_PATH = "Please specify a directory which should be backup up/restored!";
    public static String CLI_SPECIFY_DATABASE_HOST = "Please specify the database host address!";
    public static String CLI_SPECIFY_DATABASE_NAME = "Please specify the database name!";
    public static String CLI_SPECIFY_DATABASE_USER = "Please specify the database user!";
    public static String CLI_SPECIFY_DATABASE_PASSWORD = "Please specify the database password!";
    public static String CLI_SPECIFY_ELASTICSEARCH_HOST = "Please specify the Elasticsearch host address!";
    public static String CLI_SPECIFY_ELASTICSEARCH_INDEX_PREFIX = "Please specify the Elasticsearch index prefix! Use the wildcard \"*\" to select all indices.";
}