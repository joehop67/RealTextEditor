package com.unsoft.editor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.util.ConcurrentModificationException;
import javax.swing.*;
import javax.xml.soap.Text;

import com.alee.laf.*;
import com.unsoft.editor.TextEditor;


public class OnOpenDialog{
    public static JFrame openFrame = new JFrame();
    private JButton select = new JButton("...");
    private JTextField fileSelect = new JTextField();
    private JFileChooser setWorkingDir = new JFileChooser();
    private JButton confirm = new JButton("OK");


    public OnOpenDialog(){
        openFrame.setTitle("Unsoft Editor");
        openFrame.add(fileSelect, BorderLayout.EAST);
        openFrame.add(select, BorderLayout.WEST);
        openFrame.add(confirm, BorderLayout.SOUTH);
        fileSelect.setText(System.getProperty("user.dir"));
        openFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        openFrame.pack();
        openFrame.setVisible(true);

        setWorkingDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        select.addActionListener(openDialog);
        confirm.addActionListener(Confirm);
        }

        Action openDialog = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(setWorkingDir.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    System.setProperty("user.dir", setWorkingDir.getSelectedFile().getAbsolutePath());
                    fileSelect.setText(System.getProperty("user.dir"));
                }
            }
        };

        Action Confirm = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TextEditor();
                openFrame.dispose();
            }
        };
    }


