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

public class Backup extends Operation {
    public Backup(
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
        ConsoleOutput.print("message", "Backing up Directory...");

        String pathBase = Helper.parseResourceProperties(this.directorySpecific.get("dirPath")).get("pathBase");
        String pathTail = Helper.parseResourceProperties(this.directorySpecific.get("dirPath")).get("pathTail");

        Helper.shellExecuteCommand("tar -cf /tmp/mpbt-" + this.sessionString + "/directory_$(date '+%Y-%m-%d-%H-%M-%S').tar -C " + pathBase + " " + pathTail, true, false);
    }

    @Override
    public void processDatabase() {
        ConsoleOutput.print("message", "Backing up Database...");

        Helper.shellExecuteCommand("mysqldump --opt --no-tablespaces -u'" + this.databaseSpecific.get("dbUser") + "' -p'" + this.databaseSpecific.get("dbPassword") + "' -h'" + this.databaseSpecific.get("dbHost") + "' " + this.databaseSpecific.get("dbName") + " > /tmp/mpbt-" + this.sessionString + "/database_$(date '+%Y-%m-%d-%H-%M-%S').sql", true, false);
    }

    @Override
    public void processElasticsearch() {
        ConsoleOutput.print("message", "Backing up Elasticsearch...");

        // Initialize Paths
        String esRepoPath = Helper.shellSearchLocalFileByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ");
        String pathBase = Helper.parseResourceProperties(esRepoPath).get("pathBase");
        String pathTail = Helper.parseResourceProperties(esRepoPath).get("pathTail");

        // Create Elasticsearch repository
        Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'", true, false);

        // Create snapshot inside repository
        Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot?wait_for_completion=true' -d '{\"indices\": \"" + this.elasticsearchSpecific.get("esIndexPrefix") + "\",\"ignore_unavailable\": true,\"include_global_state\": false}'", true, false);

        // "Unmount" Elasticsearch repository
        Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'", true, false);

        // Backup snapshot from filesystem to archive
        Helper.shellExecuteCommand("tar -cf /tmp/mpbt-" + this.sessionString + "/elasticsearch_$(date '+%Y-%m-%d-%H-%M-%S').tar -C " + pathBase + " " + pathTail, true, false);

        // "Remount" Elasticsearch repository
        Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'", true, false);

        // Delete snapshot
        Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot'", true, false);

        // Delete repository
        Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'", true, false);
    }
}
