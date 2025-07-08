package com.tedu.element;

import javax.swing.*;
import java.awt.*;

public class Tool extends ElementObj {
    public enum Type { HEAL, DOUBLE, INVINCIBLE }
    private Type type;

    public Tool(){}

    public Type getType(){return type;}

    @Override
    public ElementObj createElement(String str){
        // str: x:100,y:200,type:HEAL
        String[] arr = str.split(",");
        for(String kv: arr){
            String[] p=kv.split(":");
            switch(p[0]){
                case "x": setX(Integer.parseInt(p[1])); break;
                case "y": setY(Integer.parseInt(p[1])); break;
                case "type": type = Type.valueOf(p[1]); break;
            }
        }
        if(type==null) type = Type.HEAL;
        String path;
        switch(type){
            case HEAL: path = "image/tool/01.png"; break;
            case DOUBLE: path = "image/tool/02.png"; break;
            case INVINCIBLE: path = "image/tool/03.png"; break;
            default: path = "image/tool/01.png";
        }
        ImageIcon icon = new ImageIcon(path);
        setIcon(icon);
        setW(icon.getIconWidth());
        setH(icon.getIconHeight());
        return this;
    }
    //击杀后的图标更新
    @Override
    public void showElement(Graphics g){
        g.drawImage(getIcon().getImage(), getX(), getY(), null);
    }
}
