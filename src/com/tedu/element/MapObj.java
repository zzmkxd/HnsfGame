package com.tedu.element;

import javax.swing.*;
import java.awt.*;

public class MapObj extends ElementObj{
    private int hp;//墙的血量
    private String name;//墙的种类，也可以使用枚举

    @Override
    public void showElement(Graphics g) {
        g.drawImage(this.getIcon().getImage(),
                this.getX(), this.getY(),
                this.getW(),this.getH(),null);
    }

    @Override
    public ElementObj createElement(String str) {
        String []arr = str.split(",");
        ImageIcon icon = null;
        switch(arr[0]){
            case "GRASS":icon = new ImageIcon("image/wall/grass.png");
                name="GRASS";
                break;
            case "BRICK":icon = new ImageIcon("image/wall/brick.png");
                this.hp=8;
                name="BRICK";
                break;
            case "RIVER":icon = new ImageIcon("image/wall/river.png");
                name="RIVER";
                break;
            case "IRON":icon = new ImageIcon("image/wall/iron.png");
                this.hp=10;
                name="IRON";
                break;
        }
        int x = Integer.parseInt(arr[1]);
        int y = Integer.parseInt(arr[2]);
        int w=icon.getIconWidth();
        int h=icon.getIconHeight();
        this.setH(h);
        this.setW(w);
        this.setX(x);
        this.setY(y);
        this.setIcon(icon);
        return this;
    }

    public void setLive(boolean live,int atk) {
        if("IRON".equals(name)||"BRICK".equals(name)||
                "RIVER".equals(name)||"GRASS".equals(name)){
            this.hp-=atk;
            if (this.hp > 0) {
                return;
            }
        }
        super.setLive(live);
    }

    // 新增方法：返回当前墙体类型标识
    public String getName(){
        return name;
    }

    // 兼容 Boss 类使用
    public boolean isObstacle(){
        // 草丛不算障碍
        return !"GRASS".equals(name);
    }

    public String getMapType(){
        return name;
    }
}
