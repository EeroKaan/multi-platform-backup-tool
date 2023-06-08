/**
 *  2023 Eero Kaan
 *  https://eerokaan.de/
 *
 *  @author    Eero Kaan <eero@eerokaan.de>
 *  @copyright 2023 Eero Kaan
 */

package de.eerokaan.mpbt.core;

import java.util.Date;
import java.text.SimpleDateFormat;

public class ConsoleOutput {
    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_RED = "\u001B[31m";
    public static String ANSI_GREEN = "\u001B[32m";
    public static String ANSI_YELLOW = "\u001B[33m";
    public static String ANSI_WHITE = "\u001B[37m";
    public static String ANSI_CYAN = "\u001B[36m";
    public static boolean debugEnabled = false;

    public static void print(String type, String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNow = simpleDateFormat.format(new Date());
        String outputMessage = "";

        if (type.equals("error")) {
            outputMessage = ANSI_RED + "[Error][" + dateNow + "] " + message + ANSI_RESET;
            System.out.println(outputMessage);
        }
        else if (type.equals("warning")) {
            outputMessage = ANSI_YELLOW + "[Warning][" + dateNow + "] " + message + ANSI_RESET;
            System.out.println(outputMessage);
        }
        else if (type.equals("success")) {
            outputMessage = ANSI_GREEN + "[Success][" + dateNow + "] " + message + ANSI_RESET;
            System.out.println(outputMessage);
        }
        else if (type.equals("message")) {
            outputMessage = ANSI_WHITE + "[Message][" + dateNow + "] " + message + ANSI_RESET;
            System.out.println(outputMessage);
        }
        else if (type.equals("debug")) {
            if (debugEnabled) {
                outputMessage = ANSI_CYAN + "[Debug][" + dateNow + "] " + message + ANSI_RESET;
                System.out.println(outputMessage);
            }
        }
    }
}