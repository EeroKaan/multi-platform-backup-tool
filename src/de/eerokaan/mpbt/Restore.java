/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

import java.util.ArrayList;
import java.util.HashMap;

public class Restore extends Job {
    public Restore(
        String environment,
        String tarball,
        ArrayList<String> jobTypes,
        HashMap<String, String> directorySpecific,
        HashMap<String, String> databaseSpecific,
        HashMap<String, String> elasticsearchSpecific
    ) {
        super(
            environment,
            tarball,
            jobTypes,
            directorySpecific,
            databaseSpecific,
            elasticsearchSpecific
        );
    }

    @Override
    public void start() {

        // ToDo: Currently "local-local" on [plain, plesk] is assumed -> Implement LXD + Local/Remote Combinations

        // Pre-Job Initializations
        String sessionString = Helper.generateRandomString();
        Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString);
        Helper.shellExecuteCommand("tar -xzf " + this.tarball + " -C /tmp/mpbt-" + sessionString);

        // Restore backup to all specified sources
        if (this.jobTypes.contains("directory")) {
            ConsoleOutput.print("message", "Restoring Directory...");

            String pathBase = Helper.pathParseStructure(this.directorySpecific.get("directoryPath")).get("pathBase");

            Helper.shellExecuteCommand("rm -rf " + this.directorySpecific.get("directoryPath"));
            Helper.shellExecuteCommand("tar -xf /tmp/mpbt-" + sessionString + "/directory_*.tar -C " + pathBase);
        }

        if (this.jobTypes.contains("database")) {
            ConsoleOutput.print("message", "Restoring Database...");

            Helper.shellExecuteCommand("mysql -u'" + this.databaseSpecific.get("dbUser") + "' -p'" + this.databaseSpecific.get("dbPassword") + "' -h'" + this.databaseSpecific.get("dbHost") + "' " + this.databaseSpecific.get("dbName") + " < /tmp/mpbt-" + sessionString + "/database_*.sql");
        }

        if (this.jobTypes.contains("elasticsearch")) {
            ConsoleOutput.print("message", "Restoring Elasticsearch...");

            // Initialize Paths
            String esRepoPath = Helper.shellSearchLocalFileByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ");
            String pathBase = Helper.pathParseStructure(esRepoPath).get("pathBase");

            // Stop Elasticsearch service
            Helper.shellExecuteCommand("service elasticsearch stop");

            // Delete old path.repo data and replace with contents from backup
            Helper.shellExecuteCommand("rm -rf " + esRepoPath);
            Helper.shellExecuteCommand("tar -xf /tmp/mpbt-" + sessionString + "/elasticsearch_*.tar -C " + pathBase);

            // Set correct permissions
            Helper.shellExecuteCommand("chown -R elasticsearch:elasticsearch " + esRepoPath);

            // Restart Elasticsearch service
            Helper.shellExecuteCommand("service elasticsearch start");

            // Create Elasticsearch repository
            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'");

            // Restore from snapshot
            Helper.shellExecuteCommand("curl -XPOST 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot/_restore?wait_for_completion=true'");

            // Delete snapshot
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot'");

            // Delete repository
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'");
        }

        // Finalize Job
        Helper.shellExecuteCommand("rm -rf /tmp/mpbt-" + sessionString);
    }
}
