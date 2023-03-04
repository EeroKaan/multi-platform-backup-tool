/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

public class Statics {

    // Paths
    static String ELASTICSEARCH_CONFIG_PATH = "/etc/elasticsearch/elasticsearch.yml";

    // Status Messages: General
    static String CHECK_OS_ERROR = "Your Operating System is not supported!";
    static String CHECK_CONTEXT_ERROR = "A error occurred while checking the specified context for connectivity!";
    static String CHECK_ELASTICSEARCH_CONFIG_ERROR = "Please make sure that the Elasticsearch config file (" + Statics.ELASTICSEARCH_CONFIG_PATH + ") is readable! Consider adding your user to the \"elasticsearch\" group.";
    static String CHECK_ELASTICSEARCH_REPO_PATH = "Please make sure to specify the \"path.repo\" option in your Elasticsearch config file (" + Statics.ELASTICSEARCH_CONFIG_PATH + ")! Skipping Elasticsearch backup/restore.";
    static String EXTERNAL_PROGRAM_UNEXPECTED_CLOSE = "An external program closed unexpectedly!";
    static String GENERIC_ERROR = "A unexpected error occurred!";

    // Status Messages: CLI Sanity-Check
    static String CLI_PARSE_EXCEPTION = "An error occurred while parsing CLI parameters!";
    static String CLI_SPECIFY_TARGET = "Please specify a valid target!";
    static String CLI_SPECIFY_MODE = "Please specify a valid mode!";
    static String CLI_SPECIFY_ENVIRONMENT = "Please supply a valid environment!";
    static String CLI_SPECIFY_CONTEXT_UNREACHABLE = "The specified context is not reachable. Please check connectivity!";
    static String CLI_SPECIFY_TYPE = "Please specify at least one backup/restore type!";
    static String CLI_SPECIFY_DIRECTORY_PATH = "Please specify a directory which should be backup up/restored!";
    static String CLI_SPECIFY_DATABASE_HOST = "Please specify the database host address!";
    static String CLI_SPECIFY_DATABASE_NAME = "Please specify the database name!";
    static String CLI_SPECIFY_DATABASE_USER = "Please specify the database user!";
    static String CLI_SPECIFY_DATABASE_PASSWORD = "Please specify the database password!";
    static String CLI_SPECIFY_ELASTICSEARCH_HOST = "Please specify the Elasticsearch host address!";
    static String CLI_SPECIFY_ELASTICSEARCH_INDEX_PREFIX = "Please specify the Elasticsearch index prefix! Use the wildcard \"*\" to select all indices.";
}