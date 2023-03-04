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

        // Currently backup-mode "backup" with "local-local" on [plain, plesk] is assumed
        String sessionString = Helper.generateRandomString();
        Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString);

        // Put together backup types from respective sources
        if (this.jobTypes.contains("directory")) {
            Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString + "/directory");
            Helper.shellExecuteCommand("tar -cf /tmp/mpbt-" + sessionString + "/directory/directory_$(date '+%Y-%m-%d-%H-%M-%S').tar -C " + Helper.pathParseLastDir(this.directorySpecific.get("directoryPath"))[0] + "/ " + Helper.pathParseLastDir(this.directorySpecific.get("directoryPath"))[1]);
        }
        if (this.jobTypes.contains("database")) {
            Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString + "/database");
            Helper.shellExecuteCommand("mysqldump --opt --no-tablespaces -u'" + this.directorySpecific.get("dbUser") + "' -p'" + this.directorySpecific.get("dbPassword") + "' -h'" + this.directorySpecific.get("dbHost") + "' " + this.directorySpecific.get("dbName") + " > /tmp/mpbt-" + sessionString + "/database/" + this.directorySpecific.get("dbName") + "_$(date '+%Y-%m-%d-%H-%M-%S').sql");
        }
        if (this.jobTypes.contains("elasticsearch")) {
            String esRepoPath = Helper.shellSearchFileByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ");

            Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString + "/elasticsearch");
            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'");
            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot?wait_for_completion=true' -d '{\"indices\": \"" + this.elasticsearchSpecific.get("esIndexPrefix") + "\",\"ignore_unavailable\": true,\"include_global_state\": false}'");
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'");
            Helper.shellExecuteCommand("tar -cf /tmp/mpbt-" + sessionString + "/elasticsearch/elasticsearch_$(date '+%Y-%m-%d-%H-%M-%S').tar -C " + Helper.pathParseLastDir(esRepoPath)[0] + "/ " + Helper.pathParseLastDir(esRepoPath)[1]);
            Helper.shellExecuteCommand("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'");
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot'");
            Helper.shellExecuteCommand("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'");
        }
    }
}
