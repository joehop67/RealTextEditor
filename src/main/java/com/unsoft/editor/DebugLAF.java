package com.unsoft.editor;

import javax.swing.UIManager;
import com.bulenkov.darcula.*;
public class DebugLAF {
    public static void main(String args[]) {
        UIManager.LookAndFeelInfo plaf[] = UIManager.getInstalledLookAndFeels();
        for (int i = 0, n = plaf.length; i < n; i++) {
            System.out.println("Name: " + plaf[i].getName());
            System.out.println("  Class name: " + plaf[i].getClassName());
        }
        System.exit(0);
        }
    }

