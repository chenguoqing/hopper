package com.hopper.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (args[0].equalsIgnoreCase("-start")) {
            if (args.length > 1) {
                System.setProperty("configFile", args[1]);
            }
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
            componentManager.start();
        } catch (Exception e) {
            logger.error("Failed to start hopper server", e);
        }
    }

    private static void shutdown(String[] args) {

    }

    private static void usage() {
        System.out.println("java Main [-start | -shutdown] | configuration file path");
    }
}
