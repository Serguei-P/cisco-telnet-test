package com.serguei.telnet.commands;

import java.io.File;

/**
 * @author Serguei
 * 
 *         This class is to keep user-related information
 * 
 */
public class UserSessionContext {
    private String currentDir = "";
    private CommandOutput output;

    public UserSessionContext() {
        String dir = (new File(".")).getAbsolutePath();
        if (dir.length() > 0 && dir.endsWith("."))
            dir = dir.substring(0, dir.length() - 1);
        if (dir.length() > 0 && dir.charAt(dir.length() - 1) == File.separatorChar)
            dir = dir.substring(0, dir.length() - 1);
        currentDir = dir;
    }

    /**
     * Returns current directory
     * 
     * @return
     */
    public String getCurrentDir() {
        return currentDir;
    }

    /**
     * Sets current directory
     * 
     * @param currentDir
     */
    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * Sets output object (strategy pattern)
     * 
     * @param output
     */
    public void setOutput(CommandOutput output) {
        this.output = output;
    }

    /**
     * Outputs one line with carriage return
     * 
     * @param line
     * @return
     */
    public boolean outputLine(String line) {
        return output.outputLine(line);
    }

    /**
     * Outputs prompt
     * 
     * @param line
     * @return
     */
    public boolean outputPrompt(String line) {
        return output.outputPrompt(line);
    }

    /**
     * Screen width
     * 
     * @return
     */
    public int maxOutputWidth() {
        return output.maxWidth();
    }

    /**
     * Returns user prompt string.
     * 
     * @return
     */
    public String makePrompt() {
        // In real application this should be
        // customisable but at this point I simply hardcoded it to something
        // unusual but recognisable
        String prompt = "--> ";
        if (currentDir.length() < output.maxWidth() - 40)
            return currentDir + prompt;
        else
            return currentDir.substring(currentDir.length() - 40) + prompt;
    }
}
