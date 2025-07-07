package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.AlphaComposite;
import java.util.List;
import com.tedu.manager.ElementManager;
import com.tedu.element.MapObj;
import com.tedu.util.SoundUtil;

//玩家和子弹类

public class Play extends ElementObj{

    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;


    private String fx="up";
    private boolean pkType = false;//true攻击 false停止
    private long lastFireTime = 0; // 上一次发射时间，用于长按节流

    private int maxHp = 20;//默认最大生命

    private long spawnTime; // 出生时间，用于无敌判定
    private long lastRiverDamage = 0;

    private boolean dmgBuff=false;
    private long buffEnd=0;
    private int baseSpeed = 5;
    private int curSpeed = 5;
    private int baseW;
    private int baseH;

    public Play(){}
    public Play(int x, int y, int w, int h, ImageIcon icon) {
        super(x, y, w, h, icon);
    }
    @Override
    public ElementObj createElement(String str){
        String[] split = str.split(",");
        this.setX(Integer.parseInt(split[0]));
        this.setY(Integer.parseInt(split[1]));
        ImageIcon icon2 =  GameLoad.imgMap.get(split[2]);
        this.setIcon(icon2);
        this.setW(icon2.getIconWidth());
        this.setH(icon2.getIconHeight());
        baseW = icon2.getIconWidth();
        baseH = icon2.getIconHeight();

        this.setHp(maxHp);
        this.setAttack(5);
        this.spawnTime = System.currentTimeMillis(); // 记录出生时间
        return this;
    }



    @Override
    public void showElement(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 草丛隐身：若在草丛中，降低透明度
        if(isInGrass()){
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f));
        }
        g2.drawImage(this.getIcon().getImage(),this.getX(),this.getY(),this.getH(),this.getW(),null);

        // 绘制血条
        drawHpBar(g2);
        g2.dispose();
    }

    private void drawHpBar(Graphics2D g2){
        int barW = this.getW();
        int barH = 5;
        int x = this.getX();
        int y = this.getY()-barH-2;
        float ratio = (float)this.getHp()/maxHp;
        g2.setColor(Color.RED);
        g2.fillRect(x, y, barW, barH);
        g2.setColor(Color.GREEN);
        g2.fillRect(x, y, (int)(barW*ratio), barH);
        // 绘制数值
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("微软雅黑", Font.BOLD, 10));
        g2.drawString("HP:"+this.getHp(), x, y-2);
    }

    private boolean isInGrass(){
        List<ElementObj> maps = ElementManager.getManager().getElementsByKey(GameElement.MAPS);
        for(ElementObj obj: maps){
            if(obj instanceof MapObj){
                MapObj mo = (MapObj)obj;
                if("GRASS".equals(mo.getName()) && this.pk(mo)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isOnHiddenMap(){
        return isInGrass();
    }

    public void keyClick(boolean bl,int key) {
        if (bl) {
            switch (key) {
                case java.awt.event.KeyEvent.VK_A: // A 左
                    this.left = true;
                    this.fx="left";
                    break;
                case java.awt.event.KeyEvent.VK_W: // W 上
                    this.up = true;
                    this.fx="up";
                    break;
                case java.awt.event.KeyEvent.VK_D: // D 右
                    this.right = true;
                    this.fx="right";
                    break;
                case java.awt.event.KeyEvent.VK_S: // S 下
                    this.down = true;
                    this.fx="down";
                    break;
                case 32:
                    // 如果是首次按下，则立即发射一颗子弹
                    if(!this.pkType){
                        fireBullet();
                        lastFireTime = System.currentTimeMillis();
                    }
                    this.pkType = true; // 标记为长按
                    break;
            }
        } else{
            switch (key) {
                case java.awt.event.KeyEvent.VK_A:this.left=false;
                break;
                case java.awt.event.KeyEvent.VK_W:this.up=false;
                break;
                case java.awt.event.KeyEvent.VK_D:this.right=false;
                break;
                case java.awt.event.KeyEvent.VK_S:this.down=false;
                break;
                case 32:this.pkType=false;
                break;
            }
        }
    }
    @Override
    public void move(){
        // 检查buff过期
        if(dmgBuff && System.currentTimeMillis()>buffEnd){
            dmgBuff=false;
            curSpeed = baseSpeed;
            this.setW(baseW);
            this.setH(baseH);
        }

        int speed = curSpeed;
        ElementManager em = ElementManager.getManager();
        List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);

        if(this.left){
            int nextX = this.getX()-speed;
            if(nextX>=0 && canThrough(nextX,this.getY(),maps)){
                this.setX(nextX);
            }
        }
        if(this.up){
            int nextY = this.getY()-speed;
            if(nextY>=0 && canThrough(this.getX(),nextY,maps)){
                this.setY(nextY);
            }
        }
        if(this.right){
            int nextX = this.getX()+speed;
            if(nextX<881-this.getW() && canThrough(nextX,this.getY(),maps)){
                this.setX(nextX);
            }
        }
        if(this.down){
            int nextY = this.getY()+speed;
            if(nextY<560-this.getH() && canThrough(this.getX(),nextY,maps)){
                this.setY(nextY);
            }
        }
    }

    private boolean canThrough(int nextX,int nextY,List<ElementObj> maps){
        Rectangle rect = new Rectangle(nextX,nextY,this.getW(),this.getH());
        // 1. 墙体碰撞（除草丛）
        for(ElementObj obj: maps){
            if(obj instanceof MapObj){
                MapObj mo = (MapObj)obj;
                if("GRASS".equals(mo.getName()) || "RIVER".equals(mo.getName())){
                    continue;
                }
                if(rect.intersects(mo.getRectangle())){
                    return false;
                }
            }
        }
        ElementManager em = ElementManager.getManager();
        // 2. 与其他玩家碰撞（目前通常只有一个玩家）
        List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        for(ElementObj p: plays){
            if(p==this) continue;
            if(rect.intersects(p.getRectangle())){
                return false;
            }
        }
        // 3. 与敌人碰撞 -> 双方扣除较小的剩余生命值
        List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
        for(ElementObj en: enemys){
            if(rect.intersects(en.getRectangle())){
                // 双方扣除较小的剩余生命值
                int damage = Math.min(this.getHp(), en.getHp());
                en.setLive(true, damage);
                this.setLive(true, damage);
                return false;
            }
        }
        // 河水掉血检测
        for(ElementObj obj: maps){
            if(obj instanceof MapObj){
                MapObj mo = (MapObj)obj;
                if("RIVER".equals(mo.getName()) && rect.intersects(mo.getRectangle())){
                    long now = System.currentTimeMillis();
                    if(now - lastRiverDamage > 500){
                        this.setLive(true,1); // 扣1点血
                        lastRiverDamage = now;
                    }
                }
            }
        }
        return true;
    }

    protected void updateImage(){
        ImageIcon icon = GameLoad.imgMap.get(fx);
        //System.out.println(icon.getIconHeight());
        this.setIcon(GameLoad.imgMap.get(fx));
    }


    @Override
    public void add(){
        if(!this.pkType){
            return; // 未长按
        }
        long now = System.currentTimeMillis();
        if(now - lastFireTime >= 500){ // 每0.5秒连发
            fireBullet();
            lastFireTime = now;
        }
    }

    private void fireBullet(){
        ElementObj element = new PlayFile().createElement(this.toString());
        element.setAttack(getCurrentAttack());
        ElementManager.getManager().addElement(element, GameElement.PLAYFILE);
        SoundUtil.playEffect("audio/shoot.wav");
    }

    //子弹生成位置   返回字符串{x:3,y:4,f:up}格式
    @Override
    public String toString(){
        int x = this.getX();
        int y = this.getY();
        switch(this.fx){
            case "up":x+=16;break;
            case "down":y+=25;x+=16;break;
            case "left":y+=16;break;
            case "right":x+=25;y+=16;break;
        }
        return "x:"+x+",y:"+y+",f:"+this.fx;
    }

    @Override
    public void setLive(boolean live,int atk){
        long now = System.currentTimeMillis();
        // 出生3秒内无敌
        if(now - spawnTime < 3000){
            return;
        }
        super.setLive(live, atk);
    }

    @Override
    public void die(){
        // 添加爆炸效果
        String pos = "x:"+this.getX()+",y:"+this.getY();
        ElementManager.getManager().addElement(new Boom().createElement(pos), GameElement.DIE);
    }

    public void applyTool(com.tedu.element.Tool.Type type){
        if(type== com.tedu.element.Tool.Type.HEAL){
            this.setHp(maxHp);
        }else{
            dmgBuff = true;
            buffEnd = System.currentTimeMillis()+2000;
            curSpeed = baseSpeed + 5;
        }
    }

    private int getCurrentAttack(){
        if(dmgBuff){
            if(System.currentTimeMillis()>buffEnd){ dmgBuff=false; }
        }
        return dmgBuff? this.getAttack()*2 : this.getAttack();
    }

}
