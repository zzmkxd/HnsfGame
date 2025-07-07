package com.tedu.element;

//所有元素的基类
import java.awt.Rectangle;
import javax.swing.*;
import java.awt.*;

public abstract class ElementObj {

    private int hp,attack;
    private int x,y,w,h;
    private boolean live = true;

    private ImageIcon icon;//图片显示
    public ElementObj(){};//无用，只是为了继承不报错写的


    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
        super();
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;

    }


    //抽象方法，用画笔显示元素
    public abstract void showElement(Graphics g);

    public void keyClick(boolean bl,int key){}
    protected void move(){}

    public final void model(){//final不允许重写
        updateImage();
        move();
        add();
    }
    protected void updateImage(){}
    protected void add(){}

    public ElementObj createElement(String str){
        return null;
    }
//实时返回元素的碰撞矩形对象
    public Rectangle getRectangle(){
        return new Rectangle(x,y,w,h);
    }
    //返回true则有碰撞
    public boolean pk(ElementObj obj){
        return this.getRectangle().intersects(obj.getRectangle());
    }

    //只要是VO类就要生成set和get方法
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
    public void setLive(boolean live, int attack) {
        this.hp -= attack;
        if (this.hp <= 0) {
            this.live = false;
        } else {
            this.live = true;
        }
    }
    //死亡方法
    public void die(){//死亡也是一个对象
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    // 判断是否处于隐藏地形（草丛）中，默认 false，子类可重写
    public boolean isOnHiddenMap(){
        return false;
    }
}
