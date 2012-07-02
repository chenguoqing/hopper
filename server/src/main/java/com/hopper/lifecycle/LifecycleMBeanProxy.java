package com.hopper.lifecycle;

import com.hopper.utils.Constants;
import org.apache.commons.modeler.Registry;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * The sub class of LifecycleMBeanProxy represents a MBean, it will register current component when initializing
 */
public abstract class LifecycleMBeanProxy extends LifecycleProxy implements MBeanRegistration {
    /**
     * domain for current component
     */
    private String domain;
    /**
     * Object name for current component
     */
    private ObjectName objectName;
    /**
     * Singleton MBeanServer reference
     */
    protected MBeanServer mBeanServer;

    @Override
    protected void doInit() throws Exception {
        if (objectName == null) {
            this.mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
            this.domain = getDomain();
            this.objectName = createObjectName(getObjectNameKeyProperties());
            register(this, objectName);
        }
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        this.mBeanServer = server;
        this.objectName = name;
        this.domain = name.getDomain();
        return name;
    }

    @Override
    public void postRegister(Boolean registrationDone) {
    }

    @Override
    public void preDeregister() throws Exception {
    }

    @Override
    public void postDeregister() {
    }

    /**
     * Register component to MBean Server with the special name properties
     *
     * @param component  the component instance will be registered
     * @param objectName ObjectName
     */
    protected final void register(Object component, ObjectName objectName) throws Exception {
        if (component == null || objectName == null) {
            throw new NullPointerException("component and objectName be null.");
        }

        Registry.getRegistry(null, null).registerComponent(component, objectName, null);
    }

    @Override
    protected void doShutdown() throws Exception {
        if (objectName != null) {
            Registry.getRegistry(null, null).unregisterComponent(objectName);
        }
    }

    /**
     * Create a {@link ObjectName} instance by domain and user-supported name properties
     */
    protected ObjectName createObjectName(String nameProperties) throws MalformedObjectNameException {
        String domain = getDomain();
        StringBuilder sb = new StringBuilder(domain);
        sb.append(':').append(nameProperties);

        return new ObjectName(sb.toString());
    }

    /**
     * Allow sub-classes to specify the key properties component of the {@link ObjectName} that will be used to
     * register this component.
     *
     * @return The string representation of the key properties component of the
     *         desired {@link ObjectName}
     */
    protected abstract String getObjectNameKeyProperties();

    /**
     * Obtain the domain under which this component will be / has been
     * registered.
     */
    public final String getDomain() {
        if (domain == null) {
            domain = getDomainInternal();
        }

        if (domain == null) {
            domain = Constants.DEFAULT_MBEAN_DOMAIN;
        }

        return domain;
    }

    /**
     * Method implemented by sub-classes to identify the domain in which MBeans
     * should be registered.
     *
     * @return The name of the domain to use to register MBeans.
     */
    protected String getDomainInternal() {
        return Constants.DEFAULT_MBEAN_DOMAIN;
    }
}
