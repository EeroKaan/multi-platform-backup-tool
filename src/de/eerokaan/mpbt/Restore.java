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

        // ToDo: Currently backup-mode "restore" with "local-local" on [plain, plesk] is assumed

        // Pre-Job Initializations
        String sessionString = Helper.generateRandomString();
        Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString);
        Helper.shellExecuteCommand("tar -xzf " + this.target + " -C /tmp/mpbt-" + sessionString);

        // Restore backup to all specified sources
        if (this.jobTypes.contains("directory")) {
            ConsoleOutput.print("message", "Restoring Directory...");

            String pathBase = Helper.pathParseStructure(this.directorySpecific.get("directoryPath")).get("pathBase");

            Helper.shellExecuteCommand("rm -rf " + this.directorySpecific.get("directoryPath"));
            Helper.shellExecuteCommand("tar -xf /tmp/mpbt-" + sessionString + "/directory_*.tar -C " + pathBase + "/");
        }

        if (this.jobTypes.contains("database")) {
            ConsoleOutput.print("message", "Restoring Database...");

            Helper.shellExecuteCommand("mysql -u'" + this.databaseSpecific.get("dbUser") + "' -p'" + this.databaseSpecific.get("dbPassword") + "' -h'" + this.databaseSpecific.get("dbHost") + "' " + this.databaseSpecific.get("dbName") + " < /tmp/mpbt-" + sessionString + "/database_*.sql");
        }

        if (this.jobTypes.contains("elasticsearch")) {
            ConsoleOutput.print("message", "Restoring Elasticsearch...");

            // ToDo: Restore ElasticSearch
        }

        // Finalize Job
        // Lorem
    }
}
