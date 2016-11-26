package com.serguei.telnet.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.serguei.telnet.commands.Command;
import com.serguei.telnet.commands.CommandOutput;
import com.serguei.telnet.commands.CommandParser;
import com.serguei.telnet.commands.UserSessionContext;
import com.serguei.telnet.util.Log;

/**
 * @author Serguei
 * 
 *         This class deals with a user session (i.e. one connection)
 * 
 */
public class UserSession {
    private final UserSessionContext context = new UserSessionContext();
    private final SocketChannel socket;
    // currently does not support output for non-Latin characters
    private final CharsetEncoder enc = Charset.forName("US-ASCII").newEncoder();
    private boolean isFinished = false;

    /**
     * Constructs an instance to process one user's connection
     * 
     * @param socket
     */
    public UserSession(SocketChannel socket) {
        this.socket = socket;
        SocketOutput output = new SocketOutput(this);
        context.setOutput(output);
    }

    /**
     * This method checks if the connection did not appear closed during latest
     * processInput() call
     * 
     * @return returns true if the user connection is closing down
     */
    public boolean isFinished() {
        return isFinished;
    }

    private final ByteBuffer buffer = ByteBuffer.allocate(8000);

    /**
     * Process input from a channel
     * 
     */
    public Command processInput() {
        try {
            buffer.clear();
            int bytesRead = socket.read(buffer);
            if (bytesRead > 0) {
                byte[] data = buffer.array();
                for (int i = 0; i < bytesRead; i++) {
                    Command command = processByte(data[i]);
                    if (command != null)
                        return command;
                }
            } else if (bytesRead < 0) {
                // the other side closed connection
                isFinished = true;
                return null;
            }
        } catch (IOException e) {
            // connection is broken
            Log.out("Process Input Exception " + e.getMessage());
            isFinished = true;
            return null;
        }
        return null;
    }

    /**
     * Output Welcome message to the user
     * 
     * @throws IOException
     */
    public void welcome() throws IOException {
        outputLine("-----------------------------------");
        outputLine("Welcome to Serguei's Telnet server!");
        outputLine("Enter 'quit' to exit");
        outputLine("-----------------------------------");
        outputStr(context.makePrompt());
    }

    /**
     * Output GoodBye message to the user
     * 
     * @throws IOException
     */
    public void sayGoodBye() throws IOException {
        outputLine("GoodBye");
    }

    /**
     * Outputs line to the terminal
     * 
     * @param line
     * @throws IOException
     */
    private boolean outputLine(String line) {
        return outputStr(line + "\r\n");
    }

    /**
     * Outputs string to the terminal
     * 
     * @param line
     * @throws IOException
     */
    private boolean outputStr(String line) {
        try {
            socket.write(enc.encode(CharBuffer.wrap(line)));
        } catch (CharacterCodingException e) {
            Log.out("CharacterCodingException: output line contains unsupported symbols");
            return false;
        } catch (IOException e) {
            Log.out("IOException: " + e.getMessage());
            return false;
        }
        return true;
    }

    // this string is built byte-by-byte until user presses CR
    private final StringBuilder strBuilder = new StringBuilder();
    private byte prevByte = 0;

    /**
     * Processing one byte at a time
     * 
     * @param ch
     * @return
     * @throws IOException
     */
    private Command processByte(byte ch) throws IOException {
        if (ch == 10 && prevByte == 13) {
            // ignore LF if it follows CR
        } else if (ch == 10 || ch == 13) {
            String commandStr = strBuilder.toString();
            strBuilder.setLength(0);
            return processCommand(commandStr);
        } else if (ch == 8) {
            if (strBuilder.length() > 0)
                strBuilder.setLength(strBuilder.length() - 1);
        } else
            strBuilder.append((char) ch);
        prevByte = ch;
        return null;
    }

    /**
     * This is a function object that represents strategy for outputting results
     * produced by commands
     */
    private static class SocketOutput implements CommandOutput {
        private final UserSession session;

        private SocketOutput(UserSession session) {
            this.session = session;
        }

        @Override
        public boolean outputLine(String line) {
            return session.outputLine(line);
        }

        @Override
        public boolean outputPrompt(String line) {
            if (line.length() > 0)
                return session.outputStr(line);
            else
                return true;
        }

        @Override
        public int maxWidth() {
            return 80;
        }

    }

    /**
     * This does initial parsing of the command and decides which command to
     * instantiate and return
     * 
     * @param commandStr
     * @return
     */
    private Command processCommand(String commandStr) {
        CommandParser parser = new CommandParser(context, commandStr);
        return parser.parse();
    }

}
