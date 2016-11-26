package com.serguei.telnet.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Serguei
 * 
 * Utility class
 *
 */
public class Utils {
    private Utils() {
    }

    /**
     * Converts string to list of strings using delimiter
     * @param line
     * @param delimiter
     * @return
     */
    public static List<String> convertToList(String line, char delimiter) {
        List<String> result = new ArrayList<String>();
        if (line.length() == 0)
            return result;
        int startPos = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == delimiter) {
                if (i > startPos)
                    result.add(line.substring(startPos, i));
                else
                    result.add("");
                startPos = i + 1;
            }
        }
        if (startPos < line.length())
            result.add(line.substring(startPos));
        else
            result.add("");
        return result;
    }

}
