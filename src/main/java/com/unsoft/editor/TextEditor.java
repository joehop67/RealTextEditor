package com.unsoft.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
//import java.util.ConcurrentModificationException;
import javax.swing.*;

import com.alee.laf.*;
//import com.jediterm.terminal.emulator.ColorPalette;
//import com.jediterm.terminal.emulator.mouse.MouseFormat;
//import com.jediterm.terminal.emulator.mouse.MouseMode;
//import com.jediterm.terminal.model.StyleState;
//import com.jediterm.terminal.model.TerminalTextBuffer;
//import com.jediterm.terminal.ui.*;
//import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
//import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.unsoft.editor.com.unsoft.editor.files.CreateChildNodes;
import com.unsoft.editor.com.unsoft.editor.files.FileNode;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.fife.rsta.ac.*;
import org.fife.rsta.ac.c.CLanguageSupport;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
//import com.bulenkov.darcula.*;
//import com.grahamedgecombe.jterminal.*;
//import com.jediterm.terminal.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.tools.*;

import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_C;
import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JAVA;

public class TextEditor{
    public static JFrame frame = new JFrame();
    private static JTabbedPane pane = new JTabbedPane();
    private RSyntaxTextArea area = new RSyntaxTextArea(20,120);
    FileNameExtensionFilter filter = new FileNameExtensionFilter("java Files", "java", "xml");
    private JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    private String currentFile = "untitled";
    private boolean changed = false;
    private JTree projectDir;
    private DefaultMutableTreeNode node;

    public TextEditor(){
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
        area.setCodeFoldingEnabled(true);
        RTextScrollPane scroll = new RTextScrollPane(area, true);
        frame.add(pane, BorderLayout.CENTER);
        pane.addTab(currentFile, scroll);
        setTheme(area);

        initTreeView();

        LanguageSupportFactory lsf = LanguageSupportFactory.get();
        LanguageSupport support = lsf.getSupportFor(SYNTAX_STYLE_JAVA);

        initEditor(area, lsf);


        JMenuBar JMB = new JMenuBar();
        JMenu file = new JMenu("file");
        JMenu edit = new JMenu("edit");
//        JMenu dir = new JMenu("dir");
        //JMenu term = new JMenu("Term");
        JMB.add(file);
//        JMB.add(dir);
        //JMB.add(term);
        frame.setJMenuBar(JMB);

//        dir.add(SetDir);
        //term.add(Term);

        file.add(New);
        file.add(Open);
        file.add(Save);
        file.add(SaveAs);
        file.add(Quit);
        file.addSeparator();
        file.add(Compile);
        file.add(BuildAnt);

        for(int i = 0; i < 4; i++)
            file.getItem(i).setIcon(null);

        edit.add(Cut);
        edit.add(Copy);
        edit.add(Paste);

        edit.getItem(0).setText("Cut");
        edit.getItem(1).setText("Copy");
        edit.getItem(2).setText("Paste");

        JToolBar tool = new JToolBar();
        frame.add(tool, BorderLayout.NORTH);
        tool.add(New);
        tool.add(Open);
        tool.add(Save);
        tool.addSeparator();

        JButton cut = tool.add(Cut), cop = tool.add(Copy), pas = tool.add(Paste);

        cut.setText(null);
        cut.setIcon(new ImageIcon("images/cut.gif"));

        cop.setText(null);
        cop.setIcon(new ImageIcon("images/copy.gif"));

        pas.setText(null);
        pas.setIcon(new ImageIcon("images/paste.gif"));

        Save.setEnabled(false);
        SaveAs.setEnabled(false);

        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.pack();
        area.addKeyListener(kl);
        frame.setTitle(currentFile);
        frame.setVisible(true);

        pane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = 0;
                if(e.getSource() instanceof JTabbedPane){
                    JTabbedPane jTabbedPane = (JTabbedPane) e.getSource();
                    index = jTabbedPane.getSelectedIndex();
                    currentFile = jTabbedPane.getTitleAt(index);
                    System.out.println(currentFile);
                }
            }
        });
    }

    private KeyListener kl = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            changed = true;
            Save.setEnabled(true);
            SaveAs.setEnabled(true);
        }
    };

//    Action Help = new AbstractAction() {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            dialog.showDialog(frame, "This is a test");
//        }
//    };
//    TODO: Lets add this terminal in somehow
//    Action Term = new AbstractAction("Term") {
//        public void actionPerformed(ActionEvent e) {
//            System.out.println("Test");
//            JFrame term = new JFrame();
//            JediTermWidget em = new JediTermWidget(sp);
//            term.setSize(300, 300);
//            term.add(em);
//            term.setVisible(true);
//        }
//    };

//    Action SetDir = new AbstractAction() {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            new OnOpenDialog();
//        }
//    };

    Action New = new AbstractAction("New", new ImageIcon("images/new.gif")) {
        public void actionPerformed(ActionEvent e) {
            saveOld();
            if(dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
                createFile(dialog.getSelectedFile().getAbsolutePath());
            }
        }
    };

    Action Compile = new AbstractAction("Compile") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Compile();
        }
    };

    Action BuildAnt = new AbstractAction("Build Ant") {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildAnt();
        }
    };

    Action Open = new AbstractAction("Open", new ImageIcon("images/open.gif")) {
        public void actionPerformed(ActionEvent e) {
            saveOld();
            dialog.setFileFilter(filter);
            if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                readInFile(dialog.getSelectedFile().getAbsolutePath());
            }
            SaveAs.setEnabled(true);
        }
    };

    Action Save = new AbstractAction("Save", new ImageIcon("images/save.gif")) {
        public void actionPerformed(ActionEvent e) {
            if(!currentFile.equals("untitled")){
                saveFile(currentFile);
            } else {
                saveFileAs();
            }
        }
    };

    Action SaveAs = new AbstractAction("Save as") {
        public void actionPerformed(ActionEvent e) {
            saveFileAs();
        }
    };

    Action Quit = new AbstractAction("Quit") {
        public void actionPerformed(ActionEvent e) {
            saveOld();
            System.exit(0);
        }
    };

    ActionMap m = area.getActionMap();
    Action Cut = m.get(DefaultEditorKit.cutAction);
    Action Copy = m.get(DefaultEditorKit.copyAction);
    Action Paste = m.get(DefaultEditorKit.pasteAction);

    private void saveFileAs(){
        if(dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            saveFile(dialog.getSelectedFile().getAbsolutePath());
    }

    private void saveOld(){
        if(changed){
            if(JOptionPane.showConfirmDialog(frame, "Would you like to save " + currentFile + "?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                saveFile(currentFile);
        }
    }


    private void readInFile(String filename){
        try{
            File fileCheck = new File(filename);
            if (fileCheck.isDirectory()){
                JOptionPane.showMessageDialog(frame, "Cannot open directory in editor");
                return;
            } else {
                FileReader r = new FileReader(filename);
                if (currentFile == "untitled" && changed == false){
                    area.read(r, null);
                    pane.setTitleAt(0, filename);
                } else {
                    newTab(r, filename);
                }
                r.close();
                currentFile = filename;
                frame.setTitle(currentFile);
                changed = false;
                JOptionPane.showMessageDialog(frame, checkFileType(filename));
            }
        } catch (IOException e){
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(frame, "Editor cannot find file called " + filename);
        }
    }

    private void saveFile(String filename){
        try{
            FileWriter w = new FileWriter(filename);
            area.write(w);
            w.close();
            currentFile = filename;
            frame.setTitle(currentFile);
            changed = false;
            Save.setEnabled(false);
        }catch(IOException e){
        }
    }

    private void createFile(String path){
        try {
            File file = new File(path);

            if (file.createNewFile()) {
                currentFile = path;
                frame.setTitle(currentFile);
                area.setText("");
                changed = false;
                Save.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(frame, "Could not create file" + path);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private String checkFileType(String filename){
        String filetype = "";
        Path path = Paths.get(filename);
        try {
            filetype = Files.probeContentType(path);
        } catch(IOException e){
            e.printStackTrace();
        }
        return filetype;
    }

    private void newTab(FileReader reader, String filename){
        RSyntaxTextArea textArea = new RSyntaxTextArea(20,120);
        RTextScrollPane scrollPane = new RTextScrollPane(textArea, true);
        textArea.setSyntaxEditingStyle(SYNTAX_STYLE_C);
        pane.addTab(filename, scrollPane);
        try {
            textArea.read(reader, null);
            setTheme(textArea);

        } catch(IOException e){
            e.printStackTrace();
        }
    }


    private void initEditor(RSyntaxTextArea area, LanguageSupportFactory lsf){
        lsf.register(area);
        area.setCaretPosition(0);
        area.requestFocusInWindow();
        area.setMarkOccurrences(true);
        ToolTipManager.sharedInstance().registerComponent(area);
    }

    private void initTreeView(){
        File fileRoot = new File(System.getProperty("user.dir"));
        node = new DefaultMutableTreeNode(new FileNode(fileRoot));
        DefaultTreeModel treeModel = new DefaultTreeModel(node);
        projectDir = new JTree(treeModel);
        projectDir.setShowsRootHandles(true);

        CreateChildNodes ccn = new CreateChildNodes(fileRoot, node);
        new Thread(ccn).start();

        JScrollPane projectDirView = new JScrollPane(projectDir);
        frame.add(projectDirView, BorderLayout.WEST);

        projectDir.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        projectDir.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                String jTreeVarSelectedPath = "";
                Object[] paths = projectDir.getSelectionPath().getPath();
                for (int i=0; i<paths.length; i++) {
                    jTreeVarSelectedPath += paths[i];
                    if (i+1 <paths.length ) {
                        jTreeVarSelectedPath += File.separator;
                    }
                }

                DefaultMutableTreeNode node = (DefaultMutableTreeNode)projectDir.getLastSelectedPathComponent();
                if(node != null){
                    Object nodeObject = node.getUserObject();

                    readInFile(fileRoot.getParent() + File.separator + jTreeVarSelectedPath);
                    //readInFile(System.getProperty("user.dir") + "/" + nodeObject);
                }
            }
        });
    }

    private void setTheme(RSyntaxTextArea textarea){
        try{
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textarea);
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    private void Compile(){
        File project = new File(System.getProperty("user.dir"));
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.lastIndexOf('.') > 0){
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex);

                    if(str.equals(".java")){
                        return true;
                    }
                }
                return false;
            }
        };
        File[] listOfFiles = project.listFiles(filter);
        String[] paths = new String[listOfFiles.length];
        for(int i = 0; i < listOfFiles.length; i++){
            String path = listOfFiles[i].getAbsolutePath();
            paths[i] = path;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(paths));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        boolean success = task.call();
        try{
            fileManager.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Success:" + success);
        System.out.println(diagnostics.getDiagnostics());
    }

    private void buildAnt(){
        ProcessBuilder pb = new ProcessBuilder();
        Map env = pb.environment();
        String path = env.get("ANT_HOME").toString();
        System.out.println(path);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.command(path + System.getProperty("file.separator")
                + "bin" + System.getProperty("file.separator") + "ant");
        try {
            Process p = pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
//        File buildFile = new File(System.getProperty("user.dir") + File.separator + "build.xml");
//        Project project = new Project();
//        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
//        project.init();
//        ProjectHelper helper = ProjectHelper.getProjectHelper();
//        project.addReference("ant.projectHelper", helper);
//        helper.parse(project, buildFile);
//        project.executeTarget(project.getDefaultTarget());
    }

    public static void main(String[] arg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WebLookAndFeel.install(true);
                new OnOpenDialog();
                //OnOpenDialog.openFrame.dispose();
                pane.updateUI();
            }
        });
    }


}
