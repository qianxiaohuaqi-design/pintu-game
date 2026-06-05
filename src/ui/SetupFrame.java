package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class SetupFrame extends JFrame {

    private String username;
    
    // 选中的网格大小与模式，默认为 4x4 休闲模式
    private int selectedGridSize = 4;
    private boolean isChallengeMode = false;

    // 界面控件
    private ToggleModernButton btn3x3;
    private ToggleModernButton btn4x4;
    private ToggleModernButton btn5x5;
    private ToggleModernButton btnCasual;
    private ToggleModernButton btnChallenge;

    private JLabel startGameBtn;
    private JLabel backBtn;

    // 背景图与按钮图片路径
    private static final String IMG = "image/login/";

    public SetupFrame(String username) {
        this.username = username;
        initFrame();
        initComponents();
        this.setVisible(true);
    }

    private void initFrame() {
        this.setTitle("拼图游戏 · 模式选择");
        this.setSize(488, 430);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.getContentPane().setLayout(null);
    }

    private void initComponents() {
        // ── 1. 窗口标题 ──
        JLabel titleLabel = new JLabel("游戏设置", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0x7B3A00));
        titleLabel.setBounds(0, 40, 488, 40);
        add(titleLabel);

        // ── 2. 网格难度选择 ──
        JLabel difficultyLabel = new JLabel("选择难度：");
        difficultyLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        difficultyLabel.setForeground(new Color(0x7B3A00));
        difficultyLabel.setBounds(70, 120, 90, 30);
        add(difficultyLabel);

        btn3x3 = new ToggleModernButton("3 x 3");
        btn3x3.setBounds(170, 118, 80, 34);
        btn3x3.addActionListener(e -> selectGridSize(3));
        add(btn3x3);

        btn4x4 = new ToggleModernButton("4 x 4");
        btn4x4.setBounds(260, 118, 80, 34);
        btn4x4.addActionListener(e -> selectGridSize(4));
        add(btn4x4);

        btn5x5 = new ToggleModernButton("5 x 5");
        btn5x5.setBounds(350, 118, 80, 34);
        btn5x5.addActionListener(e -> selectGridSize(5));
        add(btn5x5);

        // 默认选中 4x4
        btn4x4.setSelected(true);

        // ── 3. 游戏模式选择 ──
        JLabel modeLabel = new JLabel("选择模式：");
        modeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        modeLabel.setForeground(new Color(0x7B3A00));
        modeLabel.setBounds(70, 185, 90, 30);
        add(modeLabel);

        btnCasual = new ToggleModernButton("休闲模式");
        btnCasual.setBounds(170, 183, 115, 34);
        btnCasual.addActionListener(e -> selectMode(false));
        add(btnCasual);

        btnChallenge = new ToggleModernButton("挑战模式");
        btnChallenge.setBounds(295, 183, 115, 34);
        btnChallenge.addActionListener(e -> selectMode(true));
        add(btnChallenge);

        // 默认选中 休闲模式
        btnCasual.setSelected(true);

        // ── 4. 开始游戏按钮 ──
        startGameBtn = makeButton(IMG + "开始游戏.png", IMG + "开始游戏按下.png", 128, 47);
        startGameBtn.setBounds(80, 280, 128, 47);
        startGameBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startGame();
            }
        });
        add(startGameBtn);

        // ── 5. 返回登录按钮 ──
        backBtn = makeButton(IMG + "返回登录.png", IMG + "返回登录按下.png", 128, 47);
        backBtn.setBounds(260, 280, 128, 47);
        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                backToLogin();
            }
        });
        add(backBtn);

        // ── 6. 背景图片（最底层）──
        JLabel bg = new JLabel(loadScaled(IMG + "setup_background.png", 470, 390));
        bg.setBounds(0, 0, 470, 390);
        add(bg);
    }

    private void selectGridSize(int size) {
        this.selectedGridSize = size;
        btn3x3.setSelected(size == 3);
        btn4x4.setSelected(size == 4);
        btn5x5.setSelected(size == 5);
    }

    private void selectMode(boolean challenge) {
        this.isChallengeMode = challenge;
        btnCasual.setSelected(!challenge);
        btnChallenge.setSelected(challenge);
    }

    private void startGame() {
        this.setVisible(false);
        this.dispose();
        // 启动主游戏窗口，传入用户名、网格大小与挑战模式开关
        new GameFrame(username, selectedGridSize, isChallengeMode);
    }

    private void backToLogin() {
        this.setVisible(false);
        this.dispose();
        new LoginFrame();
    }

    // ── 控件样式定义 ────────────────────────────────────────

    /**
     * 自定义现代切换式按钮，拥有悬停效果、点击高亮，并能在选中状态和普通状态下切换颜色
     */
    static class ToggleModernButton extends JButton {
        private boolean isSelectedState = false;

        // 默认金色系
        private static final Color GOLD_BG = new Color(0xC49A3C);
        private static final Color GOLD_HOVER = new Color(0xD8A84A);
        private static final Color GOLD_ACTIVE = new Color(0xAB842F);

        // 选中绿色系
        private static final Color GREEN_BG = new Color(0x3E8E41);
        private static final Color GREEN_HOVER = new Color(0x4CAF50);
        private static final Color GREEN_ACTIVE = new Color(0x2E7D32);

        private Color currentBg = GOLD_BG;

        ToggleModernButton(String text) {
            super(text);
            setFont(new Font("微软雅黑", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentBg = isSelectedState ? GREEN_HOVER : GOLD_HOVER;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentBg = isSelectedState ? GREEN_BG : GOLD_BG;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    currentBg = isSelectedState ? GREEN_ACTIVE : GOLD_ACTIVE;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentBg = isSelectedState ? GREEN_HOVER : GOLD_HOVER;
                    repaint();
                }
            });
        }

        public void setSelected(boolean selected) {
            this.isSelectedState = selected;
            this.currentBg = selected ? GREEN_BG : GOLD_BG;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── 辅助工具方法 ───────────────────────────────────────

    private JLabel makeButton(String normalPath, String pressedPath, int w, int h) {
        ImageIcon normalIcon  = loadScaled(normalPath, w, h);
        ImageIcon pressedIcon = loadScaled(pressedPath, w, h);
        JLabel btn = new JLabel(normalIcon);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { btn.setIcon(pressedIcon); }
            @Override public void mouseReleased(MouseEvent e) { btn.setIcon(normalIcon);  }
            @Override public void mouseExited(MouseEvent e)   { btn.setIcon(normalIcon);  }
        });
        return btn;
    }

    private ImageIcon loadScaled(String path, int w, int h) {
        java.net.URL url = this.getClass().getResource("/" + path);
        ImageIcon raw;
        if (url != null) {
            raw = new ImageIcon(url);
        } else {
            raw = new ImageIcon(path);
        }
        if (raw.getIconWidth() <= 0) {
            return new ImageIcon();
        }
        Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
