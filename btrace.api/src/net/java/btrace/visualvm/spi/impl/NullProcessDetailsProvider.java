/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.spi.impl;

import java.util.Properties;
import net.java.btrace.visualvm.spi.ProcessDetailsProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
public class NullProcessDetailsProvider extends ProcessDetailsProvider {
    final private Properties nullProperties = new Properties();

    @Override
    public boolean canBeTraced(int pid) {
        return false;
    }

    @Override
    public Properties getSystemProperties(int pid) {
        return nullProperties;
    }



}
