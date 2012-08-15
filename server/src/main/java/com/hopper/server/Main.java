package com.hopper.server;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

/**
 * Hopper server entrance
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            usage();
            System.exit(-1);
        }

        if (args.length > 1) {
            System.setProperty("configFile", args[1]);
        }

        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        if (args[0].equalsIgnoreCase("-start")) {
            start(args);
        } else if (args[0].equalsIgnoreCase("-shutdown")) {
            shutdown(args);
        } else {
            usage();
            System.exit(-1);
        }
    }

    private static void start(String[] args) {
        ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        try {
            componentManager.initialize();
            componentManager.start();
        } catch (Exception e) {
            logger.error("Failed to start hopper server", e);
        }
    }

    private static void shutdown(String[] args) {
        ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        try {
            componentManager.initialize();

            Socket socket = new Socket("127.0.0.1", componentManager.getGlobalConfiguration().getShutdownPort());
            socket.setSoLinger(true, 1000);
            socket.getOutputStream().write(componentManager.getGlobalConfiguration().getShutdownCommand().getBytes());
            socket.close();
        } catch (Exception e) {
            logger.error("Failed to shutdown hopper server", e);
        }
    }

    private static void usage() {
        System.out.println("java Main [-start | -shutdown] | configuration file path");
    }
}
