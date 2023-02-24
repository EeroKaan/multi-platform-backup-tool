/**
 * 2023 Eero Kaan
 * https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

public class Backup {
    public static void startBackup(
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
        String sessionString = Helper.generateRandomString();

        // Source is local, Target is local
        if (!directoryInputIsRemote && !directoryOutputIsRemote) {
            Backup.dumpDatabaseToRespectiveTmp(environment, directoryInputIsRemote, sessionString, lxcContainerName, databaseHost, databaseName, databaseUser, databasePassword, null, null, null);

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

        // Source is local, Target is remote
        else if (!directoryInputIsRemote && directoryOutputIsRemote) {
            String remoteOutputUser = Helper.extractFromRemoteResource("user", directoryOutput);
            String remoteOutputHost = Helper.extractFromRemoteResource("host", directoryOutput);
            String remoteOutputPort = Helper.extractFromRemoteResource("port", directoryOutput);
            String remoteOutputPath = Helper.extractFromRemoteResource("path", directoryOutput);

            Backup.dumpDatabaseToRespectiveTmp(environment, directoryInputIsRemote, sessionString, lxcContainerName, databaseHost, databaseName, databaseUser, databasePassword, null, null, null);

            if (environment.equals("plain") || environment.equals("plesk")) {
                Helper.executeBashCommand("mv /tmp/mpbt-dbdump-" + sessionString + ".sql " + directoryInput + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql");
                Helper.executeBashCommand("cd " + directoryInput + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryInput.replaceAll(".*\\/", ""));
                Helper.executeBashCommand("rm -rf " + directoryInput + "/" + databaseName + "_*.sql");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteOutputPort + "' /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteOutputUser + "@" + remoteOutputHost + ":/" + remoteOutputPath + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
                Helper.executeBashCommand("rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz");
            }
            else if (environment.equals("lxc")) {
                Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql " + directoryInput + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"");
                Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"cd " + directoryInput + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryInput.replaceAll(".*\\/", "") + "\"");
                Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"rm -rf " + directoryInput + "/" + databaseName + "_*.sql\"");
                Helper.executeBashCommand("lxc file pull " + lxcContainerName + "/tmp/mpbt-backup-" + sessionString + ".tar.gz /tmp/mpbt-backup-" + sessionString + ".tar.gz");
                Helper.executeBashCommand("lxc exec " + lxcContainerName + " -- bash -c \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteOutputPort + "' /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteOutputUser + "@" + remoteOutputHost + ":/" + remoteOutputPath + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
                Helper.executeBashCommand("rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz");
            }
        }

        // Source is remote, Target is local
        else if (directoryInputIsRemote && !directoryOutputIsRemote) {
            String remoteInputUser = Helper.extractFromRemoteResource("user", directoryInput);
            String remoteInputHost = Helper.extractFromRemoteResource("host", directoryInput);
            String remoteInputPort = Helper.extractFromRemoteResource("port", directoryInput);
            String remoteInputPath = Helper.extractFromRemoteResource("path", directoryInput);

            Backup.dumpDatabaseToRespectiveTmp(environment, directoryInputIsRemote, sessionString, lxcContainerName, databaseHost, databaseName, databaseUser, databasePassword, remoteInputUser, remoteInputHost, remoteInputPort);

            if (environment.equals("plain") || environment.equals("plesk")) {
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql /" + remoteInputPath + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"cd /" + remoteInputPath + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteInputPath.replaceAll(".*\\/", "") + "\"");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /" + remoteInputPath + "/" + databaseName + "_*.sql\"");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteInputPort + "' " + remoteInputUser + "@" + remoteInputHost + ":/tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryOutput + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
            }
            else if (environment.equals("lxc")) {
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql /" + remoteInputPath + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"cd /" + remoteInputPath + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteInputPath.replaceAll(".*\\/", "") + "\"'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"rm -rf /" + remoteInputPath + "/" + databaseName + "_*.sql\"'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc file pull " + lxcContainerName + "/tmp/mpbt-backup-" + sessionString + ".tar.gz /tmp/mpbt-backup-" + sessionString + ".tar.gz'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"'");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteInputPort + "' " + remoteInputUser + "@" + remoteInputHost + ":/tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryOutput + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
            }
        }

        // Source is remote, Target is remote
        else if (directoryInputIsRemote && directoryOutputIsRemote) {
            String remoteInputUser = Helper.extractFromRemoteResource("user", directoryInput);
            String remoteInputHost = Helper.extractFromRemoteResource("host", directoryInput);
            String remoteInputPort = Helper.extractFromRemoteResource("port", directoryInput);
            String remoteInputPath = Helper.extractFromRemoteResource("path", directoryInput);

            String remoteOutputUser = Helper.extractFromRemoteResource("user", directoryOutput);
            String remoteOutputHost = Helper.extractFromRemoteResource("host", directoryOutput);
            String remoteOutputPort = Helper.extractFromRemoteResource("port", directoryOutput);
            String remoteOutputPath = Helper.extractFromRemoteResource("path", directoryOutput);

            Backup.dumpDatabaseToRespectiveTmp(environment, directoryInputIsRemote, sessionString, lxcContainerName, databaseHost, databaseName, databaseUser, databasePassword, remoteInputUser, remoteInputHost, remoteInputPort);

            if (environment.equals("plain") || environment.equals("plesk")) {
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql /" + remoteInputPath + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"cd /" + remoteInputPath + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteInputPath.replaceAll(".*\\/", "") + "\"");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /" + remoteInputPath + "/" + databaseName + "_*.sql\"");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteInputPort + "' " + remoteInputUser + "@" + remoteInputHost + ":/tmp/mpbt-backup-" + sessionString + ".tar.gz " + "/tmp/mpbt-backup-" + sessionString + ".tar.gz"));
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteOutputPort + "' /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteOutputUser + "@" + remoteOutputHost + ":/" + remoteOutputPath + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
                Helper.executeBashCommand("rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz");
            }
            else if (environment.equals("lxc")) {
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql /" + remoteInputPath + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"cd /" + remoteInputPath + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteInputPath.replaceAll(".*\\/", "") + "\"'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"rm -rf /" + remoteInputPath + "/" + databaseName + "_*.sql\"'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc file pull " + lxcContainerName + "/tmp/mpbt-backup-" + sessionString + ".tar.gz /tmp/mpbt-backup-" + sessionString + ".tar.gz'");
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " 'lxc exec " + lxcContainerName + " -- bash -c \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"'");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteInputPort + "' " + remoteInputUser + "@" + remoteInputHost + ":/tmp/mpbt-backup-" + sessionString + ".tar.gz " + "/tmp/mpbt-backup-" + sessionString + ".tar.gz"));
                Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
                Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avbe 'ssh -p " + remoteOutputPort + "' /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteOutputUser + "@" + remoteOutputHost + ":/" + remoteOutputPath + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
                Helper.executeBashCommand("rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz");
            }
        }

        // Backup finished
        ConsoleOutput.print("success", "Backup finished successfully!");
    }

    private static void dumpDatabaseToRespectiveTmp(String environment, boolean directoryInputIsRemote, String sessionString, String lxcContainerName, String databaseHost, String databaseName, String databaseUser, String databasePassword, String remoteUser, String remoteHost, String remotePort) {
        if (
            !databaseHost.isEmpty() &&
            !databaseName.isEmpty() &&
            !databaseUser.isEmpty() &&
            !databasePassword.isEmpty()
        ) {
            // Remote Machine Command Filters
            String remotePrefixCommand = "";
            String remoteQuotationMarks = "";

            if (directoryInputIsRemote) {
                remotePrefixCommand = "ssh -p " + remotePort + " " + remoteUser + "@" + remoteHost + " ";

                if (environment.equals("lxc")) {remoteQuotationMarks = "'";}
                else {remoteQuotationMarks = "\"";}
            }

            // Create Database Dump
            if (environment.equals("plain")) {
                Helper.executeBashCommand(remotePrefixCommand + remoteQuotationMarks + "mysqldump --opt --no-tablespaces -u'" + databaseUser + "' -p'" + databasePassword + "' -h'" + databaseHost + "' " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql" + remoteQuotationMarks);
            }
            else if (environment.equals("plesk")) {
                Helper.executeBashCommand(remotePrefixCommand + remoteQuotationMarks + "plesk db dump " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql" + remoteQuotationMarks);
            }
            else if (environment.equals("lxc")) {
                Helper.executeBashCommand(remotePrefixCommand + remoteQuotationMarks + "lxc exec " + lxcContainerName + " -- bash -c \"mysqldump --opt --no-tablespaces -u'" + databaseUser + "' -p'" + databasePassword + "' -h'" + databaseHost + "' " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql\"" + remoteQuotationMarks);
            }
        }
    }
}