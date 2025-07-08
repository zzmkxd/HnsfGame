package com.tedu.show;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 游戏结束对话框，只能用鼠标点击，空格无效。
 */
public class GameOverDialog extends JDialog {
    public GameOverDialog(GameJFrame parent, int level) {
        // 创建一个模态对话框，父窗口为parent，标题为"游戏结束"，模态为true
        super(parent, "游戏结束", true);
        // 设置对话框大小为300*150
        setSize(300, 150);
        // 将对话框居中显示
        setLocationRelativeTo(parent);
        // 设置对话框布局为BorderLayout
        setLayout(new BorderLayout());

        // 创建一个标签，显示"你失败了！"，居中对齐
        JLabel label = new JLabel("你失败了！", SwingConstants.CENTER);
        // 设置标签字体为微软雅黑，加粗，大小为18
        label.setFont(new Font("微软雅黑", Font.BOLD, 18));
        // 将标签添加到对话框中央
        add(label, BorderLayout.CENTER);

        // 创建一个面板，用于放置按钮
        JPanel btnPanel = new JPanel();
        // 创建返回菜单按钮
        JButton btnMenu = new JButton("返回菜单");
        // 创建重新挑战按钮
        JButton btnRetry = new JButton("重新挑战");

        // 取消按钮聚焦，避免键盘触发
        btnMenu.setFocusable(false);
        btnRetry.setFocusable(false);
        // 设置默认按钮为空
        getRootPane().setDefaultButton(null);

        // 为返回菜单按钮添加点击事件
        btnMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dispose();
                // 显示菜单
                parent.showMenu();
            }
        });
        // 为重新挑战按钮添加点击事件
        btnRetry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dispose();
                // 重新开始当前关卡
                parent.startGameLevel(level);
            }
        });
        // 将按钮添加到面板中
        btnPanel.add(btnMenu);
        btnPanel.add(btnRetry);
        // 将面板添加到对话框底部
        add(btnPanel, BorderLayout.SOUTH);
    }
} 