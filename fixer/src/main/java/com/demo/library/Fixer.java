package com.demo.library;

/**
 * Created by runzii on 16-10-11.
 */

public interface Fixer {

    Object dispatchMethod(Object host, String methodSign, Object[] params);

}
