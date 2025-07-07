package com.tedu.element;

import java.awt.*;

public class Bullet extends ElementObj {
    private int dx, dy;
    private int attack = 2;
    private Color color = Color.MAGENTA;

    public Bullet(){}

    @Override
    public ElementObj createElement(String str){
        String[] arr = str.split(",");
        for(String kvp : arr){
            String[] kv = kvp.split(":");
            switch(kv[0]){
                case "x": this.setX(Integer.parseInt(kv[1])); break;
                case "y": this.setY(Integer.parseInt(kv[1])); break;
                case "dx": this.dx = Integer.parseInt(kv[1]); break;
                case "dy": this.dy = Integer.parseInt(kv[1]); break;
                case "color": this.color = Color.decode(kv[1]); break;
                case "attack": this.attack = Integer.parseInt(kv[1]); break;
            }
        }
        this.setW(6);
        this.setH(6);
        return this;
    }

    @Override
    public void showElement(Graphics g){
        g.setColor(color);
        g.fillOval(this.getX(), this.getY(), this.getW(), this.getH());
    }

    @Override
    public int getAttack(){
        return attack;
    }

    @Override
    protected void move(){
        this.setX(this.getX()+dx);
        this.setY(this.getY()+dy);
        if(this.getX()<0 || this.getX()>900 || this.getY()<0 || this.getY()>600){
            this.setLive(false);
        }
    }
} 