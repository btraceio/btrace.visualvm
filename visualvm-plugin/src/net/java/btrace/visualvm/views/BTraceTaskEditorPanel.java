/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * BTraceTaskEditorPanel.java
 *
 * Created on Aug 28, 2008, 4:38:48 PM
 */

package net.java.btrace.visualvm.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import net.java.btrace.visualvm.ui.components.EventChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import net.java.btrace.visualvm.api.BTraceTask;
import net.java.btrace.visualvm.api.BTraceTask.StateListener;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@sun.com>
 */
public class BTraceTaskEditorPanel extends javax.swing.JPanel implements StateListener {
    final private static Logger LOGGER = Logger.getLogger(BTraceTaskEditorPanel.class.getName());

    final private static String EDITOR_LAST_SCRIPT = "editor.lastOpened";

    final private static String SCRIPT_TEMPLATE = "/* BTrace Script Template */\n" +
            "import com.sun.btrace.annotations.*;\n" +
            "import static com.sun.btrace.BTraceUtils.*;\n\n" +
            "@BTrace\n" +
            "public class TracingScript {\n" +
            "\t/* put your code here */\n" +
            "}";

    final private Preferences prefs = NbPreferences.forModule(BTraceTaskEditorPanel.class);

    private String lastOpenPath = null;
    private String lastSavePath = null;

    private BTraceTask task = null;
    final private DocumentListener documentListener = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (task == null) return;
            task.setScript(scriptEditor.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (task == null) return;
            task.setScript(scriptEditor.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (task == null) return;
            task.setScript(scriptEditor.getText());
        }
    };

    /** Creates new form BTraceTaskEditorPanel */
    public BTraceTaskEditorPanel(BTraceTask task) {
        this.task = task;
        task.addStateListener(this);

        initComponents();

        initializeEditor();
    }

    synchronized private void initializeEditor() {
        boolean initialized = false;

        scriptEditor.setContentType("text/x-btrace");

        scriptEditor.getDocument().addDocumentListener(documentListener);

        if (scriptEditor.getText().isEmpty()) {
            if (getLastScriptPath() != null) {
                try {
                    loadScript(new File(getLastScriptPath()));
                    unsafeCheck.setSelected(task.isUnsafe());
                    updateUnsafeCheck();
                    initialized = true;
                } catch (FileNotFoundException ex) {
                    // ignore
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (!initialized) {
                scriptEditor.setText(SCRIPT_TEMPLATE);
            }
        }
    }

    @Override
    public void stateChanged(BTraceTask.State state) {
        LOGGER.log(Level.FINEST, "BTrace task state changed to {0}", state);
        switch (state) {
            case FINISHED:
            case NEW: {
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnEvent.setEnabled(false);
                break;
            }
            case STARTING: {
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);
                btnEvent.setEnabled(false);
                break;
            }
            case RUNNING: {
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnEvent.setEnabled(task.hasEvents());
                break;
            }
        }
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        scriptEditor = new javax.swing.JEditorPane();
        scriptEditor.getDocument().addDocumentListener(documentListener);
        toolBar = new javax.swing.JToolBar();
        openScript = new javax.swing.JButton();
        saveScript = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnEvent = new javax.swing.JButton();
        toolBarSeparator = new javax.swing.JPanel(new FlowLayout(FlowLayout.LEADING, 0,0)) {
            public Dimension getPreferredSize() {
                if (isGTKLookAndFeel() || isNimbusLookAndFeel()) {
                    int currentWidth = toolBar.getSize().width;
                    int minimumWidth = toolBar.getMinimumSize().width;
                    int extraWidth = currentWidth - minimumWidth;
                    return new Dimension(Math.max(extraWidth, 0), 0);
                } else {
                    return super.getPreferredSize();
                }
            }
        }
        ;
        unsafeCheck = new javax.swing.JCheckBox();

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        setPreferredSize(new java.awt.Dimension(400, 300));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setOpaque(false);

        scriptEditor.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        scriptEditor.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scriptEditor.setContentType(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.scriptEditor.contentType")); // NOI18N
        scriptEditor.setMaximumSize(new java.awt.Dimension(800, 600));
        scriptEditor.setMinimumSize(new java.awt.Dimension(200, 100));
        scriptEditor.setOpaque(false);
        scriptEditor.setPreferredSize(new java.awt.Dimension(200, 100));
        jScrollPane1.setViewportView(scriptEditor);

        toolBar.setBorder(null);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(false);
        toolBar.setOpaque(false);

        openScript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/java/btrace/visualvm/resources/openFIle.png"))); // NOI18N
        openScript.setMnemonic('o');
        openScript.setText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.openScript.text")); // NOI18N
        openScript.setFocusable(false);
        openScript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openScriptActionPerformed(evt);
            }
        });
        toolBar.add(openScript);

        saveScript.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/java/btrace/visualvm/resources/saveFile.png"))); // NOI18N
        saveScript.setMnemonic('a');
        saveScript.setText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.saveScript.text")); // NOI18N
        saveScript.setFocusable(false);
        saveScript.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveScriptActionPerformed(evt);
            }
        });
        toolBar.add(saveScript);
        toolBar.add(jSeparator2);

        btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/java/btrace/visualvm/resources/startScript.png"))); // NOI18N
        btnStart.setMnemonic('S');
        btnStart.setText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.btnStart.text")); // NOI18N
        btnStart.setFocusable(false);
        btnStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        toolBar.add(btnStart);

        btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/java/btrace/visualvm/resources/stopScript.png"))); // NOI18N
        btnStop.setMnemonic('t');
        btnStop.setText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.btnStop.text")); // NOI18N
        btnStop.setEnabled(false);
        btnStop.setFocusable(false);
        btnStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });
        toolBar.add(btnStop);
        toolBar.add(jSeparator1);

        btnEvent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/net/java/btrace/visualvm/resources/event.png"))); // NOI18N
        btnEvent.setMnemonic('e');
        btnEvent.setText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.btnEvent.text")); // NOI18N
        btnEvent.setEnabled(false);
        btnEvent.setFocusable(false);
        btnEvent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEventActionPerformed(evt);
            }
        });
        toolBar.add(btnEvent);

        toolBarSeparator.setOpaque(false);
        toolBar.add(toolBarSeparator);

        unsafeCheck.setMnemonic('u');
        unsafeCheck.setText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.unsafeCheck.text")); // NOI18N
        unsafeCheck.setToolTipText(org.openide.util.NbBundle.getMessage(BTraceTaskEditorPanel.class, "BTraceTaskEditorPanel.unsafeCheck.toolTipText")); // NOI18N
        unsafeCheck.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        unsafeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unsafeCheckActionPerformed(evt);
            }
        });
        toolBar.add(unsafeCheck);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEventActionPerformed
        Set<String> events = task.getNamedEvents();
        if (events.isEmpty()) {
            task.sendEvent();
            return;
        }
        if (events.size() == 1) {
            task.sendEvent(events.iterator().next());
            return;
        }

        EventChooser chooser = EventChooser.getChooser(task.getNamedEvents(), task.hasAnonymousEvents());
        NotifyDescriptor nd = new DialogDescriptor.Confirmation(chooser, "Select the event to send", NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            String event = chooser.getSelectedEvent();
            if (event != null && !event.isEmpty()) {
                task.sendEvent(event);
            } else {
                task.sendEvent();
            }
        }
}//GEN-LAST:event_btnEventActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        task.stop();
}//GEN-LAST:event_btnStopActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        task.start();
}//GEN-LAST:event_btnStartActionPerformed

    private void saveScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveScriptActionPerformed
        JFileChooser chooser = lastSavePath != null ? new JFileChooser(lastSavePath) : new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return (f.getName().endsWith(".java"));
            }

            @Override
            public String getDescription() {
                return "BTrace source files (*.java)";
            }
        });
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File script = chooser.getSelectedFile();
            lastSavePath = script.getParent();
            setLastScriptPath(chooser.getSelectedFile().getAbsolutePath());

            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile())));
                bw.write(scriptEditor.getText(), 0, scriptEditor.getText().length());
                saveMetadata(script);
            } catch (IOException iOException) {
                // TODO
                iOException.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
}//GEN-LAST:event_saveScriptActionPerformed

    private void openScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openScriptActionPerformed
        JFileChooser chooser = lastOpenPath != null ? new JFileChooser(lastOpenPath) : new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return (f.getName().endsWith(".java"));
            }

            @Override
            public String getDescription() {
                return "BTrace source files (*.java)";
            }
        });
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                loadScript(chooser.getSelectedFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
}//GEN-LAST:event_openScriptActionPerformed

    private void unsafeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unsafeCheckActionPerformed
        updateUnsafeCheck();
    }//GEN-LAST:event_unsafeCheckActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEvent;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JButton openScript;
    private javax.swing.JButton saveScript;
    private javax.swing.JEditorPane scriptEditor;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel toolBarSeparator;
    private javax.swing.JCheckBox unsafeCheck;
    // End of variables declaration//GEN-END:variables


    private void loadScript(File script) throws IOException {
        lastOpenPath = script.getParent();
        setLastScriptPath(script.getAbsolutePath());

        char[] buffer = new char[2048];
        StringBuilder sb = new StringBuilder();
        int readCount = -1;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(script)));
            do {
                readCount = br.read(buffer);
                if (readCount > -1) {
                    sb.append(buffer, 0, readCount);
                }
            } while (readCount > -1);
            loadMetadata(script);
            scriptEditor.setText(sb.toString());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException iOException) {
                    // ignore
                }
            }
        }
    }

    private void loadMetadata(File script) {
        String path = script.getAbsolutePath();
        String metadata = path;
        if (path.endsWith(".java")) {
            metadata = path.replace(".java", ".btmeta");
        } else {
            metadata = path + ".btmeta";
        }

        Properties p = new Properties();

        try {
            p.load(new InputStreamReader(new FileInputStream(metadata)));

            String classPath = p.getProperty("class-path", "");
            StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
            while (st.hasMoreTokens()) {
                task.addCPEntry(st.nextToken());
            }
//            boolean unsafe = Boolean.parseBoolean(p.getProperty("unsafe", "false"));
//            task.setUnsafe(unsafe);
        } catch (IOException iOException) {
            // ignore; simply don't add a custom classpath as it doesn't exist
        }
    }

    private void saveMetadata(File script) {
        String path = script.getAbsolutePath();
        String metadata = path;
        if (path.endsWith(".java")) {
            metadata = metadata.replace(".java", ".btmeta");
        } else {
            metadata = metadata + ".btmeta";
        }

        Properties p = new Properties();

        try {
            p.setProperty("class-path", task.getClassPath());
            p.setProperty("unsafe", Boolean.toString(task.isUnsafe()));
            
            p.store(new OutputStreamWriter(new FileOutputStream(metadata)), "BTrace metadata");
        } catch (IOException iOException) {
            // ignore; simply don't add a custom classpath as it doesn't exist
        }
    }

    private static boolean isGTKLookAndFeel() {
        // is current L&F some kind of GTKLookAndFeel?
        return UIManager.getLookAndFeel().getID().equals("GTK"); //NOI18N
    }

    /** Determines if current L&F is Nimbus */
    private static boolean isNimbusLookAndFeel() {
        // is current L&F Nimbus?
        return UIManager.getLookAndFeel().getID().equals("Nimbus"); //NOI18N
    }

    private void updateUnsafeCheck() {
        if (unsafeCheck.isSelected()) {
            unsafeCheck.setForeground(Color.RED);
        } else {
            unsafeCheck.setForeground(Color.BLACK);
        }
//        task.setUnsafe(unsafeCheck.isSelected());
    }

    public String getLastScriptPath() {
        return prefs.get(EDITOR_LAST_SCRIPT, null);
    }

    public void setLastScriptPath(String path) {
        prefs.put(EDITOR_LAST_SCRIPT, path);
    }

}
