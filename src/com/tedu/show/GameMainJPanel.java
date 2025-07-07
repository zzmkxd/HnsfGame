package com.tedu.show;

import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import com.tedu.show.GameOverDialog;

public class GameMainJPanel extends JPanel implements Runnable {
    private ElementManager em;
    private boolean running = false;
    private GameJFrame frame;
    private int level = 1;
    private boolean gameOverShown = false; // 避免多次弹窗

    public GameMainJPanel() {
        this(1);
    }

    public GameMainJPanel(int level){
        this.level = level;
        init();
        setOpaque(true);
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
    }

    public void setGameFrame(GameJFrame frame) {
        this.frame = frame;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    private void init() {
        em = ElementManager.getManager();
    }

    // 返回主菜单（保持不变）
    public void returnToMenu() {
        if (frame != null) {
            running = false;
            frame.showMenu();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 保证面板尺寸跟随父容器（窗口）
        if(getParent()!=null){
            Dimension parentSize = getParent().getSize();
            if(!parentSize.equals(getSize())){
                setSize(parentSize);
            }
        }

        Graphics2D g2 = (Graphics2D) g.create();
        double scaleX = getWidth()/900.0;
        double scaleY = getHeight()/600.0;
        g2.scale(scaleX, scaleY);

        Map<GameElement, List<ElementObj>> all = em.getGameElements();
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            for (int i = 0; i < list.size(); i++) {
                ElementObj obj = list.get(i);
                obj.showElement(g2);
            }
        }
        g2.dispose();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            repaint();

            // 检测玩家是否死亡
            List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
            List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
            if(plays.size()==0 && enemys.size()!=0 && !gameOverShown){
                gameOverShown = true;
                if(frame!=null){
                    frame.disableKeyListener();
                }
                showGameOverDialog();
            }
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void showGameOverDialog(){
        SwingUtilities.invokeLater(() -> {
            new GameOverDialog(frame, level).setVisible(true);
            running = false; // dialog关闭后停止刷新循环
        });
    }
}