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

import java.util.ArrayList;

public class Job {
    private String tarball;
    private ArrayList<String> jobResources;
    private Operation operation;

    public Job(
        String tarball,
        ArrayList<String> jobResources,
        Operation operation
    ) {
        this.tarball = tarball;
        this.jobResources = jobResources;
        this.operation = operation;
    }

    public void start() {

        // Pre-Job initializations
        String sessionString = Helper.generateRandomString();
        //Helper.shellExecuteCommand("mkdir /tmp/mpbt-" + sessionString);

        //if (this.operation instanceof Restore) {Helper.shellExecuteCommand("tar -xzf " + this.tarball + " -C /tmp/mpbt-" + sessionString);}

        // ToDo: Local/Remote

        // Process different resources with appropriate directions
        this.operation.sessionString = sessionString;

        if (this.jobResources.contains("directory")) {
            this.operation.processDirectory();
        }
        if (this.jobResources.contains("database")) {
            this.operation.processDatabase();
        }
        if (this.jobResources.contains("elasticsearch")) {
            this.operation.processElasticsearch();
        }

        // Finalize job
        //if (this.operation instanceof Backup) {Helper.shellExecuteCommand("tar -czf " + this.tarball + " -C /tmp/mpbt-" + sessionString + " .");}
        //Helper.shellExecuteCommand("rm -rf /tmp/mpbt-" + sessionString);
    }
}