package com.tedu.controller;

import com.tedu.element.ElementObj;
import com.tedu.element.Enemy;
import com.tedu.element.Boss;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.element.TrackingBullet;
import java.util.List;
import java.util.Map;
import com.tedu.util.SoundUtil;
import javax.swing.*;

//说明游戏主线程，用于控制游戏加载，游戏关卡，运行时自动化，判定
//地图切换，资源释放和重新读取
public class GameThread extends Thread {
    private ElementManager em;
    private int currentLevel = 1;          // 当前关卡编号，从 1 开始
    private static final int MAX_LEVEL = 10; // 目前最多 10 关（对应 1.map~10.map）
    private volatile boolean active = true;  // 用于安全终止线程
    private com.tedu.show.GameJFrame frame;
    private long lastToolSpawn = System.currentTimeMillis();

    public GameThread() {
        em = ElementManager.getManager();
    }

    public GameThread(int startLevel, com.tedu.show.GameJFrame frame) {
        this();
        this.currentLevel = startLevel;
        this.frame = frame;
    }

    public GameThread(int startLevel){
        this(startLevel, null);
    }

    @Override
    public void run() {//主线程
        while(active)
        {
            gameLoad();
            gameRun();
            gameOver();
            try {
                sleep(50);
            }catch(InterruptedException e){
                if(!active){
                    break;
                }
            }
        }

    }

    private void gameRun(){
        while(active)//true可以变成变量，用于控制关卡结束
        {
            Map<GameElement, List<ElementObj>> all =em.getGameElements();
            List<ElementObj>enemys = em.getElementsByKey(GameElement.ENEMY);
            List<ElementObj>files = em.getElementsByKey(GameElement.PLAYFILE);
            List<ElementObj>enemyFiles = em.getElementsByKey(GameElement.ENEMYFILE);
            List<ElementObj>maps = em.getElementsByKey(GameElement.MAPS);
            moveAndUpdate(all);

            ElementPk(enemys,files);
            ElementPk(files,maps);
            ElementPk(enemyFiles,maps);        // 敌人子弹撞墙
            List<ElementObj>players = em.getElementsByKey(GameElement.PLAY);
            List<ElementObj>tools = em.getElementsByKey(GameElement.TOOL);
            ElementPk(players, enemyFiles);    // 敌人子弹击中玩家
            ElementPk(players, tools);        // 玩家拾取道具

            // 第10关定时生成道具
            if(currentLevel==10 && System.currentTimeMillis()-lastToolSpawn>=20000){
                spawnRandomTool();
                lastToolSpawn = System.currentTimeMillis();
            }

            // 如果当前关卡没有敌人，关卡结束
            if(enemys.size()==0){
                if(currentLevel==MAX_LEVEL){
                    // 通关
                    SoundUtil.stopBgm();
                    if(frame!=null){
                        // 停用键盘监听，防止误触
                        frame.disableKeyListener();
                        JOptionPane.showMessageDialog(frame,
                                "恭喜你成功通关！",
                                "游戏胜利",
                                JOptionPane.INFORMATION_MESSAGE);
                        frame.showMenu();
                    }
                    terminate();
                    return;
                }
                // 1-9 关所有敌人被消灭，提示进入下一关（阻塞，等待玩家确认）
                if(frame!=null){
                    javax.swing.JOptionPane.showMessageDialog(frame,
                            "已消灭所有敌人，进入下一关！",
                            "关卡完成",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }else{
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "已消灭所有敌人，进入下一关！",
                            "关卡完成",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
                // 玩家确认后进入下一关
                currentLevel++;
                return; // 跳出 gameRun，回到 run() 重新加载
            }
            try{
                sleep(33);
            }catch (InterruptedException e){
                if(!active){
                    break;
                }
            }
        }
    }
    //物体之间碰撞
    public void ElementPk(List<ElementObj>listA,List<ElementObj>listB){

        for(int i = 0 ; i < listA.size();i++) {
            ElementObj a = listA.get(i);
            for(int j = 0; j < listB.size();j++){
                ElementObj b = listB.get(j);
                if(!a.pk(b)) continue;

                // 处理子弹 VS 地图
                if((a instanceof com.tedu.element.PlayFile || a instanceof com.tedu.element.EnemyFile || a instanceof com.tedu.element.TrackingBullet) && b instanceof com.tedu.element.MapObj){
                    com.tedu.element.MapObj map = (com.tedu.element.MapObj)b;
                    String name = map.getName();
                    if("GRASS".equals(name)){
                        // 草丛：子弹直接穿过
                        continue;
                    }
                    if("RIVER".equals(name)){
                        // 河水：子弹消失但河水不损坏
                        a.setLive(false);
                        break;
                    }
                    // 砖块或铁墙等
                    if(a instanceof com.tedu.element.PlayFile){
                        // 玩家子弹：对墙体造成伤害
                        map.setLive(false, a.getAttack());
                    }
                    if(a instanceof com.tedu.element.TrackingBullet){
                        // 追踪弹：摧毁地图但保留自身
                        map.setLive(false);
                    }else{
                        // 其它子弹销毁
                        a.setLive(false);
                    }
                    break;
                }

                // 玩家拾取道具
                if(a instanceof com.tedu.element.Play && b instanceof com.tedu.element.Tool){
                    com.tedu.element.Play pl = (com.tedu.element.Play)a;
                    com.tedu.element.Tool tool = (com.tedu.element.Tool)b;
                    pl.applyTool(tool.getType());
                    b.setLive(false);
                    continue;
                }

                // 玩家被追踪弹击中 -> Boss 恢复 20 HP
                if(a instanceof com.tedu.element.Play && b instanceof com.tedu.element.TrackingBullet){
                    com.tedu.element.TrackingBullet tb = (com.tedu.element.TrackingBullet)b;
                    com.tedu.element.Boss boss = tb.getOwner();
                    if(boss!=null && boss.isLive()){
                        boss.setHp(Math.min(boss.getHp()+20, 100));
                    }
                }

                // 通用逻辑
                a.setLive(true, b.getAttack());
                b.setLive(false);
                break;
            }
        }
    }
    //游戏元素自动化方法
    public void moveAndUpdate(Map<GameElement, List<ElementObj>> all){
        for(GameElement ge:GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            for(int i = 0; i < list.size();i++) {
                ElementObj obj = list.get(i);
                if(!obj.isLive()){//如果死亡
                    obj.die();
                    list.remove(i--);
                    continue;
                }
                obj.model();
            }
        }
    }

    private void gameOver(){}


    private void gameLoad(){
        // 重新初始化元素容器（清空上一关残留）
        em.init();

        GameLoad.LoadImg();//加载图片（可多次调用，不影响）
        GameLoad.MapLoad(currentLevel);//根据关卡号加载对应地图
        //加载主角
        GameLoad.LoadPlay();
        //加载敌人
        if(currentLevel==10){
            em.addElement(new Boss().createElement(""), GameElement.ENEMY);
        }else{
            for(int i=0;i<6;i++){
                em.addElement(new Enemy().createElement(""), GameElement.ENEMY);
            }
        }
        // 随机生成 5 个道具
        java.util.Random rand = new java.util.Random();
        for(int i=0;i<5;i++){
            int x,y; boolean collide;
            List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
            do{
                x = rand.nextInt(900-30);
                y = rand.nextInt(600-30);
                com.tedu.element.Tool toolTemp = new com.tedu.element.Tool();
                toolTemp.setW(30); toolTemp.setH(30);
                toolTemp.setX(x); toolTemp.setY(y);
                java.awt.Rectangle r = toolTemp.getRectangle();
                collide=false;
                for(ElementObj m: maps){
                    if(r.intersects(m.getRectangle())){collide=true;break;}
                }
            }while(collide);
            int t = rand.nextInt(3);
            String type = t==0?"HEAL":(t==1?"DOUBLE":"INVINCIBLE");
            em.addElement(new com.tedu.element.Tool().createElement("x:"+x+",y:"+y+",type:"+type), GameElement.TOOL);
        }
        //全部加载完成，游戏启动

        if(frame != null){
            frame.setTitle(com.tedu.show.LevelSelectDialog.getLevelName(currentLevel));
            if(frame.getGamePanel()!=null){
                frame.getGamePanel().setLevel(currentLevel);
            }
        }

        // 播放/切换背景音乐
        SoundUtil.playBgm("audio/bgm.wav");
    }

    // 外部调用，安全终止线程
    public void terminate(){
        active = false;
        this.interrupt(); // 打断 sleep
        SoundUtil.stopBgm();
    }

    private void spawnRandomTool(){
        java.util.Random rand = new java.util.Random();
        List<ElementObj> maps = em.getElementsByKey(GameElement.MAPS);
        int x,y; boolean collide;
        do{
            x = rand.nextInt(900-30);
            y = rand.nextInt(600-30);
            com.tedu.element.Tool tmp = new com.tedu.element.Tool();
            tmp.setW(30); tmp.setH(30);
            tmp.setX(x); tmp.setY(y);
            java.awt.Rectangle r = tmp.getRectangle();
            collide = false;
            for(ElementObj m: maps){
                if(r.intersects(m.getRectangle())){ collide=true; break; }
            }
        }while(collide);

        int t = rand.nextInt(3);
        String typeStr = t==0?"HEAL": (t==1?"DOUBLE":"INVINCIBLE");
        em.addElement(new com.tedu.element.Tool().createElement("x:"+x+",y:"+y+",type:"+typeStr), GameElement.TOOL);
    }
}
