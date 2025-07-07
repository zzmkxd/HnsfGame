package com.tedu.element;

import javax.swing.*;
import java.awt.*;

// 爆炸特效，只显示一段时间后自动消失
public class Boom extends ElementObj{
    private long startTime;
    private long duration = 500; // 毫秒

    @Override
    public ElementObj createElement(String str){
        // str 格式 "x:100,y:200"
        String[] arr = str.split(",");
        for(String s: arr){
            String[] kv = s.split(":");
            if(kv[0].equals("x")) this.setX(Integer.parseInt(kv[1]));
            if(kv[0].equals("y")) this.setY(Integer.parseInt(kv[1]));
        }
        ImageIcon icon = new ImageIcon("image/boom/boom.png");
        this.setIcon(icon);
        this.setW(icon.getIconWidth());
        this.setH(icon.getIconHeight());
        startTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public void showElement(Graphics g){
        g.drawImage(this.getIcon().getImage(),this.getX(),this.getY(),this.getW(),this.getH(),null);
    }

    @Override
    protected void move(){
        if(System.currentTimeMillis()-startTime>duration){
            this.setLive(false);
        }
    }
} 