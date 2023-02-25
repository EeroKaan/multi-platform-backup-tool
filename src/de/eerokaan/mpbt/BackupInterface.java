/**
 * 2023 Eero Kaan
 * https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt;

public interface BackupInterface {
    public String sessionString = Helper.generateRandomString();

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
    );
}
