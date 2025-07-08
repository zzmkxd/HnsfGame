package com.tedu.show;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 关卡选择窗口，显示 10 个按钮，每个按钮带名称。
 */
public class LevelSelectDialog extends JDialog {
    private static final String[] LEVEL_NAMES = {
            "第一关：死亡峡谷",
            "第二关：烈焰荒原",
            "第三关：冰封雪域",
            "第四关：幽暗丛林",
            "第五关：沙漠风暴",
            "第六关：钢铁工厂",
            "第七关：城市废墟",
            "第八关：熔岩地心",
            "第九关：风暴海岸",
            "第十关：王者之巅"
    };

    public LevelSelectDialog(GameJFrame parent) {
        // 创建一个对话框，父窗口为parent，标题为"选择关卡"，模态为true
        super(parent, "选择关卡", true);
        // 设置对话框大小为400x400
        setSize(400, 400);
        // 将对话框居中显示
        setLocationRelativeTo(parent);

        // 创建一个面板，布局为5行2列，水平和垂直间距为10
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        // 遍历LEVEL_NAMES数组
        for(int i=0;i<LEVEL_NAMES.length;i++){
            // 获取关卡编号
            int level = i+1;
            // 创建一个按钮，文本为LEVEL_NAMES数组中的元素
            JButton btn = new JButton(LEVEL_NAMES[i]);
            // 为按钮添加点击事件监听器
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 调用父窗口的startGameLevel方法，传入关卡编号
                    parent.startGameLevel(level);
                    // 关闭对话框
                    dispose();
                }
            });
            // 将按钮添加到面板中
            panel.add(btn);
        }
        // 将面板添加到对话框中
        add(panel);
    }

    public static String getLevelName(int level){
        if(level>=1 && level<=LEVEL_NAMES.length){
            return LEVEL_NAMES[level-1];
        }
        return "第"+level+"关";
    }
} 