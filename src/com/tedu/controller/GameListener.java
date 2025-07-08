package com.tedu.controller;


//监听类

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameListener implements KeyListener{
    private ElementManager em = ElementManager.getManager();

    private Set<Integer> set = new HashSet<Integer>();

    //用来加一点注释
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override//按下：上下左右38 40 37 39
    public void keyPressed(KeyEvent e) {
        //System.out.println("按下"+e.getKeyCode());
        int key = e.getKeyCode();
        if(set.contains(key)){
            return;
        }
        set.add(key);
        List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
        for(ElementObj obj : play){
            obj.keyClick(true,e.getKeyCode());
        }
    }
    @Override//松开
    public void keyReleased(KeyEvent e) {

        if(!set.contains(e.getKeyCode())){
            return;
        }
        set.remove(e.getKeyCode());
        List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
        for(ElementObj obj : play){
            obj.keyClick(false,e.getKeyCode());
        }
    }

}
