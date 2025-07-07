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

        // 开始游戏按钮（点击后跳转到GameMainJPanel）
        JButton startButton = new JButton("开始游戏");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 24));
        startButton.setBounds(350, 300, 200, 60);
        startButton.addActionListener(e -> {
            new LevelSelectDialog(frame).setVisible(true);
        });
        background.add(startButton);
        // 退出按钮
        JButton exitButton = new JButton("退出游戏");
        exitButton.setFont(new Font("微软雅黑", Font.BOLD, 24));
        exitButton.setBounds(350, 400, 200, 60);
        exitButton.addActionListener(e -> System.exit(0));
        background.add(exitButton);
    }
}