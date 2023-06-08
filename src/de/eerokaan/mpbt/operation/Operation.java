/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.operation;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Operation {
    public String sessionString;
    public String environment;
    public ArrayList<String> jobTypes;
    public HashMap<String, String> directorySpecific;
    public HashMap<String, String> databaseSpecific;
    public HashMap<String, String> elasticsearchSpecific;

    public Operation(
        String environment,
        ArrayList<String> jobTypes,
        HashMap<String, String> directorySpecific,
        HashMap<String, String> databaseSpecific,
        HashMap<String, String> elasticsearchSpecific
    ) {
        this.environment = environment;
        this.jobTypes = jobTypes;
        this.directorySpecific = directorySpecific;
        this.databaseSpecific = databaseSpecific;
        this.elasticsearchSpecific = elasticsearchSpecific;
    }

    public abstract void start();
}