package com.serguei.telnet.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.serguei.telnet.util.Utils;

/**
 * @author Serguei
 * 
 *         This processes command "cd"
 * 
 */
public class CdCommand extends Command {
    private final String param;

    public CdCommand(UserSessionContext context, String param) {
        super(context);
        this.param = param;
    }

    @Override
    public void process() {
        String path = getContext().getCurrentDir();
        // this could happen on Windows
        if (path.charAt(path.length()-1) == File.separatorChar)
            path = path.substring(0, path.length()-1);
        String param = this.param;
        // allow to use both / and system-standard on OS with non-Unix separator
        if (File.separatorChar != '/')
            param = param.replace('/', File.separatorChar);
        String newCurrDir;
        if (param.startsWith(File.separator)) {
            int pos = path.indexOf(File.separator);
            if (pos > 0)
                newCurrDir = path.substring(0, pos) + param; // this is for Windows
            else
                newCurrDir = param;  // this is for Unix
        } else {
            List<String> currentList = Utils.convertToList(path, File.separatorChar);
            List<String> list = Utils.convertToList(param, File.separatorChar);
            for (String dir : list) {
                if (dir.equals(".")) {
                    // nothing
                } else if (dir.equals("..")) {
                    currentList.remove(currentList.size() - 1);
                } else if (dir.length() > 0)
                    currentList.add(dir);
            }
            StringBuilder newPath = new StringBuilder();
            for (int i = 0; i < currentList.size(); i++) {
                if (i > 0)
                    newPath.append(File.separatorChar);
                newPath.append(currentList.get(i));
            }
            newCurrDir = newPath.toString();
        }
        File file = new File(newCurrDir);
        if (file.isDirectory()) {
            try {
                getContext().setCurrentDir(file.getCanonicalPath());
            } catch (IOException e) {
                // this should not happen as we know the directory exists
                getContext().setCurrentDir(newCurrDir);
            }
        } else
            outputLine("Path " + newCurrDir + " does not exist");
    }

    @Override
    public String getName() {
        return "cd";
    }

}
