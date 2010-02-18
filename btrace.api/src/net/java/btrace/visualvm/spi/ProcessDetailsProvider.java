/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.spi;

import java.util.Properties;
import net.java.btrace.visualvm.spi.impl.NullProcessDetailsProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
abstract public class ProcessDetailsProvider {
    final static public ProcessDetailsProvider NULL = new NullProcessDetailsProvider();
    
    abstract public boolean canBeTraced(int pid);
    abstract public Properties getSystemProperties(int pid);

}
