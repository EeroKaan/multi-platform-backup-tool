/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.operation;

import java.util.HashMap;

public abstract class Operation {
    public String sessionString;
    public HashMap<String, String> directorySpecific;
    public HashMap<String, String> databaseSpecific;
    public HashMap<String, String> elasticsearchSpecific;

    public Operation(
        HashMap<String, String> directorySpecific,
        HashMap<String, String> databaseSpecific,
        HashMap<String, String> elasticsearchSpecific
    ) {
        this.directorySpecific = directorySpecific;
        this.databaseSpecific = databaseSpecific;
        this.elasticsearchSpecific = elasticsearchSpecific;
    }

    public abstract void processDirectory();

    public abstract void processDatabase();

    public abstract void processElasticsearch();
}