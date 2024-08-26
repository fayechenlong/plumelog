package com.beeplay;


import org.junit.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test

    private List<String> getIndex(String indexStr) {
        List<String> indexsList = new ArrayList<>();
        String[] indexs = indexStr.split(",");
        for (int i = 0; i < indexs.length; i++) {
            if (indexs[i].contains("*")) {
                File dir = new File(".");
                if (dir.isDirectory()) {
                    //获取当前目录下的所有子项
                    File[] subs = dir.listFiles();
                    for (File sub : subs) {
                        String name = sub.getName();
                        if (Pattern.matches(indexs[i].replace("*", ".*"), name)) {
                            indexsList.add(name);
                        }
                    }
                }
            } else {
                indexsList.add(indexs[i]);
            }
        }
        return indexsList;
    }
}
