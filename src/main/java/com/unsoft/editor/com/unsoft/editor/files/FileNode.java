package com.unsoft.editor.com.unsoft.editor.files;

import java.io.File;

public class FileNode {
    private File file;

    public FileNode(File file){
        this.file = file;
    }

    public File getFile(){
        return file;
    }

    @Override
    public String toString(){
        String name = file.getName();
        if(name.equals("")){
            return file.getAbsolutePath();
        }else{
            return name;
        }
    }

}
