/**
 * 2023 Eero Kaan
 * https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

public class BackupLocalToLocal implements BackupInterface {
    @Override
    public void startBackup(
        String environment,
        String directoryInput,
        String directoryOutput,
        boolean directoryInputIsRemote,
        boolean directoryOutputIsRemote,
        String lxcContainerName,
        String databaseHost,
        String databaseName,
        String databaseUser,
        String databasePassword
    ) {
        Backup.dumpDatabaseToRespectiveTmp(
            environment,
            directoryInputIsRemote,
            sessionString,
            lxcContainerName,
            databaseHost,
            databaseName,
            databaseUser,
            databasePassword,
            null,
            null,
            null
        );

        if (environment.equals("plain") || environment.equals("plesk")) {
            Helper.executeBashCommand("mv /tmp/mpbt-dbdump-" + sessionString + ".sql " + directoryInput + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql");
            Helper.executeBashCommand("cd " + directoryInput + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryInput.replaceAll(".*\\/", ""));
            Helper.executeBashCommand("rm -rf " + directoryInput + "/" + databaseName + "_*.sql");
            Helper.executeBashCommand("mv /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryOutput + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz");
        }
        else if (environment.equals("lxc")) {
            Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql " + directoryInput + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"");
            Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"cd " + directoryInput + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryInput.replaceAll(".*\\/", "") + "\"");
            Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"rm -rf " + directoryInput + "/" + databaseName + "_*.sql\"");
            Helper.executeBashCommand("lxc file pull " + lxcContainerName + "/tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryOutput + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz");
            Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
        }
    }
}
