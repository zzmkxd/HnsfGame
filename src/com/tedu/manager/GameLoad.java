package com.tedu.manager;

import com.tedu.element.ElementObj;
import com.tedu.element.MapObj;


import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.util.*;

public class GameLoad {
    private static ElementManager em = ElementManager.getManager();
    public static Map<String, ImageIcon> imgMap = new HashMap<>();
    public static Map<String, List<ImageIcon>>imgMaps;


    private static Properties pro = new Properties();

    public static void MapLoad(int mapId){
        String mapName = "com/tedu/text/"+mapId+".map";
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream maps = classLoader.getResourceAsStream(mapName);
        if(maps == null){
            System.out.println("配置文件读取异常，请重新安装");
            return;
        }
        try{
            pro.clear();
            pro.load(maps);
            Enumeration<?> names=pro.propertyNames();
            while(names.hasMoreElements()){
                String key = names.nextElement().toString();
                String [] arrs=pro.getProperty(key).split(";");
                for(int i = 0;i<arrs.length;i++){
                    ElementObj element = new MapObj().createElement(key+","+arrs[i]);
                    em.addElement(element,GameElement.MAPS);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        //加载图片代码

    }
    public static void LoadImg(){//可以带参数，不同关带不同图片资源
        String texturl="com/tedu/text/GameData.pro";
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream texts = classLoader.getResourceAsStream(texturl);
        pro.clear();
        try{
            pro.load(texts);
            Set<Object> set =  pro.keySet();
            for(Object o : set){
                String url = pro.getProperty(o.toString());
                imgMap.put(o.toString(), new ImageIcon(url));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void LoadPlay(){
        LoadObj();
        String playStr="500,500,up";
        ElementObj obj = getObj("play");
        ElementObj play =  obj.createElement(playStr);
        em.addElement(play,GameElement.PLAY);
    }

    public static ElementObj getObj(String str){
        try{
            Class<?> class1 = objMap.get("play");
            Object newInstance = class1.newInstance();
            if(newInstance instanceof ElementObj){
                return  (ElementObj) newInstance;
            }
        }catch (InstantiationException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String,Class<?>> objMap = new HashMap<>();

    public static void LoadObj(){
        String texturl="com/tedu/text/Obj.pro";
        ClassLoader classLoader = GameLoad.class.getClassLoader();
        InputStream texts = classLoader.getResourceAsStream(texturl);
        pro.clear();
        try{
            pro.load(texts);
            Set<Object> set =  pro.keySet();
            for(Object o : set){
                String classurl = pro.getProperty(o.toString());
                Class<?> forName = Class.forName(classurl);
                objMap.put(o.toString(), forName);
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }


    //用于测试
    public static void main(String[] args){
        MapLoad(5);
    }




}
