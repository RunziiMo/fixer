package com.demo.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * Created by runzii on 16-10-11.
 */
public class DxyPatchLoader {


    private static DxyPatchLoader ourInstance = new DxyPatchLoader();

    public static DxyPatchLoader getInstance() {
        return ourInstance;
    }

    private DxyPatchLoader() {
    }


    public void loadPatch(@NonNull Context context, @NonNull LoaderCallback callback) {

        try {
            //加载补丁Dex文件
            DexClassLoader dexClassLoader = new DexClassLoader(callback.getPatchInfo().patchFilePath, getOdexPath(context),
                    null, getClass().getClassLoader());

            //加载补丁装载类PatchList
            Class<?> patchBoxClass = Class.forName(callback.getPatchInfo().patchListName, true, dexClassLoader);
            IPatchList patchList = (IPatchList) patchBoxClass.newInstance();

            //遍历加载补丁类
            for (String className : patchList.getPatchClasses()) {
                Class<?> patchClass = dexClassLoader.loadClass(className);
                Object patchInstance = patchClass.newInstance();

                //反射修改bug类的$fixer字段
                int index = className.indexOf("$Patch");
                if (index == -1) {
                    Log.e("DxyHotfix:", "热更新的补丁类必须以$Patch做结尾");
                    return;
                }
                String bugClassName = className.substring(0, index);
                Class<?> bugClass = getClass().getClassLoader().loadClass(bugClassName);
                Field saviorField = bugClass.getDeclaredField("$fixer");
                saviorField.setAccessible(true);
                saviorField.set(null, patchInstance);
            }

            callback.loadComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getOdexPath(Context context) {
        return context.getCacheDir().getPath();
    }

    public interface LoaderCallback {

        PatchInfo getPatchInfo();

        void loadComplete();
    }
}
