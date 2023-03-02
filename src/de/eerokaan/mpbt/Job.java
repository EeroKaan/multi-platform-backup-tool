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

public class Job {
    String environment;
    String context;
    ArrayList<String> jobTypes;
    HashMap<String, String> directorySpecific;
    HashMap<String, String> databaseSpecific;
    HashMap<String, String> elasticsearchSpecific;

    public Job(
        String environment,
        String context,
        ArrayList<String> jobTypes,
        HashMap<String, String> directorySpecific,
        HashMap<String, String> databaseSpecific,
        HashMap<String, String> elasticsearchSpecific
    ) {
        this.environment = environment;
        this.context = context;
        this.jobTypes = jobTypes;
        this.directorySpecific = directorySpecific;
        this.databaseSpecific = databaseSpecific;
        this.elasticsearchSpecific = elasticsearchSpecific;
    }
}
