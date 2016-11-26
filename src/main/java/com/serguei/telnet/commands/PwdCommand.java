package com.serguei.telnet.commands;

/**
 * @author Serguei
 * 
 * This processes command "pwd"
 *
 */
public class PwdCommand extends Command {
    
    public PwdCommand(UserSessionContext context) {
        super(context);
    }
    
    @Override
    public String getName() {
        return "pwd";
    }
    
    @Override
    public void process() {
        outputLine(getContext().getCurrentDir());
    }

}
