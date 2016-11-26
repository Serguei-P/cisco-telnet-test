package com.serguei.telnet.commands;

/**
 * @author Serguei
 * 
 * This is a Quit command - user decided to close the connection
 * Unlike other commands, this does not do anything (method process() is never called)
 *
 */
public class QuitCommand extends Command {

    public QuitCommand(UserSessionContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public void process() {
        // not supposed to run, for testing purposes only
        outputLine("Quitting");
    }

}
