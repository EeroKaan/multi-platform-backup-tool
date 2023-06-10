/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.operation;

import de.eerokaan.mpbt.core.*;
import java.util.HashMap;

public class Restore extends Operation {
    public Restore(
        HashMap<String, String> directorySpecific,
        HashMap<String, String> databaseSpecific,
        HashMap<String, String> elasticsearchSpecific
    ) {
        super(
            directorySpecific,
            databaseSpecific,
            elasticsearchSpecific
        );
    }

    @Override
    public void processDirectory() {
        ConsoleOutput.print("message", "Restoring Directory...");

        String pathBase = Helper.parseResourceProperties(this.directorySpecific.get("dirPath")).get("pathBase");

        Helper.shellExecuteCommand("rm -rf " + this.directorySpecific.get("dirPath"), true, false);
        Helper.shellExecuteCommand("tar -xf /tmp/mpbt-" + this.sessionString + "/directory_*.tar -C " + pathBase, true, false);
    }

    @Override
    public void processDatabase() {
        ConsoleOutput.print("message", "Restoring Database...");

        Helper.shellExecuteCommand("mysql -u'" + this.databaseSpecific.get("dbUser") + "' -p'" + this.databaseSpecific.get("dbPassword") + "' -h'" + this.databaseSpecific.get("dbHost") + "' " + this.databaseSpecific.get("dbName") + " < /tmp/mpbt-" + this.sessionString + "/database_*.sql", true, false);
    }

    @Override
    public void processElasticsearch() {
        ConsoleOutput.print("message", "Restoring Elasticsearch...");

        // Initialize Paths
        String esRepoPath = Helper.shellSearchLocalFileByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ");
        String pathBase = Helper.parseResourceProperties(esRepoPath).get("pathBase");

        // Stop Elasticsearch service
        Helper.shellExecuteCommand("service elasticsearch stop", true, false);

        // Delete old path.repo data and replace with contents from backup
        Helper.shellExecuteCommand("rm -rf " + esRepoPath, true, false);
        Helper.shellExecuteCommand("tar -xf /tmp/mpbt-" + this.sessionString + "/elasticsearch_*.tar -C " + pathBase, true, false);

        // Set correct permissions
        Helper.shellExecuteCommand("chown -R elasticsearch:elasticsearch " + esRepoPath, true, false);

        // Restart Elasticsearch service
        Helper.shellExecuteCommand("service elasticsearch start", true, false);

        // Create Elasticsearch repository
        Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'", true, false);

        // Restore from snapshot
        Helper.shellExecuteCommand("curl -XPOST 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot/_restore?wait_for_completion=true'", true, false);

        // Delete snapshot
        Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot'", true, false);

        // Delete repository
        Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'", true, false);
    }
}
