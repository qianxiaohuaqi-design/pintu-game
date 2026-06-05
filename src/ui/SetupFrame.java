package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class SetupFrame extends JFrame {

    private String username;
    
    // 选中的网格大小与模式，默认为 4x4
    private int selectedGridSize = 4;
    private boolean isChallengeMode = false;
    private boolean modeSelected = false;

    // 界面控件
    private JLabel difficultyLabel;
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

        // ── 2. 游戏模式选择 (调整到最上方，Y=120) ──
        JLabel modeLabel = new JLabel("选择模式：");
        modeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        modeLabel.setForeground(new Color(0x7B3A00));
        modeLabel.setBounds(70, 120, 90, 30);
        add(modeLabel);

        btnCasual = new ToggleModernButton("休闲模式");
        btnCasual.setBounds(170, 118, 115, 34);
        btnCasual.addActionListener(e -> selectMode(false));
        add(btnCasual);

        btnChallenge = new ToggleModernButton("挑战模式");
        btnChallenge.setBounds(295, 118, 115, 34);
        btnChallenge.addActionListener(e -> selectMode(true));
        add(btnChallenge);

        // ── 3. 网格难度选择 (调整到中下方，Y=185，初始隐藏) ──
        difficultyLabel = new JLabel("选择难度：");
        difficultyLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        difficultyLabel.setForeground(new Color(0x7B3A00));
        difficultyLabel.setBounds(70, 185, 90, 30);
        add(difficultyLabel);

        btn3x3 = new ToggleModernButton("3 x 3");
        btn3x3.setBounds(170, 183, 80, 34);
        btn3x3.addActionListener(e -> selectGridSize(3));
        add(btn3x3);

        btn4x4 = new ToggleModernButton("4 x 4");
        btn4x4.setBounds(260, 183, 80, 34);
        btn4x4.addActionListener(e -> selectGridSize(4));
        add(btn4x4);

        btn5x5 = new ToggleModernButton("5 x 5");
        btn5x5.setBounds(350, 183, 80, 34);
        btn5x5.addActionListener(e -> selectGridSize(5));
        add(btn5x5);

        // 默认将 4x4 设置为选中状态
        btn4x4.setSelected(true);

        // ── 4. 开始游戏按钮 (初始隐藏) ──
        startGameBtn = makeButton(IMG + "开始游戏.png", IMG + "开始游戏按下.png", 128, 47, () -> startGame());
        startGameBtn.setBounds(80, 280, 128, 47);
        add(startGameBtn);

        // ── 5. 返回登录按钮 (常驻可见) ──
        backBtn = makeButton(IMG + "返回登录.png", IMG + "返回登录按下.png", 128, 47, () -> backToLogin());
        backBtn.setBounds(260, 280, 128, 47);
        add(backBtn);

        // 初始设置难度和开始游戏按钮不可见
        setDifficultyAndStartVisible(false);

        // ── 6. 背景图片（最底层）──
        JLabel bg = new JLabel(loadScaled(IMG + "setup_background.png", 470, 390));
        bg.setBounds(0, 0, 470, 390);
        add(bg);
    }

    private void setDifficultyAndStartVisible(boolean visible) {
        difficultyLabel.setVisible(visible);
        btn3x3.setVisible(visible);
        btn4x4.setVisible(visible);
        btn5x5.setVisible(visible);
        startGameBtn.setVisible(visible);
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
        if (!modeSelected) {
            modeSelected = true;
            setDifficultyAndStartVisible(true);
            // 模式选择后，重绘一下面板确保刷新显示
            this.getContentPane().repaint();
        }
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
     * 自定义拟真 3D 切换式按钮，拥有悬停效果、点击高亮，并能在选中状态和普通状态下切换颜色
     */
    static class ToggleModernButton extends JButton {
        private boolean isSelectedState = false;

        ToggleModernButton(String text) {
            super(text);
            setFont(new Font("微软雅黑", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setSelected(boolean selected) {
            this.isSelectedState = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();

            // 1. 绘制投影阴影
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);

            // 2. 根据选中与否及触发状态选择 3D 渐变颜色
            Color gradStart;
            Color gradEnd;
            Color borderColor;

            if (isSelectedState) {
                // 亮眼森林绿 (选中)
                if (pressed) {
                    gradStart = new Color(0x1B5E20);
                    gradEnd = new Color(0x388E3C);
                } else if (rollover) {
                    gradStart = new Color(0x4CAF50);
                    gradEnd = new Color(0x236926);
                } else {
                    gradStart = new Color(0x43A047);
                    gradEnd = new Color(0x1B5E20);
                }
                borderColor = new Color(0xFFD700); // 金色发光边框
            } else {
                // 复古木质黄褐色 (未选中)
                if (pressed) {
                    gradStart = new Color(0x733E0F);
                    gradEnd = new Color(0xB57B36);
                } else if (rollover) {
                    gradStart = new Color(0xD49245);
                    gradEnd = new Color(0x824A16);
                } else {
                    gradStart = new Color(0xC68037);
                    gradEnd = new Color(0x733E0F);
                }
                borderColor = new Color(0x56300D); // 深木色边框
            }

            // 3. 填充主渐变色
            GradientPaint bgGrad = new GradientPaint(0, 0, gradStart, 0, h - 2, gradEnd);
            g2.setPaint(bgGrad);
            g2.fillRoundRect(1, 1, w - 3, h - 3, 10, 10);

            // 4. 绘制边框
            g2.setColor(borderColor);
            if (isSelectedState) {
                g2.setStroke(new BasicStroke(2.2f));
            } else {
                g2.setStroke(new BasicStroke(1.5f));
            }
            g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);

            // 5. 绘制上半部分的玻璃反光高光罩
            if (!pressed) {
                GradientPaint gloss = new GradientPaint(
                        0, 1, new Color(255, 255, 255, 80),
                        0, h / 2, new Color(255, 255, 255, 0)
                );
                g2.setPaint(gloss);
                g2.fillRoundRect(2, 2, w - 5, h / 2 - 1, 8, 8);
            }

            // 6. 选中时在右上角绘制一个小型的白勾指示器
            if (isSelectedState) {
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawLine(w - 14, 10, w - 11, 13);
                g2.drawLine(w - 11, 13, w - 7, 7);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── 辅助工具方法 ───────────────────────────────────────

    private JLabel makeButton(String normalPath, String pressedPath, int w, int h, Runnable action) {
        ImageIcon normalIcon  = loadCroppedAndTransparent(normalPath, w, h);
        ImageIcon pressedIcon = loadCroppedAndTransparent(pressedPath, w, h);
        JLabel btn = new JLabel(normalIcon);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    btn.setIcon(pressedIcon);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    btn.setIcon(normalIcon);
                    if (action != null && btn.contains(e.getPoint())) {
                        action.run();
                    }
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setIcon(normalIcon);
            }
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

    /**
     * 去除图片中的白底，精确定位内容并等比缩放
     */
    private ImageIcon loadCroppedAndTransparent(String path, int w, int h) {
        java.net.URL url = this.getClass().getResource("/" + path);
        BufferedImage raw = null;
        try {
            if (url != null) {
                raw = ImageIO.read(url);
            } else {
                File file = new File(path);
                if (file.exists()) {
                    raw = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (raw == null) {
            return new ImageIcon();
        }

        // 扫描不接近纯白的实际内容包围盒
        int minX = raw.getWidth();
        int minY = raw.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < raw.getHeight(); y++) {
            for (int x = 0; x < raw.getWidth(); x++) {
                int rgb = raw.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // 判断像素是否不是白底 (阈值设定在 250 以下)
                if (r < 250 || g < 250 || b < 250) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // 如果全部为白底，则不进行截取
        if (maxX < minX || maxY < minY) {
            minX = 0;
            minY = 0;
            maxX = raw.getWidth() - 1;
            maxY = raw.getHeight() - 1;
        }

        // 增加 2 像素边缘缓冲区以避免微小边缘锯齿被截断
        minX = Math.max(0, minX - 2);
        minY = Math.max(0, minY - 2);
        maxX = Math.min(raw.getWidth() - 1, maxX + 2);
        maxY = Math.min(raw.getHeight() - 1, maxY + 2);

        int cropW = maxX - minX + 1;
        int cropH = maxY - minY + 1;

        // 重构为具有 Alpha 通道的 BufferedImage
        BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < cropH; y++) {
            for (int x = 0; x < cropW; x++) {
                int rgb = raw.getRGB(minX + x, minY + y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (r >= 250 && g >= 250 && b >= 250) {
                    // 透明像素
                    cropped.setRGB(x, y, 0x00FFFFFF & rgb);
                } else {
                    cropped.setRGB(x, y, rgb);
                }
            }
        }

        Image scaled = cropped.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
