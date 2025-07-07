package com.tedu.util;

import javax.sound.sampled.*;
import java.io.File;

public class SoundUtil {
    private static Clip bgmClip;

    // 播放一次性效果音
    public static void playEffect(String path){
        try(AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path))){
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        }catch(Exception ignored){}
    }

    // 循环播放 BGM，如果已经有 BGM 在播放则先停止
    public static void playBgm(String path){
        if(bgmClip!=null && bgmClip.isRunning()) return; // 已在播放
        stopBgm();
        try(AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path))){
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }catch(Exception ignored){}
    }

    public static void stopBgm(){
        if(bgmClip!=null){
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }
} 