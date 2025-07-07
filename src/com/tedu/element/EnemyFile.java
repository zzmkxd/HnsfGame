package com.tedu.element;

import java.awt.*;

// 敌人子弹类（蓝色）
public class EnemyFile extends ElementObj {
    private int attack = 2;
    private int moveNum = 5;
    private String fx;

    public EnemyFile(){}

    @Override
    public ElementObj createElement(String str){
        String[] arr = str.split(",");
        for(String s: arr){
            String[] kv = s.split(":");
            switch(kv[0]){
                case "x": this.setX(Integer.parseInt(kv[1]));break;
                case "y": this.setY(Integer.parseInt(kv[1]));break;
                case "f": this.fx = kv[1];break;
            }
        }
        this.setW(6);
        this.setH(6);
        return this;
    }

    @Override
    public void showElement(Graphics g){
        g.setColor(Color.BLUE);
        g.fillOval(this.getX(),this.getY(),this.getW(),this.getH());
    }

    @Override
    public int getAttack(){
        return attack;
    }

    @Override
    protected void move(){
        if(this.getX()<0 || this.getX()>900 || this.getY()<0 || this.getY()>600){
            this.setLive(false);
            return;
        }
        switch(this.fx){
            case "up": this.setY(this.getY()-moveNum);break;
            case "left": this.setX(this.getX()-moveNum);break;
            case "right": this.setX(this.getX()+moveNum);break;
            case "down": this.setY(this.getY()+moveNum);break;
        }
    }
} 