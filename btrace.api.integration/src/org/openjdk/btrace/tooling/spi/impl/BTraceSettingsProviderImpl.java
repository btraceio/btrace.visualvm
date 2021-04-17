/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openjdk.btrace.tooling.spi.impl;

import org.openjdk.btrace.tooling.api.BTraceSettings;

/**
 *
 * @author Jaroslav Bachorik yardus@netbeans.org
 */
final public class BTraceSettingsProviderImpl implements org.openjdk.btrace.tooling.spi.BTraceSettingsProvider {
    private final BTraceSettings bs = new BTraceSettings() {

        @Override
        public boolean isDebugMode() {
            return false;
        }

        @Override
        public String getDumpClassPath() {
            return "";
        }

        @Override
        public boolean isDumpClasses() {
            return false;
        }
    };

    @Override
    public BTraceSettings getSettings() {
        return bs;
    }

}
