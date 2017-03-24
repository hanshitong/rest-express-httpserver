package org.restexpress.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * 引入的类，非restexpress本身
 * @author hanst
 *
 */
public class Classes {
	private static Hashtable<String,Method> mc = new Hashtable<>(); 
	
    private Classes() {}
 
    /**
     * 
     * <p>
     * 获取方法参数名称
     * </p>
     * 
     * @param cm
     * @return
     */
    protected static String[] getMethodParamNames(CtMethod cm) {
        CtClass cc = cm.getDeclaringClass();
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
                .getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            try {
				throw new Exception(cc.getName());
			} catch (Exception e) {				 
				e.printStackTrace();
			}
        }
 
        String[] paramNames = null;
        try {
            paramNames = new String[cm.getParameterTypes().length];
        } catch (NotFoundException e) {
        	e.printStackTrace(); 
        }
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = attr.variableName(i + pos);
        }
        return paramNames;
    }
 
    /**
     * 获取方法参数名称，按给定的参数类型匹配方法
     * 
     * @param clazz
     * @param method
     * @param paramTypes
     * @return
     */
    public static String[] getMethodParamNames(Class<?> clazz, String method,
            Class<?>... paramTypes) {
 
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = null;
        CtMethod cm = null;
        try {
            cc = pool.get(clazz.getName());
 
            String[] paramTypeNames = new String[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++)
                paramTypeNames[i] = paramTypes[i].getName();
 
            cm = cc.getDeclaredMethod(method, pool.get(paramTypeNames));
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return getMethodParamNames(cm);
    }
 
    /**
     * 获取方法参数名称，匹配同名的某一个方法
     * 
     * @param clazz
     * @param method
     * @return
     * @throws NotFoundException
     *             如果类或者方法不存在
     * @throws MissingLVException
     *             如果最终编译的class文件不包含局部变量表信息
     */
    public static String[] getMethodParamNames(Class<?> clazz, String method) {
 
        ClassPool pool = ClassPool.getDefault();
        CtClass cc;
        CtMethod cm = null;
        try {
            cc = pool.get(clazz.getName());
            cm = cc.getDeclaredMethod(method);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return getMethodParamNames(cm);
    }
    
    /**
     * 根据获取缓存中的pojo类的get方法对象,注意pojo中的方法不能同名
     * @param clazz
     * @param method get方法名
     * @return
     */
//    public static Method getCacheGetMethod(Class<?> clazz, String method){
//    	String key = clazz.getName() + "." + method;
//    	Method m = mc.get(key);
//    	if (m == null){
//    		Method[] ms = clazz.getMethods();
//    		for(Method i: ms){
//    			if (i.getName().equals("get" + method.substring(0, 1).toUpperCase() + method.substring(1)))
//    				mc.put(key,i);
//    		}
//    		return null; 
//    	}
//    }
 
    public static void main(String[] args) throws SecurityException,
    	NoSuchMethodException {    	
    }
}
