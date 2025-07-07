package com.tedu.element;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.List;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import java.awt.AlphaComposite;


public class Enemy extends ElementObj{
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    private String fx = "up";
    private String skinType; // "bot" 或 "play2"
    Random rand = new Random();
    private long lastTurnTime = 0;
    private long lastFireTime = 0; // 子弹发射计时

    private int maxHp = 10;

    @Override
    public void showElement(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 草丛隐身也适用于敌人（可根据需求调整）
        if(isInGrass()){
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.4f));
        }

        g2.drawImage(this.getIcon().getImage(),this.getX(),this.getY(),this.getW(),this.getH(),null);

        drawHpBar(g2);
        g2.dispose();
    }

    private void drawHpBar(Graphics2D g2){
        int barW = this.getW();
        int barH = 5;
        int x = this.getX();
        int y = this.getY()-barH-2;
        float ratio = (float)this.getHp()/maxHp;
        g2.setColor(Color.GRAY);
        g2.fillRect(x, y, barW, barH);
        g2.setColor(Color.RED);
        g2.fillRect(x, y, (int)(barW*ratio), barH);

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
    public ElementObj createElement(String str){
        // 先设置尺寸，便于后续碰撞检测
        this.setH(35);
        this.setW(35);

        Random rand = new Random();
        ElementManager em = ElementManager.getManager();
        List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);

        // 统一使用蓝色 bot 皮肤
        this.skinType = "bot";
        this.maxHp = 10;

        int x,y;
        boolean collide;
        // 随机生成坐标，直到不与任何地图块发生碰撞
        do {
            x = rand.nextInt(900 - this.getW());
            y = rand.nextInt(600 - this.getH());
            this.setX(x);
            this.setY(y);

            Rectangle rect = this.getRectangle();
            collide = false;
            for(ElementObj map : maps){
                if(rect.intersects(map.getRectangle())){
                    collide = true;
                    break;
                }
            }
        } while (collide);

        this.setHp(maxHp);
        this.setAttack(5);
        this.fx = "up";
        updateImage();

        return this;
    }

    @Override
    public void move() {
        int speed = 5;
        ElementManager em = ElementManager.getManager();
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        boolean chasing = false;
        if(!players.isEmpty()){
            ElementObj p = players.get(0);
            int dx = p.getX()-this.getX();
            int dy = p.getY()-this.getY();
            if(dx*dx + dy*dy <= 200*200){
                chasing = true;
                // 设定方向标记
                this.left=this.right=this.up=this.down=false;
                if(Math.abs(dx) > Math.abs(dy)){
                    if(dx>0){ this.right=true; this.fx="right"; }
                    else { this.left=true; this.fx="left"; }
                }else{
                    if(dy>0){ this.down=true; this.fx="down"; }
                    else{ this.up=true; this.fx="up"; }
                }
            }
        }

        if(!chasing){
            Turn();
        }

        List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
        if(this.left){
            int nextX = this.getX()-speed;
            if(nextX>=0 && canThrough(nextX,this.getY(),maps)) this.setX(nextX);
        }
        if(this.up){
            int nextY = this.getY()-speed;
            if(nextY>=0 && canThrough(this.getX(),nextY,maps)) this.setY(nextY);
        }
        if(this.right){
            int nextX = this.getX()+speed;
            if(nextX<881-this.getW() && canThrough(nextX,this.getY(),maps)) this.setX(nextX);
        }
        if(this.down){
            int nextY = this.getY()+speed;
            if(nextY<560-this.getH() && canThrough(this.getX(),nextY,maps)) this.setY(nextY);
        }
    }

    private boolean canThrough(int nextX,int nextY,List<ElementObj> maps){
        Rectangle rect = new Rectangle(nextX,nextY,this.getW(),this.getH());
        // 1. 与墙体碰撞（除草丛）
        for(ElementObj obj: maps){
            if(obj instanceof MapObj){
                MapObj mo = (MapObj)obj;
                if("GRASS".equals(mo.getName())){
                    continue;
                }
                if(rect.intersects(mo.getRectangle())){
                    return false;
                }
            }
        }

        ElementManager em = ElementManager.getManager();
        // 2. 与其他敌人碰撞
        List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
        for(ElementObj en : enemys){
            if(en == this) continue;
            if(rect.intersects(en.getRectangle())){
                return false; // 不伤血，只阻挡
            }
        }
        // 3. 与玩家碰撞 -> 互相扣血，阻挡
        List<ElementObj> players = em.getElementsByKey(GameElement.PLAY);
        for(ElementObj p : players){
            if(rect.intersects(p.getRectangle())){
                int damage = Math.min(p.getHp(), this.getHp());
                p.setLive(true, damage);
                this.setLive(true, damage);
                return false;
            }
        }
        return true;
    }

    public void Turn(){
        long currentTime = System.currentTimeMillis();
        if(currentTime-lastTurnTime < 2000){
            return;
        }
        lastTurnTime = currentTime;
        int randfx = rand.nextInt(4);
        switch (randfx) {
            case 0:
                this.down = false;
                this.up = false;
                this.right = false;
                this.left = true;
                this.fx = "left";
                break;
            case 1:
                this.down = false;
                this.right = false;
                this.left = false;
                this.up = true;
                this.fx = "up";
                break;
            case 2:
                this.left = false;
                this.down = false;
                this.up = false;
                this.right = true;
                this.fx = "right";
                break;
            case 3:
                this.up = false;
                this.left = false;
                this.right = false;
                this.down = true;
                this.fx = "down";
                break;
        }

        // 方向改变后同步更新图片
        updateImage();
    }

    @Override
    protected void updateImage(){
        String path;
        if("bot".equals(skinType)){
            path = "image/tank/bot/bot_" + fx + ".png";
        }else{
            path = "image/tank/play2/player2_" + fx + ".png";
        }
        this.setIcon(new ImageIcon(path));
    }

    // 每 2 秒发射一颗蓝色子弹
    @Override
    protected void add(){
        long now = System.currentTimeMillis();
        if(now - lastFireTime < 2000){
            return;
        }
        lastFireTime = now;
        ElementObj bullet = new EnemyFile().createElement(this.toString());
        ElementManager.getManager().addElement(bullet, GameElement.ENEMYFILE);
    }

    @Override
    public void die(){
        String pos = "x:"+this.getX()+",y:"+this.getY();
        ElementManager em = ElementManager.getManager();
        em.addElement(new Boom().createElement(pos), GameElement.DIE);
        // 25% 掉落道具
        if(Math.random()<0.25){
            String type = Math.random()<0.5?"HEAL":"DOUBLE";
            em.addElement(new Tool().createElement(pos+",type:"+type), GameElement.TOOL);
        }
    }

    // 生成子弹坐标字符串，与 Play 相同逻辑
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

    public String getFx(){return fx;}
    public void setFx(String fx){this.fx=fx;}

    // 兼容 Boss
    public int getHealth(){return this.getHp();}
    public void setHealth(int hp){this.setHp(hp);}
}
