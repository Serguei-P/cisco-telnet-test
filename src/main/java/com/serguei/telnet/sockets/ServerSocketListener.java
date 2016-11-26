package com.serguei.telnet.sockets;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.serguei.telnet.commands.Command;
import com.serguei.telnet.util.Log;

/**
 * @author Serguei Poliakov
 * 
 *         This class waits for new connections from telnet clients, dispatches
 *         incoming data processing and keeps track of opened connections
 * 
 *         This creates its own thread.
 * 
 */
public class ServerSocketListener implements Runnable {

    private final int port;
    private final int maxConnection = 25; // maximum number of allowed
                                          // connections
    private Thread thread;
    private ServerSocketChannel serverSocket;
    private volatile boolean isStopping = false;
    private volatile boolean isAvailable = false;
    private Selector selector;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Constructs a listener for a port
     * 
     * @param port
     *            - port number to wait for connections
     */
    public ServerSocketListener(int port) {
        this.port = port;
    }

    /**
     * Start waiting for connections
     */
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Kill all user connections, stop waiting for new connection and stop all
     * threads
     */
    public void stop() {
        // Volatile variable isAvailable set to false to prevent new connections
        isAvailable = false;
        // Volatile variable isStopping will tell the main loop to stop waiting
        isStopping = true;
        // We need to wake up the selector, or it will block forever
        if (selector != null)
            selector.wakeup();
        try {
            // Shutting down executor that controls command processing threads
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (selector != null) {
            Set<SelectionKey> keySet = selector.keys();
            for (SelectionKey key : keySet) {
                if (key.channel() instanceof SocketChannel)
                    closeConnection(key, (SocketChannel) key.channel());
            }
        }
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            Log.out("serverSocket close failure: " + e.getMessage());
        }
    }

    /**
     * Calculates number of active connections (could be higher then real as
     * selection keys are not removed immediately)
     * 
     * @return
     */
    private int sessionCount() {
        if (selector != null)
            // one of the keys in selector is SocketChannelServer
            return selector.keys().size() - 1;
        else
            return 0;
    }

    /**
     * If we need to check if connection is to be allowed, this is a place for
     * it
     * 
     * @param socket
     * @return
     * @throws IOException
     */
    private boolean checkAllowed(SocketChannel socket) throws IOException {
        if (sessionCount() >= maxConnection) {
            socket.close();
            return false;
        }
        return true;
    }

    /**
     * Main loop - waiting for new events from the port
     */
    @Override
    public void run() {
        try {
        	Thread.currentThread().setName("ServerSocketListener thread");
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(port));
        } catch (IOException e) {
            // This can happen if the port is used already or is not allowed.
            // The error message should be more informative then the one in the
            // next try-catch block (which is for the same exception).
            Log.out("Error binding to port " + port + " " + e.getMessage());
            return;
        }
        try {
            selector = SelectorProvider.provider().openSelector();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            isAvailable = true;
            do {
                try {
                    selector.select(); // this blocks until something happens
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext() && !isStopping) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isValid()) {
                            // Processing accepting and reading. Writing is done
                            // in different threads.
                            if (key.isAcceptable() && isAvailable) {
                                ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                                acceptConnection(key, serverSocket);
                            } else if (key.isReadable() && !isStopping) {
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                readConnection(key, socketChannel);
                            }
                        }
                    }
                } catch (SocketException e) {
                    if (isStopping) {
                        Log.out("ServerSocket closed " + e.getMessage());
                    } else {
                        Log.out("SocketExcepton " + e.getMessage());
                    }
                }
            } while (!isStopping); // isStopping is volatile
        } catch (IOException e) {
            Log.out("IOException " + e.getMessage());
        }
    }

    /**
     * This processes request from a client to accept connection
     * 
     * @param key
     * @param serverSocket
     * @throws IOException
     */
    private void acceptConnection(SelectionKey key, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel socket = serverSocket.accept();
        if (checkAllowed(socket)) {
            UserSession userSession = new UserSession(socket);
            socket.configureBlocking(false);
            SelectionKey newKey = socket.register(selector, SelectionKey.OP_READ);
            newKey.attach(userSession);
            userSession.welcome();
            Log.out("New connection. Total=" + sessionCount());
        } else {
            Log.out("Connection refused");
            socket.close();
        }
    }

    /**
     * Close a connection
     * 
     * @param key
     * @param socketChannel
     */
    private void closeConnection(SelectionKey key, SocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException e) {
            Log.out("Close socketChannel exception " + e.getMessage());
        }
        key.cancel();
    }

    /**
     * This processes an even when income data appears on connection. Data is
     * read in this thread and initial command parsing takes place here, while
     * the actual processing takes place in a different thread taken from a
     * thread pool created by Executor
     * 
     * @param key
     * @param socketChannel
     */
    private void readConnection(SelectionKey key, SocketChannel socketChannel) {
        UserSession userSession = (UserSession) key.attachment();
        if (userSession != null) {
            Command command = userSession.processInput();
            if (userSession.isFinished()) {
                // user broke the connection
                closeConnection(key, socketChannel);
                Log.out("Connection closed.");
            } else if (command != null) {
                if (command.getName().equals("quit")) {
                    // user requested the connection to stop
                    try {
                        userSession.sayGoodBye();
                    } catch (IOException e) {
                        Log.out("sayGoodBuy " + e.getMessage());
                    }
                    closeConnection(key, socketChannel);
                    Log.out("User quit");
                } else
                    // this is where the actual processing of the command will
                    // take place
                    executor.execute(command);
            }
        } else
            Log.out("No user session found");
    }

}
