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

import com.sun.tools.visualvm.modules.tracer.ItemValueFormatter;
import com.sun.tools.visualvm.modules.tracer.ProbeItemDescriptor;
import com.sun.tools.visualvm.modules.tracer.TracerProbeDescriptor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.management.Attribute;
import javax.management.openmbean.CompositeData;
import javax.swing.Icon;

/**
 *
 * @author Jiri Sedlacek
 */
class JavaFXSyncCallsProbe extends JavaFXProbe {

    private static final String NAME = "Synchronization calls";
    private static final String DESCR = "Monitors amount of synchronization calls";
    private static final int POSITION = 50;


    JavaFXSyncCallsProbe() {
        super( 1, createItemDescriptors(), createAttrs());
    }


    long[] getValues(List<Attribute> metrics) {
        return new long[] {
            Math.max(((Long)metrics.get(0).getValue()).longValue(), 0)
        };
    }
    

    static final TracerProbeDescriptor createDescriptor(Icon icon, boolean available) {
        return new TracerProbeDescriptor(NAME, DESCR, icon, POSITION, available);
    }

    private static final ProbeItemDescriptor[] createItemDescriptors() {
        return new ProbeItemDescriptor[] {
            ProbeItemDescriptor.lineItem("Synchronization calls", "Monitors amount of synchronization calls", ItemValueFormatter.DEFAULT_DECIMAL, 0, ProbeItemDescriptor.MAX_VALUE_UNDEFINED)
        };
    }

    private static final String[] createAttrs() {
        return new String[] { "synchronizationTime" }; // NOI18N
    }

}
