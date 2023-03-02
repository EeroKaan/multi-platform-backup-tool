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
        Helper.executeShellCommand("mkdir /tmp/mpbt-" + sessionString);

        // Put together backup types from respective sources
        if (this.jobTypes.contains("directory")) {
            Helper.executeShellCommand("mkdir /tmp/mpbt-" + sessionString + "/directory");
            Helper.executeShellCommand("cp -r " + this.directorySpecific.get("directoryPath") + " /tmp/mpbt-" + sessionString + "/directory");
        }
        if (this.jobTypes.contains("database")) {
            Helper.executeShellCommand("mkdir /tmp/mpbt-" + sessionString + "/database");
            Helper.executeShellCommand("mysqldump --opt --no-tablespaces -u'" + this.directorySpecific.get("dbUser") + "' -p'" + this.directorySpecific.get("dbPassword") + "' -h'" + this.directorySpecific.get("dbHost") + "' " + this.directorySpecific.get("dbName") + " > /tmp/mpbt-" + sessionString + "/database/" + this.directorySpecific.get("dbName") + "_$(date '+%Y-%m-%d-%H-%M-%S').sql");
        }
        if (this.jobTypes.contains("elasticsearch")) {
            Helper.executeShellCommand("mkdir /tmp/mpbt-" + sessionString + "/elasticsearch");
        }
    }
}
