package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.element.ElementObj;
import com.tedu.element.Enemy;

import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.awt.*;
import java.awt.geom.*;

/**
 * Boss：第 10 关专属敌人，血量 80，攻击力 20，8 向紫色子弹，高射速。
 */
public class Boss extends Enemy {

    // 3 向射击冷却 2s
    private static final int FIRE_COOLDOWN = 2000;
    // 低血量时 16 向射击冷却 5s
    private static final int RADIAL_COOLDOWN = 5000;
    private long lastFire = 0;
    private long lastRadial = 0;
    private boolean rLeft=false, rRight=false, rUp=false, rDown=false;
    private long lastRandomTime=0;
    private static final String BULLET_COLOR = "#FF66FF";

    public Boss() {
        super();
        this.setFx("up");
        this.setHealth(80);
        this.setW(45);
        this.setH(45);
        this.setIcon(new ImageIcon("image/tank/play2/player2_up.png"));
    }

    @Override
    protected void updateImage() {
        String path = "image/tank/play2/player2_"+getFx()+".png";
        this.setIcon(new ImageIcon(path));
    }

    @Override
    protected void add() {
        long now = System.currentTimeMillis();
        ElementManager em = ElementManager.getManager();
        if(now-lastFire>=FIRE_COOLDOWN){
            lastFire = now;
            fireTriple(em);
        }
        if(now - lastRadial >= RADIAL_COOLDOWN){
            lastRadial = now;
            fireRadial(em);
        }
    }

    private void fireTriple(ElementManager em){
        double[] angles;
        switch(getFx()){
            case "left": angles = new double[]{Math.PI, Math.PI+Math.toRadians(15), Math.PI-Math.toRadians(15)}; break;
            case "right": angles = new double[]{0, Math.toRadians(15), -Math.toRadians(15)}; break;
            case "down": angles = new double[]{Math.PI/2, Math.PI/2+Math.toRadians(15), Math.PI/2-Math.toRadians(15)}; break;
            default: angles = new double[]{-Math.PI/2, -Math.PI/2+Math.toRadians(15), -Math.PI/2-Math.toRadians(15)};
        }
        for(double ang: angles){
            createBullet(em, ang, 6, 4);
        }
    }

    private void fireRadial(ElementManager em){
        for(int i=0;i<16;i++){
            double ang = 2*Math.PI/16*i;
            createBullet(em, ang, 6, 4);
        }
    }

    private void createBullet(ElementManager em, double ang, int speed, int atk){
        int dx = (int)Math.round(Math.cos(ang)*speed);
        int dy = (int)Math.round(Math.sin(ang)*speed);
        String data = String.format("x:%d,y:%d,dx:%d,dy:%d,color:%s,attack:%d",
                this.getX()+this.getW()/2, this.getY()+this.getH()/2, dx, dy, "#FF66FF", atk);
        Bullet b = (Bullet) new Bullet().createElement(data);
        b.setW(10);
        b.setH(10);
        em.addElement(b, GameElement.ENEMYFILE);
        int BossSpeed = getHealth()<24?6:2;
        if(Math.abs(dx)>Math.abs(dy)){
            if(dx>0){ this.setX(this.getX()+speed); this.setFx("right"); }
            else { this.setX(this.getX()-speed); this.setFx("left"); }
        }else{
            if(dy>0){ this.setY(this.getY()+speed); this.setFx("down"); }
            else { this.setY(this.getY()-speed); this.setFx("up"); }
        }
    }

    @Override
    public void move() {
        // 追踪玩家在 400 半径内
        ElementManager em = ElementManager.getManager();
        java.util.List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
        if(plays.isEmpty()) return;
        ElementObj player = plays.get(0);
        if(player.isOnHiddenMap()){
            // 玩家在草丛中，停止追踪
        }else{
            int dx = player.getX()-this.getX();
            int dy = player.getY()-this.getY();
            if(dx*dx + dy*dy <= 400*400){
                int speed = 2;
                if(Math.abs(dx)>Math.abs(dy)){
                    if(dx>0){ this.setX(this.getX()+speed); this.setFx("right"); }
                    else { this.setX(this.getX()-speed); this.setFx("left"); }
                }else{
                    if(dy>0){ this.setY(this.getY()+speed); this.setFx("down"); }
                    else { this.setY(this.getY()-speed); this.setFx("up"); }
                }
            }
        }
        //边界限制
        if(this.getX()<0) this.setX(0);
        if(this.getY()<0) this.setY(0);
        if(this.getX()>900-this.getW()) this.setX(900-this.getW());
        if(this.getY()>600-this.getH()) this.setY(600-this.getH());

        // 吞噬碰撞到的方块
        java.util.List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
        for(ElementObj obj: maps){
            if(obj instanceof com.tedu.element.MapObj){
                com.tedu.element.MapObj mo = (com.tedu.element.MapObj)obj;
                if(this.pk(mo)){
                    mo.setLive(false);
                }
            }
        }
    }

    private void playShootSound(){
        try(AudioInputStream ais =
                AudioSystem.getAudioInputStream(
                    new File("audio/shoot.wav"))){
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        }catch(Exception e){ /* 忽略或打印 */ }
    }

    @Override
    public ElementObj createElement(String str){
        // 随机出生位置（复制 Enemy 中避障逻辑）
        this.setW(45);
        this.setH(45);

        java.util.Random rand = new java.util.Random();
        ElementManager em = ElementManager.getManager();
        java.util.List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);

        int x,y; boolean collide;
        do{
            x = rand.nextInt(900 - this.getW());
            y = rand.nextInt(600 - this.getH());
            this.setX(x); this.setY(y);
            java.awt.Rectangle r = this.getRectangle();
            collide = false;
            for(ElementObj m : maps){
                if(r.intersects(m.getRectangle())){ collide = true; break; }
            }
        }while(collide);

        // 80 HP 保持
        this.setHealth(80);
        return this;
    }

    @Override
    public void showElement(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();

        g2.drawImage(this.getIcon().getImage(), this.getX(), this.getY(), this.getW(), this.getH(), null);

        // 绘制血条
        int barW = this.getW();
        int barH = 5;
        int x = this.getX();
        int y = this.getY()-barH-2;
        float ratio = (float)this.getHp()/80f;
        g2.setColor(Color.GRAY);
        g2.fillRect(x, y, barW, barH);
        g2.setColor(Color.RED);
        g2.fillRect(x, y, (int)(barW*ratio), barH);

        // 绘制居中文本
        String txt = "HP:"+this.getHp();
        g2.setFont(new Font("微软雅黑", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int txtW = fm.stringWidth(txt);
        g2.setColor(Color.WHITE);
        g2.drawString(txt, x + (barW - txtW)/2, y-2);

        g2.dispose();
    }
}