package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

import javax.swing.*;
import java.awt.*;

/**
 * 黑色追踪子弹：Boss 血量低于 50 时每 8 秒发射 1 颗。
 * 半径 30px（直径 60），移速 6，攻击力 6，存在 5 秒。
 * 击中玩家后为 Boss 回复 20 HP。
 */
public class TrackingBullet extends ElementObj {
    private static final int SPEED = 5;
    private static final int ATTACK = 6;
    private static final long LIFE_TIME = 5000;
    private final long spawnTime;
    private Boss owner;

    public TrackingBullet(){
        spawnTime = System.currentTimeMillis();
    }

    public void setOwner(Boss boss){
        this.owner = boss;
    }

    public Boss getOwner(){ return owner; }

    @Override
    public ElementObj createElement(String str){
        // "x:100,y:100" 解析
        String[] arr = str.split(",");
        for(String kvp : arr){
            String[] kv = kvp.split(":");
            switch(kv[0]){
                case "x": this.setX(Integer.parseInt(kv[1])); break;
                case "y": this.setY(Integer.parseInt(kv[1])); break;
            }
        }
        this.setW(60);
        this.setH(60);
        return this;
    }

    @Override
    public void showElement(Graphics g){
        g.setColor(Color.BLACK);
        g.fillOval(this.getX(), this.getY(), this.getW(), this.getH());
    }

    @Override
    public int getAttack(){ return ATTACK; }

    @Override
    protected void move(){
        // 超时销毁
        if(System.currentTimeMillis() - spawnTime > LIFE_TIME){
            this.setLive(false);
            return;
        }
        // 追踪最近玩家
        ElementManager em = ElementManager.getManager();
        java.util.List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        if(players.isEmpty()){
            this.setLive(false);
            return;
        }
        ElementObj player = players.get(0);
        double px = player.getX() + player.getW()/2.0;
        double py = player.getY() + player.getH()/2.0;
        double cx = this.getX() + this.getW()/2.0;
        double cy = this.getY() + this.getH()/2.0;
        double ang = Math.atan2(py-cy, px-cx);
        int dx = (int)Math.round(Math.cos(ang) * SPEED);
        int dy = (int)Math.round(Math.sin(ang) * SPEED);
        this.setX(this.getX()+dx);
        this.setY(this.getY()+dy);
        // 边界检查
        if(this.getX()< -60 || this.getX()>960 || this.getY()< -60 || this.getY()>660){
            this.setLive(false);
            return;
        }

        // 碰撞地图元素即摧毁并消失
        java.util.List<ElementObj> maps = ElementManager.getManager().getElementsByKey(GameElement.MAPS);
        for(ElementObj obj : maps){
            if(obj instanceof MapObj){
                MapObj mo = (MapObj)obj;
                if(this.pk(mo)){
                    mo.setLive(false); // 直接摧毁，但子弹继续存在
                    break;
                }
            }
        }
    }

    @Override
    public void die(){
        // 无额外效果，由 GameThread.ElementPk 在击中玩家时处理回血
    }
} 