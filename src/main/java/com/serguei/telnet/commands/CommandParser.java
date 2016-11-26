package com.serguei.telnet.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Serguei
 * 
 *         This class parses a command
 * 
 */
public class CommandParser {
    private final UserSessionContext context;
    private final String commandStr;

    /**
     * Construct an instance of the command parser
     * 
     * @param context
     * @param commandStr
     */
    public CommandParser(UserSessionContext context, String commandStr) {
        this.context = context;
        this.commandStr = commandStr;
    }

    /**
     * This does initial parsing of the command line and decides object of which
     * class implementing abstract Command to instantiate for further processing
     * of the request
     * 
     * @return
     */
    public Command parse() {
        List<String> words = divideIntoWords(commandStr.trim());
        if (words.size() > 0) {
            Command command = null;
            String firstWord = words.remove(0);
            if (firstWord.equals("pwd"))
                command = new PwdCommand(context);
            if (firstWord.equals("cd")) {
                if (words.size() == 0)
                    command = new PwdCommand(context);
                else
                    command = new CdCommand(context, words.get(0));
            } else if (firstWord.equals("ls"))
                command = new LsCommand(context, words);
            else if (firstWord.equals("mkdir"))
                command = new MkDirCommand(context, words.size() > 0 ? words.get(0) : "");
            else if (firstWord.equals("quit"))
                command = new QuitCommand(context);
            if (command != null)
                return command;
        }
        return new WrongCommand(context, commandStr);
    }

    /**
     * Divide string into different elements
     * 
     * @param line
     * @return
     */
    private List<String> divideIntoWords(String line) {
        List<String> result = new ArrayList<String>();
        StringBuilder word = new StringBuilder();
        boolean quote = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            // words can be delimited by space or tab character
            if ((ch == ' ' || ch == 9) && !quote) { 
                if (word.length() > 0)
                    result.add(word.toString());
                word.setLength(0);
            } else if (result.size() == 0 && (ch == '.' || ch == File.separatorChar || ch == '/')) {
                // parameters after such commands as "cd" might start without a
                // space
                if (word.length() > 0)
                    result.add(word.toString());
                word.setLength(0);
                word.append(ch);
            } else if (ch == '"') {
                if (!quote)
                    quote = true;
                else
                    quote = false;
            } else
                word.append(ch);
        }
        if (word.length() > 0)
            result.add(word.toString());
        return result;
    }

}
