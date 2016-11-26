package com.serguei.telnet.commands;

import java.io.File;

/**
 * @author Serguei
 * 
 * This processes command "mkdir"
 *
 */
public class MkDirCommand extends Command {
    private final String param;

    public MkDirCommand(UserSessionContext context, String param) {
        super(context);
        this.param = param;
    }

    @Override
    public void process() {
        if (param.length() > 0) {
            File file = new File(getContext().getCurrentDir() + File.separator + param);
            if (file.mkdirs())
                outputLine(file.getAbsolutePath() + " created.");
            else
                outputLine(file.getAbsolutePath() + "cannot be created");
        } else
            outputLine("Error: Parameter required");
    }

    @Override
    public String getName() {
        return "mkdir";
    }

}
