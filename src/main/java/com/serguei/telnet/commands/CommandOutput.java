package com.serguei.telnet.commands;

/**
 * @author Serguei
 * 
 *         Implementations of this interface describe how results of the
 *         commands are send to the user (strategy pattern)
 * 
 *         Currently the implementation in local class in
 *         com.serguei.telnet.sockets.UserSession outputs results into a socket,
 *         while implementation in com.serguei.telnet.test.TestCommands for
 *         jUnit stores results in a list
 * 
 */
public interface CommandOutput {

    /**
     * Output string
     * 
     * @param line
     * @return
     */
    public boolean outputLine(String line);

    /**
     * Outputs prompt (if required)
     * 
     * @param line
     * @return
     */
    public boolean outputPrompt(String line);

    /**
     * Maximum output width
     * 
     * @return
     */
    public int maxWidth();
}
