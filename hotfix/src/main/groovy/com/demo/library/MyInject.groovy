package com.demo.library

import javassist.*

public class MyInject {

    private static ClassPool pool = ClassPool.getDefault()
    private static String injectStr = "System.out.println(\"I Love RunziiMo\" );";

    public static void injectDir(String path, String packageName) {
        pool.appendClassPath(path)

        int rootIndex = path.indexOf("/app/build/intermediates/")
        String fixerPath = path.substring(0, rootIndex) + "/fixer/build/intermediates/classes/release"
        println fixerPath;
        pool.appendClassPath(fixerPath)
        pool.insertClassPath("/home/runzii/Android/Sdk/platforms/android-24/android.jar")

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->

                String filePath = file.absolutePath

                //确保当前文件是class文件，并且不是系统自动生成的class文件
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")
                        && !filePath.contains("\$Patch.class")
                        && !filePath.contains("PatchList.class")) {
                    // 判断当前目录是否是在我们的应用包里面
                    int index = filePath.indexOf(packageName);
                    boolean isMyPackage = index != -1;
                    if (isMyPackage) {

                        println 'filePath = ' + filePath + ' packageName = ' + packageName;
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end)
                                .replace('\\', '.').replace('/', '.')

                        //开始修改class文件
                        CtClass c = pool.getCtClass(className)

                        println 'className = ' + className + ' c.name = ' + c.name;
                        if (c.isFrozen()) {
                            c.defrost()
                        }

                        pool.importPackage("com.demo.library")
//
//                        Iterator it = pool.getImportedPackages();
//                        while (it.hasNext()){
//                            println it.next();
//                        }

                        //给类添加$fixer变量，即补丁变量
                        CtField fixer = new CtField(pool.get("com.demo.library.Fixer"), "\$fixer", c);
                        fixer.setModifiers(Modifier.STATIC);
                        println fixer.fieldInfo.descriptor + " " + fixer.name + " " + fixer.modifiers;
                        boolean b = false;
                        for (CtField ctField : c.getDeclaredFields()) {
                            if (fixer.fieldInfo.descriptor.equals(ctField.fieldInfo.descriptor)
                                    && fixer.name.equals(ctField.name)
                                    && fixer.modifiers == ctField.modifiers) {
                                b = true;
                                break;
                            }
                            println ctField.fieldInfo.descriptor + " " + ctField.name + " " + ctField.modifiers;
                        }
                        //如果已经有了$fixer变量
                        if (b)
                            return;
                        c.addField(fixer);

                        //遍历类的所有方法
                        CtMethod[] methods = c.getDeclaredMethods();
                        for (CtMethod method : methods) {
                            //在每个方法之前插入判断语句，判断类的补丁实例是否存在
                            StringBuilder injectStr = new StringBuilder();
                            injectStr.append("if(\$fixer!=null){\n")
                            String javaThis = "null,"
                            if (!Modifier.isStatic(method.getModifiers())) {
                                javaThis = "this,"
                            }
                            String runStr = "\$fixer.dispatchMethod(" + javaThis + "\"" + method.getName() + "." + method.getSignature() + "\" ,\$args)"
                            injectStr.append(addReturnStr(method, runStr))
                            injectStr.append("}")
                            println "插入了：\n" + injectStr.toString() + "\n";
                            method.insertBefore(injectStr.toString())
                        }
//                        CtConstructor[] cts = c.getDeclaredConstructors()
//                        if (cts == null || cts.length == 0) {
//                            //手动创建一个构造函数
//                            CtConstructor constructor = new CtConstructor(new CtClass[0], c)
//                            constructor.insertBeforeBody(injectStr)
//                            c.addConstructor(constructor)
//                        } else {
//                            cts[0].insertBeforeBody(injectStr)
//                        }
//                        println cts[0].longName;
                        c.writeFile(path)
                        c.detach()
                    }
                }
            }
        }
    }

    //给非void方法加入return语句，并处理基本类型返回值
    public static String addReturnStr(CtMethod method, String runStr) {
        String returnStr = "";
        String typeStr = "";
        switch (method.getReturnType()) {
            case CtClass.voidType:
                return runStr + ";\n return;"
                break;
            case CtClass.booleanType:
                returnStr = "return ((Boolean)";
                typeStr = ".booleanValue()";
                break;
            case CtClass.byteType:
                returnStr = "return ((byte)";
                typeStr = ".byteValue()";
                break;
            case CtClass.charType:
                returnStr = "return ((char)";
                typeStr = ".charValue()";
                break;
            case CtClass.doubleType:
                returnStr = "return ((Number)";
                typeStr = ".doubleValue()";
                break;
            case CtClass.floatType:
                returnStr = "return ((Number)";
                typeStr = ".floatValue()";
                break;
            case CtClass.intType:
                returnStr = "return ((Number)";
                typeStr = ".intValue()";
                break;
            case CtClass.longType:
                returnStr = "return ((Number)";
                typeStr = ".longValue()";
                break;
            case CtClass.shortType:
                returnStr = "return ((Number)";
                typeStr = ".shortValue()";
                break;
            default:
                returnStr = "return((" + getReturnType(method.getSignature()) + ")";
                break;
        }
        return returnStr + "(" + runStr + "))" + typeStr + ";\n";
    }

    //解析方法签名，获取方法放回值类型
    public static String getReturnType(String methodSign) {
        println methodSign;
        String type = "";
        int index = methodSign.indexOf(")L");
        String jType = methodSign.substring(index + 2, methodSign.length() - 1);
        type = jType.replace(" / ", ".");
        return type;
    }

}