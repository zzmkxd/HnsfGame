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
    private boolean invBuff=false;
    private long invBuffEnd=0;
    private int baseSpeed = 7;
    private int curSpeed = 7;
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
        // 尺寸整体缩小 6px
        this.setW(this.getW()-6);
        this.setH(this.getH()-6);
        baseW = this.getW();
        baseH = this.getH();

        this.setHp(maxHp);
        this.setAttack(2);
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

        // 无敌或buff遮罩
        Color overlay = null;
        boolean invActive = isInvincible() || isInvBuffActive();
        if(invActive && dmgBuff){
            overlay = new Color(128,0,128,80); // 紫色
        }else if(invActive){
            overlay = new Color(0,0,255,80);
        }else if(dmgBuff){
            overlay = new Color(255,0,0,80);
        }
        if(overlay!=null){
            g2.setColor(overlay);
            int radius = Math.max(this.getW(), this.getH()) + 10;
            g2.fillOval(this.getX()-5, this.getY()-5, radius, radius);
        }

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
        List<ElementObj> mapsSnapshot = new java.util.ArrayList<>(ElementManager.getManager().getElementsByKey(GameElement.MAPS));
        for(ElementObj obj: mapsSnapshot){
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
                    this.right = this.up = this.down = false;
                    this.fx="left";
                    break;
                case java.awt.event.KeyEvent.VK_W: // W 上
                    this.up = true;
                    this.left = this.right = this.down = false;
                    this.fx="up";
                    break;
                case java.awt.event.KeyEvent.VK_D: // D 右
                    this.right = true;
                    this.left = this.up = this.down = false;
                    this.fx="right";
                    break;
                case java.awt.event.KeyEvent.VK_S: // S 下
                    this.down = true;
                    this.left = this.right = this.up = false;
                    this.fx="down";
                    break;
                case java.awt.event.KeyEvent.VK_J: // J 攻击
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
                case java.awt.event.KeyEvent.VK_J:
                    this.pkType=false; // 松开 J 结束连发
                break;
                case 32: // 兼容空格键停止攻击
                    this.pkType=false;
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
        // 检查invBuff过期
        if(invBuff && System.currentTimeMillis()>invBuffEnd){
            invBuff=false;
        }

        int speed = curSpeed;
        // 河流减速
        if(isInRiver()){
            speed = Math.max(1, speed-2);
        }
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
            if(nextY<550-this.getH() && canThrough(this.getX(),nextY,maps)){
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
        // 3. 与敌人碰撞
        List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
        for(ElementObj en: enemys){
            if(rect.intersects(en.getRectangle())){
                if(en instanceof com.tedu.element.Boss){
                    // Boss 碰撞：弹开并扣4血
                    this.setLive(true, 4);

                    int dx = nextX - this.getX();
                    int dy = nextY - this.getY();
                    if(Math.abs(dx) > Math.abs(dy)){
                        // 左右方向碰撞
                        if(dx < 0){ // 向左移动撞到
                            this.setX(en.getX() + en.getW() + 1);
                        }else{ // 向右
                            this.setX(en.getX() - this.getW() - 1);
                        }
                    }else{
                        // 上下方向碰撞
                        if(dy < 0){ // 向上
                            this.setY(en.getY() + en.getH() + 1);
                        }else{ // 向下
                            this.setY(en.getY() - this.getH() - 1);
                        }
                    }
                    return false;
                }
                // 其他敌人：双方扣较小生命值
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
        // 发射主子弹
        ElementManager em = ElementManager.getManager();
        createAndAddBullet(0,0, em);

        if(dmgBuff){
            // 额外左右/上下偏移的两颗子弹
            int offset = 10;
            switch(this.fx){
                case "up":
                case "down":
                    createAndAddBullet(-offset,0, em);
                    createAndAddBullet(offset,0, em);
                    break;
                case "left":
                case "right":
                    createAndAddBullet(0,-offset, em);
                    createAndAddBullet(0,offset, em);
                    break;
            }
        }
    }

    private void createAndAddBullet(int dx, int dy, ElementManager em){
        // 根据当前方向，调整起点字符串
        String base = this.toString(); // contains x:...,y:...
        String[] pts = base.split(",");
        int x=0,y=0; StringBuilder sb=new StringBuilder();
        for(String p: pts){
            if(p.startsWith("x:")){ x = Integer.parseInt(p.substring(2)); }
            if(p.startsWith("y:")){ y = Integer.parseInt(p.substring(2)); }
        }
        x += dx; y += dy;
        String data = String.format("x:%d,y:%d,f:%s", x,y,this.fx);
        ElementObj bullet = new PlayFile().createElement(data);
        bullet.setAttack(getCurrentAttack());
        em.addElement(bullet, GameElement.PLAYFILE);
        SoundUtil.playEffect("audio/shoot.wav");
    }

    //子弹生成位置   返回字符串{x:3,y:4,f:up}格式
    @Override
    public String toString(){
        int x = this.getX();
        int y = this.getY();
        switch(this.fx){
            case "up":x+=13;break;
            case "down":y+=21;x+=13;break;
            case "left":y+=13;break;
            case "right":x+=21;y+=13;break;
        }
        return "x:"+x+",y:"+y+",f:"+this.fx;
    }

    @Override
    public void setLive(boolean live,int atk){
        long now = System.currentTimeMillis();
        // 出生无敌或buff无敌
        if(now - spawnTime < 3000 || isInvBuffActive()){
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
        switch(type){
            case HEAL:
                this.setHp(Math.min(maxHp, this.getHp()+10));
                break;
            case DOUBLE:
                dmgBuff = true;
                buffEnd = System.currentTimeMillis()+6000; // 6 秒
                curSpeed = baseSpeed*2;
                break;
            case INVINCIBLE:
                invBuff = true;
                invBuffEnd = System.currentTimeMillis()+3000; // 3 秒
                break;
        }
    }

    private int getCurrentAttack(){
        return this.getAttack();
    }

    private boolean isInRiver(){
        List<ElementObj> maps = ElementManager.getManager().getElementsByKey(GameElement.MAPS);
        for(ElementObj obj : maps){
            if(obj instanceof MapObj){
                MapObj mo = (MapObj)obj;
                if("RIVER".equals(mo.getName()) && this.pk(mo)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInvincible(){
        return System.currentTimeMillis()-spawnTime < 3000;
    }

    private boolean isInvBuffActive(){
        return invBuff && System.currentTimeMillis()<invBuffEnd;
    }

}
