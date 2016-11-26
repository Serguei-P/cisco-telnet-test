package com.serguei.telnet.commands;

/**
 * @author Serguei
 * 
 *         Abstract class describing a command. This implements Runnable as
 *         usually commands executed in a separate thread
 * 
 */
public abstract class Command implements Runnable {
    private final UserSessionContext context;

    /**
     * Name of a command
     * @return
     */
    public abstract String getName();

    /**
     * Processes the command - this is where the job is done.
     */
    public abstract void process();

    public Command(UserSessionContext context) {
        this.context = context;
    }

    /**
     * returns Context data
     * 
     * @return
     */
    public UserSessionContext getContext() {
        return context;
    }

    /**
     * Outputs line.
     * 
     * @param line
     */
    protected boolean outputLine(String line) {
        return context.outputLine(line);
    }

    /**
     * Implementation of run() - it calls process() to do the real job
     */
    public void run() {
        // We don't want two threads to write data to the same socket channel at
        // the same time.
        // Variable 'context' has one-to-one relationship with an opened
        // SocketChannel.
        // In practice this means that the execution of the second command
        // issued by a user is queued until the first
        // command is finished
        synchronized (context) {
            process();
            context.outputPrompt(context.makePrompt());
        }
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
