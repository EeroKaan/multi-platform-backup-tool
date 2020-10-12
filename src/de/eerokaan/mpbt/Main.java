/**
 * 2020 Eero Kaan
 * https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2020 Eero Kaan
 */

package de.eerokaan.mpbt;

public class Main {
    public static void startBackup(
        String directoryInput,
        String directoryOutput,
        boolean directoryInputIsRemote,
        boolean directoryOutputIsRemote,
        String databaseEnvironment,
        String databaseAddress,
        String databaseName,
        String databaseUser,
        String databasePassword
    ) {
        String sessionString = Helper.generateRandomString();

        //Source is local, Target is local
        if (!directoryInputIsRemote && !directoryOutputIsRemote) {
            Main.dumpDatabaseToRespectiveTmp(directoryInputIsRemote, sessionString, databaseEnvironment, databaseAddress, databaseName, databaseUser, databasePassword, null, null, null);
            Helper.executeBashCommand("mv /tmp/mpbt-dbdump-" + sessionString + ".sql " + directoryInput + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql");
            Helper.executeBashCommand("cd " + directoryInput + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryInput.replaceAll(".*\\/", ""));
            Helper.executeBashCommand("rm -rf " + directoryInput + "/" + databaseName + "_*.sql");
            Helper.executeBashCommand("mv /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryOutput + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz");
        }

        //Source is local, Target is remote
        else if (!directoryInputIsRemote && directoryOutputIsRemote) {
            String remoteOutputUser = Helper.extractFromRemoteResource("user", directoryOutput);
            String remoteOutputHost = Helper.extractFromRemoteResource("host", directoryOutput);
            String remoteOutputPort = Helper.extractFromRemoteResource("port", directoryOutput);
            String remoteOutputPath = Helper.extractFromRemoteResource("path", directoryOutput);

            Main.dumpDatabaseToRespectiveTmp(directoryInputIsRemote, sessionString, databaseEnvironment, databaseAddress, databaseName, databaseUser, databasePassword, null, null, null);
            Helper.executeBashCommand("mv /tmp/mpbt-dbdump-" + sessionString + ".sql " + directoryInput + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql");
            Helper.executeBashCommand("cd " + directoryInput + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryInput.replaceAll(".*\\/", ""));
            Helper.executeBashCommand("rm -rf " + directoryInput + "/" + databaseName + "_*.sql");
            Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avzbe 'ssh -p " + remoteOutputPort + "' /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteOutputUser + "@" + remoteOutputHost + ":/" + remoteOutputPath + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
            Helper.executeBashCommand("rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz");
        }

        //Source is remote, Target is local
        else if (directoryInputIsRemote && !directoryOutputIsRemote) {
            String remoteInputUser = Helper.extractFromRemoteResource("user", directoryInput);
            String remoteInputHost = Helper.extractFromRemoteResource("host", directoryInput);
            String remoteInputPort = Helper.extractFromRemoteResource("port", directoryInput);
            String remoteInputPath = Helper.extractFromRemoteResource("path", directoryInput);

            Main.dumpDatabaseToRespectiveTmp(directoryInputIsRemote, sessionString, databaseEnvironment, databaseAddress, databaseName, databaseUser, databasePassword, remoteInputUser, remoteInputHost, remoteInputPort);
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql /" + remoteInputPath + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"");
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"cd /" + remoteInputPath + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteInputPath.replaceAll(".*\\/", "") + "\"");
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /" + remoteInputPath + "/" + databaseName + "_*.sql\"");
            Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avzbe 'ssh -p " + remoteInputPort + "' " + remoteInputUser + "@" + remoteInputHost + ":/tmp/mpbt-backup-" + sessionString + ".tar.gz " + directoryOutput + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");
        }

        //Source is remote, Target is remote
        else if (directoryInputIsRemote && directoryOutputIsRemote) {
            String remoteInputUser = Helper.extractFromRemoteResource("user", directoryInput);
            String remoteInputHost = Helper.extractFromRemoteResource("host", directoryInput);
            String remoteInputPort = Helper.extractFromRemoteResource("port", directoryInput);
            String remoteInputPath = Helper.extractFromRemoteResource("path", directoryInput);

            String remoteOutputUser = Helper.extractFromRemoteResource("user", directoryOutput);
            String remoteOutputHost = Helper.extractFromRemoteResource("host", directoryOutput);
            String remoteOutputPort = Helper.extractFromRemoteResource("port", directoryOutput);
            String remoteOutputPath = Helper.extractFromRemoteResource("path", directoryOutput);

            Main.dumpDatabaseToRespectiveTmp(directoryInputIsRemote, sessionString, databaseEnvironment, databaseAddress, databaseName, databaseUser, databasePassword, remoteInputUser, remoteInputHost, remoteInputPort);
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"mv /tmp/mpbt-dbdump-" + sessionString + ".sql /" + remoteInputPath + "/" + databaseName + "_$(date '+%Y-%m-%d-%H-%M-%S').sql\"");
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"cd /" + remoteInputPath + "/.. && tar -czf /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteInputPath.replaceAll(".*\\/", "") + "\"");
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /" + remoteInputPath + "/" + databaseName + "_*.sql\"");
            Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avzbe 'ssh -p " + remoteInputPort + "' " + remoteInputUser + "@" + remoteInputHost + ":/tmp/mpbt-backup-" + sessionString + ".tar.gz " + "/tmp/mpbt-backup-" + sessionString + ".tar.gz"));
            Helper.executeBashCommand("ssh -p " + remoteInputPort + " " + remoteInputUser + "@" + remoteInputHost + " \"rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz\"");

            Helper.executeBashCommand(Helper.rsyncResilienceWrapper("rsync --partial -avzbe 'ssh -p " + remoteOutputPort + "' /tmp/mpbt-backup-" + sessionString + ".tar.gz " + remoteOutputUser + "@" + remoteOutputHost + ":/" + remoteOutputPath + "/backup_$(date '+%Y-%m-%d-%H-%M-%S').tar.gz"));
            Helper.executeBashCommand("rm -rf /tmp/mpbt-backup-" + sessionString + ".tar.gz");
        }

        //Backup finished
        ConsoleOutput.print("success", "Backup finished successfully!");
    }

    private static void dumpDatabaseToRespectiveTmp(boolean directoryInputIsRemote, String sessionString, String databaseEnvironment, String databaseAddress, String databaseName, String databaseUser, String databasePassword, String remoteUser, String remoteHost, String remotePort) {
        if (directoryInputIsRemote) {
            //Database is on a remote machine
            if ( databaseEnvironment.equals("plain") || databaseEnvironment.equals("plesk") ) {
                if (databaseEnvironment.equals("plain")) {
                    Helper.executeBashCommand("ssh -p " + remotePort + " " + remoteUser + "@" + remoteHost + " \"mysqldump --opt -u'" + databaseUser + "' -p'" + databasePassword + "' -h'" + databaseAddress + "' " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql\"");
                }
                else if (databaseEnvironment.equals("plesk")) {
                    Helper.executeBashCommand("ssh -p " + remotePort + " " + remoteUser + "@" + remoteHost + " \"plesk db dump " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql\"");
                }
            }
        }
        else {
            //Database is on the local machine
            if ( databaseEnvironment.equals("plain") || databaseEnvironment.equals("plesk") ) {
                if (databaseEnvironment.equals("plain")) {
                    Helper.executeBashCommand("mysqldump --opt -u'" + databaseUser + "' -p'" + databasePassword + "' -h'localhost' " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql");
                }
                else if (databaseEnvironment.equals("plesk")) {
                    Helper.executeBashCommand("plesk db dump " + databaseName + " > /tmp/mpbt-dbdump-" + sessionString + ".sql");
                }
            }
        }
    }
}