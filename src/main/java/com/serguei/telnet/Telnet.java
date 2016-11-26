package com.serguei.telnet;

import java.io.IOException;

import com.serguei.telnet.sockets.ServerSocketListener;

/**
 * Main class of Telnet application
 * 
 * Currently it waits for one port only. It is possible to extend it to wait for different ports.
 * 
 * @author Serguei Poliakov
 *
 */
public class Telnet {

	// Currently one instance only. If more then one port is to be listened to, then this should be changed
	// to a collection of objects ServerSocketListener.
	private ServerSocketListener listener = null;

	/**
	 * Start thread listening to port
	 * @param port number
	 * @return true if started successfully
	 */
	public boolean start(int port) {
		listener = new ServerSocketListener(port);
		if (listener == null)
			return false;
		listener.start();
		return true;
	}

	/**
	 * Close all connections and stop all threads
	 */
	public void stop() {
      listener.stop();
	}
	
	/**
	 * Just wait until someone type a string with 'Q' in it so that we can stop the server
	 */
	private static void waitQuitCommand() {
		try {
			int ch;
			do {
			  ch = System.in.read();
			} while (ch != 'Q' && ch != 'q');
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
	    int portNo;
	    if (args.length > 0) {
    	    try {
        	    portNo = Integer.parseInt(args[0]);
    	    } catch (NumberFormatException e) {
    	        System.out.println("Wrong port number");
    	        return;
    	    }
	    }
	    else
	        portNo = 23;
	    Thread.currentThread().setName("Main Telnet Thread");
		Telnet telnet = new Telnet();
		if (telnet.start(portNo)) {
			System.out.println("Telnet server started");
			System.out.println("Enter Q to exit");
			Telnet.waitQuitCommand();
			System.out.println("Stopping...");
			telnet.stop();
            System.out.println("Stopped");
		}
		else
			System.out.println("Error starting Server Socket Listener");
	}
}
