package com.tedu.show;

//游戏窗体 主要实现窗口的关闭，最大最小化，显示


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.tedu.controller.GameThread;
import com.tedu.controller.GameListener;
import com.tedu.show.LevelSelectDialog;


//面板绑定到窗体
//监听绑定
//游戏主线程启动
//显示窗体


public class GameJFrame extends JFrame {
    public static int GameX = 900;
    public static int GameY = 600;
    private JPanel jPanel = null;//正在显示的面板
    private KeyListener keyListener = null;//键盘监听
    private MouseMotionListener mouseMotionListener = null;//鼠标监听
    private MouseListener mouseListener = null;
    private Thread thread = null;//主线程
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private MainMenuPanel menuPanel;
    private GameMainJPanel gamePanel;


    public GameJFrame() {
        init();
    }
    public void init() {
        this.setSize(GameX, GameY);
        this.setTitle("坦克大战");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);//窗口初始生成在屏幕正中间
        // 使用卡片布局管理面板切换
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        setContentPane(contentPanel);
        // 初始化菜单和游戏面板（注意游戏面板不传入参数，保持原有构造）
        menuPanel = new MainMenuPanel(this);
        gamePanel = new GameMainJPanel(); // 直接使用原有构造函数

        contentPanel.add(menuPanel, "MENU");
        contentPanel.add(gamePanel, "GAME");

        // 显示菜单
        showMenu();
    }




    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public KeyListener getKeyListener(){
        return this.keyListener;
    }

    public void disableKeyListener(){
        if(this.keyListener != null){
            this.removeKeyListener(this.keyListener);
        }
    }

    public void setMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.mouseMotionListener = mouseMotionListener;
    }

    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
    //窗体布局,存档，读档。。。。。。。。。。。。。。。。。。。自由发挥
    public void addButton(){
        //this.setLayout(null);
    }
    public void setPanel(JPanel panel) {
        this.jPanel = panel;
    }


    public void setPanel(GameMainJPanel panel) {
        this.gamePanel = panel;
        gamePanel.setGameFrame(this); // 注入主窗口引用
        contentPanel.add(gamePanel, "GAME");
    }

    public GameMainJPanel getGamePanel(){
        return this.gamePanel;
    }

    public void start(){
        if(jPanel!=null){
            this.add(jPanel);
        }
        if(keyListener!=null){
            this.addKeyListener(keyListener);
        }
        if(thread!=null){
            thread.start();
        }
        //界面刷新
        this.setVisible(true);
        //如果jp是runnable的子类实体对象
        if(this.jPanel instanceof Runnable)
        {
            new Thread((Runnable) this.jPanel).start();
            System.out.println("是否启动");
        }
    }
    // 显示游戏面板并设置菜单栏
    public void startGame() {
        cardLayout.show(contentPanel, "GAME");
        this.requestFocusInWindow();             // JFrame 抢回焦点
        createGameMenuBar(); // 创建游戏中的菜单栏

        if (gamePanel instanceof Runnable) {
            new Thread((Runnable) gamePanel).start();
        }
    }

    // 创建游戏中的菜单栏
    private void createGameMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        JMenuItem returnItem = new JMenuItem("返回菜单", KeyEvent.VK_R);
        returnItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        returnItem.addActionListener(e -> gamePanel.returnToMenu());

        JMenuItem exitItem = new JMenuItem("退出游戏", KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(returnItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar); // 在JFrame上设置菜单栏
    }

    // 显示菜单时移除菜单栏
    public void showMenu() {
        cardLayout.show(contentPanel, "MENU");
        setJMenuBar(null); // 移除菜单栏

        // 回到菜单时也停止当前线程
        if(this.thread != null){
            if(this.thread instanceof com.tedu.controller.GameThread){
                ((com.tedu.controller.GameThread)this.thread).terminate();
            }else{
                this.thread.interrupt();
            }
            this.thread = null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();      // 面板显示出来后马上要焦点
    }

    // 新增方法：根据关卡号启动游戏
    public void startGameLevel(int level){
        // 移除旧的游戏面板（如有）
        if(gamePanel!=null){
            contentPanel.remove(gamePanel);
        }

        // 停止旧的主线程，防止速度叠加
        if(this.thread != null){
            if(this.thread instanceof com.tedu.controller.GameThread){
                ((com.tedu.controller.GameThread)this.thread).terminate();
            }else{
                this.thread.interrupt();
            }
        }

        gamePanel = new GameMainJPanel(level);
        gamePanel.setGameFrame(this);
        contentPanel.add(gamePanel, "GAME");

        // 更新标题
        this.setTitle(LevelSelectDialog.getLevelName(level));

        // 创建并绑定监听、线程
        GameListener listener = new GameListener();
        GameThread thread = new GameThread(level, this);

        // 先移除旧监听，避免多次添加
        for(KeyListener kl : this.getKeyListeners()){
            this.removeKeyListener(kl);
        }
        this.setKeyListener(listener);
        this.addKeyListener(listener);

        this.setThread(thread);

        // 启动主线程
        thread.start();

        cardLayout.show(contentPanel, "GAME");
        this.requestFocusInWindow();
        createGameMenuBar();

        if(gamePanel instanceof Runnable){
            new Thread((Runnable)gamePanel).start();
        }
    }
}
