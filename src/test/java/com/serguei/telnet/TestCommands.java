package com.serguei.telnet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.serguei.telnet.commands.Command;
import com.serguei.telnet.commands.CommandOutput;
import com.serguei.telnet.commands.CommandParser;
import com.serguei.telnet.commands.UserSessionContext;

public class TestCommands {
    
    private class TestOutput implements CommandOutput {
        private final List<String> list = new ArrayList<String>(); 
        @Override
        public boolean outputLine(String line) {
            System.out.println(line);
            list.add(line);
            return true;
        }
        public boolean outputPrompt(String line) {
            return true;
        }
        public String getLine() {
            if (list.size()>0)
                return list.get(0);
            else
                return null;
        }
        @Override
        public int maxWidth() {
            return 80;
        }
        public boolean included(String value) {
            for (String line : list) {
                if (line.indexOf(value)>=0)
                    return true;
            }
            return false;
        }
    }
    
    private TestOutput output;
    private UserSessionContext context;
    private String rootDir;
    
    @Before
    public void beforeTest() {
      output = new TestOutput();
      context = new UserSessionContext();
      context.setOutput(output);
      rootDir = System.getProperty("user.dir");
    }
    
    private void runCommand(String commandStr) {
        output.list.clear();
        CommandParser parser = new CommandParser(context,commandStr);
        Command command = parser.parse();
        command.run();       
    }
    
    @Test
    public void testPwd() {
        runCommand("pwd");
        Assert.assertEquals(rootDir, output.getLine());
    }
    
    @Test
    public void testCd() {
        // the directories created for Maven project
        // it should work with both Unix-style or System-specific separators
        String startPath = rootDir + File.separator + "target" + File.separator + "classes";
        runCommand("cd target/classes");
        String expectedPath = startPath;
        runCommand("pwd");
        Assert.assertEquals(expectedPath, output.getLine());
        runCommand("cd  com/serguei/telnet");  //double space
        runCommand("pwd");
        expectedPath = startPath + File.separator + "com" + File.separator + "serguei"+ File.separator + "telnet";
        Assert.assertEquals(expectedPath, output.getLine());
        runCommand("cd..");  // no space
        runCommand("pwd");
        expectedPath = startPath + File.separator + "com" + File.separator + "serguei";
        Assert.assertEquals(expectedPath, output.getLine());
        runCommand("cd .." + File.separator + "serguei" + File.separator + "telnet" + File.separator + "sockets");
        runCommand("pwd");
        expectedPath = startPath + File.separator + "com" + File.separator + "serguei"+ File.separator + "telnet"+ File.separator + "sockets";
        Assert.assertEquals(expectedPath, output.getLine());
        runCommand("cd");
        Assert.assertEquals(expectedPath, output.getLine());
        runCommand("cd $$$$");
        String errorStr = "Path "+expectedPath + File.separator + "$$$$ does not exist";
        Assert.assertEquals(errorStr, output.getLine());
        runCommand("pwd");
        Assert.assertEquals(expectedPath, output.getLine());
        runCommand("cd /");
        runCommand("pwd");
 //  Win-only     Assert.assertEquals("C:" + File.separator, output.getLine());
    }
    
    @Test
    public void testLs() {
        runCommand("cd target/classes/com/serguei/telnet");
        runCommand("ls");
        Assert.assertTrue(output.included("commands"));
        Assert.assertTrue(output.included("Telnet.class"));
        runCommand("ls -l");
        Assert.assertTrue(output.included("commands"));
        Assert.assertTrue(output.included("Telnet.class"));
        runCommand("ls -1 *omm*");
        Assert.assertTrue(output.list.contains("commands"));
        Assert.assertFalse(output.list.contains("Telnet.class"));
        runCommand("ls commands/User*");
        Assert.assertTrue(output.included("UserSessionContext.class"));
        Assert.assertFalse(output.included("QuitCommand.class"));
    }
    
    @Test
    public void testMkDir() {
        File file = new File(rootDir + File.separator + "12345678");
        file.delete();
        runCommand("ls");
        Assert.assertFalse(output.included("12345678"));
        runCommand("mkdir 12345678");
        runCommand("ls");
        Assert.assertTrue(output.included("12345678"));
    }
    
    @Test
    public void testQuit() {
        runCommand("quit");
        Assert.assertTrue(output.included("Quitting"));
    }
    
    @Test
    public void testWrongCommand() {
        runCommand("wrongCommand");
        Assert.assertTrue(output.included("Wrong Command"));
    }
    
}
