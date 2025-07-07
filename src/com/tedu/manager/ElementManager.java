package com.tedu.manager;

import com.tedu.element.ElementObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
说明本类是元素管理器，同时提供方法给予视图和控制获取数据
q1：储存所有元素数据，怎么存放？list map set
q2：管理器是试图控制要访问，管理器就必须只有一个


 */
public class ElementManager {
    /*
    string 作为key 匹配所有元素play->list<object> listPlay
     */
    private Map<GameElement, List<ElementObj>>gameElements;
    public Map<GameElement, List<ElementObj>> getGameElements() {
        return gameElements;
    }
    //添加元素
    public void addElement(ElementObj obj,GameElement ge) {
        gameElements.get(ge).add(obj);//添加对象到集合中，按key进行存储
    }
    //按key返回list集合，取出某一类元素
    public List<ElementObj> getElementsByKey(GameElement ge) {
        return gameElements.get(ge);
    }

    private static ElementManager EM=null;
    //synchronized线程锁  保证本方法执行的过程中只有一个线程
    public static synchronized ElementManager getManager() {
        if(EM == null) {
            EM = new ElementManager();
        }
        return EM;
    }

    // 兼容其他模块别名
    public static ElementManager getElementManager(){
        return getManager();
    }

    private ElementManager() {
        init();//实例化方法
    }
    public void init(){
        //hashMap hash散列
        gameElements=new HashMap<GameElement,List<ElementObj>>();
        //将每种元素集合都放入到map中
        for(GameElement ge : GameElement.values()) {//通过循环读取枚举类型的方式添加集合
            gameElements.put(ge,new ArrayList<ElementObj>());
        }
    }
}
