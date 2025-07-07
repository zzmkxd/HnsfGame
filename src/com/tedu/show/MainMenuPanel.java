package com.tedu.show;

import javax.swing.*;
import java.awt.*;
import com.tedu.show.LevelSelectDialog;

public class MainMenuPanel extends JPanel {
    private GameJFrame frame;

    public MainMenuPanel(GameJFrame frame) {
        this.frame = frame;
        setLayout(null);

        // 设置指定背景图片
        JLabel background = new JLabel(new ImageIcon("image/login_background.png"));
        background.setBounds(0, 0, GameJFrame.GameX, GameJFrame.GameY);
        add(background);

        int btnW = 200, btnH = 60;
        // 计算居中位置的方法
        java.util.function.BiConsumer<JButton, Integer> reposition = (btn, offsetY) -> {
            int w = getWidth();
            int h = getHeight();
            btn.setBounds((w-btnW)/2, h/2 + offsetY, btnW, btnH);
        };

        // 开始游戏按钮（点击后跳转到GameMainJPanel）
        JButton startButton = new JButton("开始游戏");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 24));
        startButton.setBounds((GameJFrame.GameX-btnW)/2, GameJFrame.GameY/2-50, btnW, btnH);
        startButton.addActionListener(e -> {
            new LevelSelectDialog(frame).setVisible(true);
        });
        background.add(startButton);
        // 退出按钮
        JButton exitButton = new JButton("退出游戏");
        exitButton.setFont(new Font("微软雅黑", Font.BOLD, 24));
        exitButton.setBounds((GameJFrame.GameX-btnW)/2, GameJFrame.GameY/2+50, btnW, btnH);
        exitButton.addActionListener(e -> System.exit(0));
        background.add(exitButton);

        // 监听面板尺寸变化，实时调整布局
        this.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override
            public void componentResized(java.awt.event.ComponentEvent e){
                int w = getWidth();
                int h = getHeight();
                background.setBounds(0,0,w,h);
                reposition.accept(startButton, -btnH-10);
                reposition.accept(exitButton, 10);
            }
        });
    }
}