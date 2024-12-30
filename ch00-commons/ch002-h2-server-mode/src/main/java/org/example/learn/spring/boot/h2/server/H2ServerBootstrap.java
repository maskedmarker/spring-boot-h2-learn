package org.example.learn.spring.boot.h2.server;

import org.h2.tools.Server;

/**
 * org.h2.tools.Server是h2的启动类,这里简单wrap一层是为说明可以通过编程方式独立启动h2
 */
public class H2ServerBootstrap {

    public static void main(String[] args) {
        try {
            // Start H2 in TCP Server mode
            Server h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
            h2Server.start();
            System.out.println("H2 Database Server started and running at: " + h2Server.getURL());

            // Keep the server running until terminated
            System.out.println("Press Ctrl+C to stop the server.");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Error starting H2 server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
