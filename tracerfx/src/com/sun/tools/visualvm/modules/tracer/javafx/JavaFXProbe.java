/*
 *  Copyright 2007-2010 Sun Microsystems, Inc.  All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Sun designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Sun in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 *  CA 95054 USA or visit www.sun.com if you need additional information or
 *  have any questions.
 */
package com.sun.tools.visualvm.modules.tracer.javafx;

import com.sun.tools.visualvm.modules.tracer.ProbeItemDescriptor;
import com.sun.tools.visualvm.modules.tracer.TracerProbe;
import com.sun.tools.visualvm.modules.tracer.TracerProbeDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class JavaFXProbe extends TracerProbe {

    private static final Logger LOGGER = Logger.getLogger(JavaFXProbe.class.getName());

    private MBeanServerConnection connection;
    private ObjectName mbean;

    private final int valuesCount;
    private final String[] attrs;


    JavaFXProbe(int valuesCount, ProbeItemDescriptor[] itemDescriptors, String[] attrs) {
        super(itemDescriptors);
        this.valuesCount = valuesCount;
        this.attrs = attrs;
    }


    synchronized final void setConnection(MBeanServerConnection connection, ObjectName mbean) {
        this.connection = connection;
        this.mbean = mbean;
    }

    synchronized final void resetConnection() {
        connection = null;
        mbean = null;
    }


    public synchronized final long[] getItemValues(long timestamp) {
        if (connection != null) {
            try {
                List<Attribute> metrics =
                        connection.getAttributes(mbean, attrs).asList();
                return getValues(metrics);
            } catch (Throwable t) {
                LOGGER.log(Level.INFO, "Failed to read FX attributes", t); // NOI18N
            }
        }

        return new long[valuesCount];
    }

    abstract long[] getValues(List<Attribute> metrics);

}
