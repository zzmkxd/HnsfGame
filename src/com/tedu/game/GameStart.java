package com.tedu.game;

import com.tedu.controller.GameListener;
import com.tedu.controller.GameThread;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;
import com.tedu.util.SoundUtil;

//程序的唯一入口
public class GameStart {
    public static void main(String[] args) {
        GameJFrame gj = new GameJFrame();
        //实例化面板，注入到jframe中
        GameMainJPanel jp = new GameMainJPanel();
        GameListener listener = new GameListener();
        //实例化主线程
        GameThread th = new GameThread();

        // 播放背景音乐
        SoundUtil.playBgm("audio/bgm.wav");

        gj.setPanel(jp);
        gj.setKeyListener(listener);
        gj.setThread(th);
        gj.start();//显示窗体
    }
}
