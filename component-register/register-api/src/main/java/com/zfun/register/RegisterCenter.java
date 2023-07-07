package com.zfun.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegisterCenter {
    private static final Map<Object, Class<?>> sRegisterProviderMap = new HashMap<>();
    private static final List<IRegisterProvider> sRegisterProviderList = new ArrayList<>();

     public static <T> T getValue(Class<?> key){
         final Object value = sRegisterProviderMap.get(key);
         if (null == value){
             return null;
         }
        return (T) value;
     }

     public static <T> T getValue(String key){
         final Object value = sRegisterProviderMap.get(key);
         if (null == value){
             return null;
         }
         return (T) value;
     }

     public static List<IRegisterProvider> getRegisterProviderList(){
         return new ArrayList<>(sRegisterProviderList);
     }

     public static void printAllRegister(){
        final Set<Object> keySet = sRegisterProviderMap.keySet();
         for (Object key : keySet) {
             System.out.println("Register:: >>>>>>>>>>>>>>>>");
             System.out.println("Register::key   >>>" + key.toString());
             System.out.println("Register::value >>>" + sRegisterProviderMap.get(key).toString());
         }
     }
}
