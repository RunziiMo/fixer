package com.demo.library;

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class MyPlugin implements Plugin<Project> {

    void apply(Project project) {
        println "Runzii=====================Mo";
        println "hello my first plugin" + project.version;
        println "Runzii=====================Mo 1.10";
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new MyTransform(project))
    }
}