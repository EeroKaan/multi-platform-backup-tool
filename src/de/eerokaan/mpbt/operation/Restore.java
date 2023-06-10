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

        String pathBase = Helper.parseResourceStructure(this.directorySpecific.get("dirPath")).get("pathBase");

        Helper.commandExecute("rm -rf " + this.directorySpecific.get("dirPath"), true, false);
        Helper.commandExecute("tar -xf /tmp/mpbt-" + this.sessionString + "/directory_*.tar -C " + pathBase, true, false);
    }

    @Override
    public void processDatabase() {
        ConsoleOutput.print("message", "Restoring Database...");

        Helper.commandExecute("mysql -u'" + this.databaseSpecific.get("dbUser") + "' -p'" + this.databaseSpecific.get("dbPassword") + "' -h'" + this.databaseSpecific.get("dbHost") + "' " + this.databaseSpecific.get("dbName") + " < /tmp/mpbt-" + this.sessionString + "/database_*.sql", true, false);
    }

    @Override
    public void processElasticsearch() {
        ConsoleOutput.print("message", "Restoring Elasticsearch...");

        // Initialize Paths
        String esRepoPath = Helper.parseResourceByKey(Statics.ELASTICSEARCH_CONFIG_PATH, "path.repo: ");
        String pathBase = Helper.parseResourceStructure(esRepoPath).get("pathBase");

        // Stop Elasticsearch service
        Helper.commandExecute("service elasticsearch stop", true, false);

        // Delete old path.repo data and replace with contents from backup
        Helper.commandExecute("rm -rf " + esRepoPath, true, false);
        Helper.commandExecute("tar -xf /tmp/mpbt-" + this.sessionString + "/elasticsearch_*.tar -C " + pathBase, true, false);

        // Set correct permissions
        Helper.commandExecute("chown -R elasticsearch:elasticsearch " + esRepoPath, true, false);

        // Restart Elasticsearch service
        Helper.commandExecute("service elasticsearch start", true, false);

        // Create Elasticsearch repository
        Helper.commandExecute("curl -XPUT -H 'content-type:application/json' 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo' -d '{\"type\":\"fs\",\"settings\":{\"location\":\"" + esRepoPath + "\",\"compress\":true}}'", true, false);

        // Restore from snapshot
        Helper.commandExecute("curl -XPOST 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot/_restore?wait_for_completion=true'", true, false);

        // Delete snapshot
        Helper.commandExecute("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo/snapshot'", true, false);

        // Delete repository
        Helper.commandExecute("curl -XDELETE 'http://" + this.elasticsearchSpecific.get("esHost") + ":9200/_snapshot/mpbt-repo'", true, false);
    }
}
