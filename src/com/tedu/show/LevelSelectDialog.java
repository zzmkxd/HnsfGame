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
        super(parent, "选择关卡", true);
        setSize(400, 400);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        for(int i=0;i<LEVEL_NAMES.length;i++){
            int level = i+1;
            JButton btn = new JButton(LEVEL_NAMES[i]);
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.startGameLevel(level);
                    dispose();
                }
            });
            panel.add(btn);
        }
        add(panel);
    }

    public static String getLevelName(int level){
        if(level>=1 && level<=LEVEL_NAMES.length){
            return LEVEL_NAMES[level-1];
        }
        return "第"+level+"关";
    }
} 