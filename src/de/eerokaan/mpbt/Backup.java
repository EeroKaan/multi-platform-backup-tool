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

public class Backup extends Job {
    public Backup(
        String environment,
        String context,
        String target,
        ArrayList<String> jobTypes,
        HashMap<String, String> directorySpecific,
        HashMap<String, String> databaseSpecific,
        HashMap<String, String> elasticsearchSpecific
    ) {
        super(
            environment,
            context,
            target,
            jobTypes,
            directorySpecific,
            databaseSpecific,
            elasticsearchSpecific
        );
    }

    @Override
    public void start() {

        // ToDo: Currently backup-mode "backup" with "local-local" on [plain, plesk] is assumed

        // Pre-Job initializations
        String sessionString = Helper.generateRandomString();
        Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString);

        // Put together backup types from respective sources
        if (this.jobTypes.contains("directory")) {
            ConsoleOutput.print("message", "Backing up Directory...");

            String pathBase = Helper.pathParseStructure(this.directorySpecific.get("directoryPath")).get("pathBase");
            String pathLastDir = Helper.pathParseStructure(this.directorySpecific.get("directoryPath")).get("pathLastDir");

            Helper.shellExecuteCommand("tar -cf /tmp/mpbt-" + sessionString + "/directory_$(date '+%Y-%m-%d-%H-%M-%S').tar -C " + pathBase + "/ " + pathLastDir);
        }

        if (this.jobTypes.contains("database")) {
            ConsoleOutput.print("message", "Backing up Database...");

            Helper.shellExecuteCommand("mysqldump --opt --no-tablespaces -u'" + this.databaseSpecific.get("dbUser") + "' -p'" + this.databaseSpecific.get("dbPassword") + "' -h'" + this.databaseSpecific.get("dbHost") + "' " + this.databaseSpecific.get("dbName") + " > /tmp/mpbt-" + sessionString + "/database_$(date '+%Y-%m-%d-%H-%M-%S').sql");
        }

        if (this.jobTypes.contains("elasticsearch")) {
            ConsoleOutput.print("message", "Backing up Elasticsearch...");

            String esRepoPath = Helper.shellSearchFileByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ");
            String pathBase = Helper.pathParseStructure(esRepoPath).get("pathBase");
            String pathLastDir = Helper.pathParseStructure(esRepoPath).get("pathLastDir");

            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'");
            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot?wait_for_completion=true' -d '{\"indices\": \"" + this.elasticsearchSpecific.get("esIndexPrefix") + "\",\"ignore_unavailable\": true,\"include_global_state\": false}'");
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'");
            Helper.shellExecuteCommand("tar -cf /tmp/mpbt-" + sessionString + "/elasticsearch_$(date '+%Y-%m-%d-%H-%M-%S').tar -C " + pathBase + "/ " + pathLastDir);
            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'");
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot'");
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'");
        }

        // Finalize Job
        Helper.shellExecuteCommand("tar -czf " + this.target + " -C /tmp/mpbt-" + sessionString + " .");
        Helper.shellExecuteCommand("rm -rf /tmp/mpbt-" + sessionString);
    }
}
