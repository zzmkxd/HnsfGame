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
        super(parent, "游戏结束", true);
        setSize(300, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("你失败了！", SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, 18));
        add(label, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnMenu = new JButton("返回菜单");
        JButton btnRetry = new JButton("重新挑战");

        // 取消按钮聚焦，避免键盘触发
        btnMenu.setFocusable(false);
        btnRetry.setFocusable(false);
        getRootPane().setDefaultButton(null);

        btnMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                parent.showMenu();
            }
        });
        btnRetry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                parent.startGameLevel(level);
            }
        });
        btnPanel.add(btnMenu);
        btnPanel.add(btnRetry);
        add(btnPanel, BorderLayout.SOUTH);
    }
} 