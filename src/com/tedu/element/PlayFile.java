package com.tedu.element;

//玩家子弹类

import java.awt.*;

public class PlayFile extends ElementObj{
    private int attack = 2;//攻击力
    private int moveNum = 20;//移速
    private String fx;

    public PlayFile() {}
    //小工厂
    @Override
    public ElementObj createElement(String str) {
        String[] split = str.split(",");
        for(String str1 : split) {
            String[] split2 = str1.split(":");
            switch(split2[0]) {
                case "x":this.setX(Integer.parseInt(split2[1]));break;
                case "y":this.setY(Integer.parseInt(split2[1]));break;
                case "f":this.fx=split2[1];break;
            }
        }
        this.setW(6);//子弹大小
        this.setH(6);
        return this;
    }
    @Override
    public void showElement(Graphics g){
            g.setColor(Color.red);
            g.fillOval(this.getX(),this.getY(),this.getW(),this.getH());
    }

    @Override
    public int getAttack(){
        return attack;
    }
    @Override
    protected void move(){//子弹移动，超出距离直接销毁
        if(this.getX()<0 || this.getX()>900 || this.getY()<0 || this.getY()>600) {
            this.setLive(false);
            return;
        }
        switch(this.fx)
        {
            case "up": this.setY(this.getY()-this.moveNum);break;
            case "left": this.setX(this.getX()-this.moveNum);break;
            case "right": this.setX(this.getX()+this.moveNum);break;
            case "down": this.setY(this.getY()+this.moveNum);break;

        }
    }

    @Override
    public void setAttack(int atk){
        this.attack = atk;
    }
}
