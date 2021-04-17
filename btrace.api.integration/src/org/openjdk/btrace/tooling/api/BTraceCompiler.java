package org.openjdk.btrace.tooling.api;

import java.io.Writer;

/**
 *
 * @author Jaroslav Bachorik yardus@netbeans.org
 */
public abstract class BTraceCompiler {
    public byte[] compile(String source, String classPath) {
        return compile(source, classPath, null);
    }

    abstract public byte[] compile(String source, String classPath, Writer errorWriter);

    abstract public String getAgentJarPath();
    abstract public String getClientJarPath();
    abstract public String getToolsJarPath();
}
