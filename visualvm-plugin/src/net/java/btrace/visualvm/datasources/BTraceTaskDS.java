/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.datasources;

import com.sun.btrace.api.BTraceEngine;
import com.sun.btrace.api.BTraceTask;
import com.sun.btrace.api.BTraceTask.State;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.java.btrace.visualvm.impl.BTraceOutputRegistry;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
public class BTraceTaskDS extends DataSource {
    private BTraceTask task;

    final private AtomicReference<PrintWriter> writerRef = new AtomicReference<PrintWriter>();

    final private static BTraceEngine engine = BTraceEngine.newInstance();
    
    private static final HashMap<Class< ? >, String> typeFormats = new HashMap<Class< ? >, String>();
    static {
        typeFormats.put(Integer.class, "%15d");
        typeFormats.put(Short.class, "%15d");
        typeFormats.put(Byte.class, "%15d");
        typeFormats.put(Long.class, "%15d");
        typeFormats.put(BigInteger.class, "%15d");
        typeFormats.put(Double.class, "%15f");
        typeFormats.put(Float.class, "%15f");
        typeFormats.put(BigDecimal.class, "%15f");
        typeFormats.put(String.class, "%-50s");
    }
    
    /**
     * @param object
     * @return
     */
    private static String getFormat(Object object) {
        if (object == null) {
            return "%-15s";
        }
        String usedFormat = typeFormats.get(object.getClass());
        if (usedFormat == null) {
            return "%-15s";
        }
        return usedFormat;
    }

    /**
     * Takes a multi-line value, prefixes and appends a blank line, and inserts tab characters at the start of every
     * line. This is derived from how dtrace displays stack traces, and it makes for pretty readable output.
     */
    private static String reformatMultilineValue(String value) {
        StringBuilder result = new StringBuilder();
        result.append("\n");
        for (String line : value.split("\n")) {
            result.append("\t").append(line);
            result.append("\n");
        }
        return result.toString();
    }

    public BTraceTaskDS(Application app) {
        super(app);
        setVisible(false);
        task = engine.createTask(app.getPid());
        task.addMessageDispatcher(new BTraceTask.MessageDispatcher() {
            
            @Override
            public void onPrintMessage(String message) {
                getWriter().print(message);

            }

            @Override
            public void onError(Throwable cause) {
                cause.printStackTrace(getWriter());
            }

            @Override
            public void onGrid(String name, List<Object[]> data) {
                getWriter().println("* " + name + " *");
                for (Object[] dataRow : data) {
                    // Convert histograms to strings, and pretty-print multi-line text
                    Object[] printRow = dataRow.clone();
                    for (int i = 0; i < printRow.length; i++) {
                        if (printRow[i] == null) {
                            printRow[i] = "<null>";
                        }
                        try {
                            Method printMethod = printRow[i].getClass().getMethod("print", PrintWriter.class);
                            if (printMethod != null) {
                                StringWriter buffer = new StringWriter();
                                PrintWriter writer = new PrintWriter(buffer);
                                printMethod.invoke(dataRow, writer);
                                writer.flush();
                                printRow[i] = buffer.toString();
                                continue;
                            }
                        } catch (NoSuchMethodException noSuchMethodException) {
                        } catch (SecurityException securityException) {
                        } catch (IllegalAccessException illegalAccessException) {
                        } catch (IllegalArgumentException illegalArgumentException) {
                        } catch (InvocationTargetException invocationTargetException) {
                        }
                        if (printRow[i] instanceof String) {
                            String value = (String) printRow[i];
                            if (value.contains("\n")) {
                                printRow[i] = reformatMultilineValue(value);
                            }
                        }
                    }

                    // Format the text
                    String usedFormat = null; //this.format;
                    if (usedFormat == null || usedFormat.length() == 0) {
                        StringBuilder buffer = new StringBuilder();
                        for (int i = 0; i < printRow.length; i++) {
                            buffer.append("  ");
                            buffer.append(getFormat(printRow[i]));
                        }
                        usedFormat = buffer.toString();
                    }
                    String line = String.format(usedFormat, printRow);

                    getWriter().println(line);
                }
            }

            @Override
            public void onNumberMap(String name, Map<String, ? extends Number> data) {
                getWriter().println("* " + name + " *");
                for(Map.Entry<String, ? extends Number> e : data.entrySet()) {
                    getWriter().println(e.getKey() + " = " + e.getValue());
                }
            }

            @Override
            public void onNumberMessage(String name, Number value) {
                getWriter().println(name + " = " +  value);
            }

            @Override
            public void onStringMap(String name, Map<String, String> data) {
                getWriter().println("* " + name + " *");
                for(Map.Entry<String, String> e : data.entrySet()) {
                    getWriter().println(e.getKey() + " = " + e.getValue());
                }
            }
        });
        task.addStateListener(new BTraceTask.StateListener() {

            @Override
            public void stateChanged(State state) {
                switch (state) {
                    case STARTING: {
                        getWriter().println("* Starting BTrace task");
                        break;
                    }
                    case COMPILING: {
                        getWriter().println("** Compiling the BTrace script ...");
                        getWriter().flush();
                        break;
                    }
                    case FAILED: {
                        getWriter().println("!!! Error occured");
                        break;
                    }
                    case COMPILED: {
                        getWriter().println("*** Compiled");
                        break;
                    }
                    case INSTRUMENTING: {
                        getWriter().println("** Instrumenting " + task.getInstrClasses() + " classes ...");
                        break;
                    }
                    case RUNNING: {
                        getWriter().println("*** Done");
                        getWriter().println("** BTrace up&running\n");
                        break;
                    }
                    case FINISHED: {
                        getWriter().println("** BTrace has stopped");
                        break;
                    }
                }
            }
        });
    }

    public BTraceTask getTask() {
        return task;
    }

    public void setWriter(Writer writer) {
        writerRef.set((writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer));
        BTraceOutputRegistry.getInstance().registerOuptut(task, writerRef.get());
    }

    private PrintWriter getWriter() {
        return writerRef.get();
    }

    @Override
    public String toString() {
        return task.toString();
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return task.equals(obj);
    }
}
