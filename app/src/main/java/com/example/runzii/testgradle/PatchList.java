package com.example.runzii.testgradle;

import com.demo.library.IPatchList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by runzii on 16-10-11.
 */

public class PatchList implements IPatchList {

    @Override
    public List<String> getPatchClasses() {
        List<String> list = new ArrayList<>();
        list.add("com.example.runzii.testgradle.MainActivity$Patch");
        return list;
    }
}
