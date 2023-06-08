/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.core;

import de.eerokaan.mpbt.operation.*;
import de.eerokaan.mpbt.direction.*;

public class Job {
    private String tarball;
    private Operation operation;
    private Direction direction;

    public Job(
        String tarball,
        Operation operation,
        Direction direction
    ) {
        this.tarball = tarball;
        this.operation = operation;
        this.direction = direction;
    }

    public void start() {

        // Pre-Job initializations
        String sessionString = Helper.generateRandomString();
        Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString);
        if (this.operation instanceof Restore) {Helper.shellExecuteCommand("tar -xzf " + this.tarball + " -C /tmp/mpbt-" + sessionString);}

        // ToDo: Local/Remote Combinations

        // Operation pipeline
        this.operation.sessionString = sessionString;
        this.operation.start();

        // Finalize job
        if (this.operation instanceof Backup) {Helper.shellExecuteCommand("tar -czf " + this.tarball + " -C /tmp/mpbt-" + sessionString + " .");}
        Helper.shellExecuteCommand("rm -rf /tmp/mpbt-" + sessionString);
    }
}