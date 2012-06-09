package com.hopper.server;

/**
 * Component factory for singleton pattern.
 */
public class ComponentManagerFactory {

    private static ComponentManager componentManager;

    public static ComponentManager getComponentManager() {

        if (componentManager == null) {
            synchronized (ComponentManagerFactory.class) {
                if (componentManager == null) {
                    componentManager = new ComponentManager();
                }
            }
        }

        return componentManager;
    }
}
