package org.openjdk.btrace.tooling.api;

/**
 *
 * @author Jaroslav Bachorik yardus@netbeans.org
 */
abstract public class BTraceSettings {
    abstract public boolean isDebugMode();
    abstract public String getDumpClassPath();
    abstract public boolean isDumpClasses();
    public String getStatsd() {
        return null;
    }
}
