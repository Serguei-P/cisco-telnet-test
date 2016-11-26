package com.serguei.telnet.commands;

/**
 * @author Serguei
 * 
 * This is to process a command that we did not recognise
 *
 */
public class WrongCommand extends Command {
    private final String cmdString;

    public WrongCommand(UserSessionContext context, String cmdString) {
        super(context);
        this.cmdString = cmdString;
    }

    @Override
    public void process() {
        if (cmdString.length()>0)
          outputLine("Wrong Command: " + cmdString);
    }

    @Override
    public String getName() {
        return "error";
    }

}
