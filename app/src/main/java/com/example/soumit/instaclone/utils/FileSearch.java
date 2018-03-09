package com.example.soumit.instaclone.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Soumit on 3/3/2018.
 */

public class FileSearch {

    /**
     * Search a directory and return a list of all **directories** inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for(int i=0; i<listFiles.length; i++){
            if(listFiles[i].isDirectory()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }

        return pathArray;
    }

    /**
     * Search a directory and return a list of all **files** inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for(int i=0; i<listFiles.length; i++){
            if(listFiles[i].isFile()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }

        return pathArray;
    }

}




















