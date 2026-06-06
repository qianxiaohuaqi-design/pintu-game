package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.ScrollPaneConstants;

public class GameFrame extends JFrame implements KeyListener, ActionListener {
    // 定义静态内部类表示盘面状态
    static class BoardState {
        int[][] data;
        int x, y; // 空白位置

        BoardState(int[][] arr, int x, int y, int size) {
            this.data = new int[size][size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(arr[i], 0, this.data[i], 0, size);
            }
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BoardState) {
                BoardState other = (BoardState) obj;
                if (this.data.length != other.data.length) return false;
                for (int i = 0; i < this.data.length; i++) {
                    for (int j = 0; j < this.data[i].length; j++) {
                        if (this.data[i][j] != other.data[i][j]) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < this.data.length; i++) {
                for (int j = 0; j < this.data[i].length; j++) {
                    hash = 31 * hash + this.data[i][j];
                }
            }
            return hash;
        }
    }

    // 自定义带有呼吸灯高亮功能的图片展示 Label
    static class TileLabel extends JLabel {
        private boolean isHighlighted = false;
        static double alpha = 0.5; // 共享的呼吸透明度

        TileLabel(Icon icon) {
            super(icon);
        }

        void setHighlighted(boolean highlighted) {
            this.isHighlighted = highlighted;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (isHighlighted) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制半透明橘红色遮罩层，Alpha 范围在 0.2 ~ 0.8 之间连续波动
                int alphaVal = (int) (alpha * 255);
                g2.setColor(new Color(255, 69, 0, alphaVal)); // 橘红色呼吸遮罩
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 绘制金色外框作为轮廓线，增加质感
                g2.setColor(new Color(255, 215, 0, alphaVal));
                g2.setStroke(new java.awt.BasicStroke(4.0f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);

                g2.dispose();
            }
        }
    }

    // 自定义现代化控制栏按钮
    static class ModernButton extends JButton {
        private boolean isActiveState = false;

        ModernButton(String text) {
            super(text);
            setFont(new Font("微软雅黑", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFocusable(false); // 避免按钮抢夺键盘焦点
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new java.awt.Dimension(110, 34));
            setRolloverEnabled(true);
        }

        void setActive(boolean active) {
            this.isActiveState = active;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();

            // 1. 绘制投影阴影
            g2.setColor(new Color(0, 0, 0, 42));
            g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);

            // 2. 根据选中与否及触发状态选择 3D 渐变颜色
            Color top;
            Color bottom;
            Color border;

            if (isActiveState) {
                // 亮眼森林绿 (激活状态，例如智能提示开启)
                top = pressed ? new Color(0x45A760) : rollover ? new Color(0x9BEAB0) : new Color(0x82D095);
                bottom = pressed ? new Color(0x2B7D42) : new Color(0x45A760);
                border = new Color(0xF2C45A); // 金色发光边框
            } else {
                // 复古木质黄褐色 (默认状态)
                top = pressed ? new Color(0xB88048) : rollover ? new Color(0xF0C987) : new Color(0xE5C290);
                bottom = pressed ? new Color(0x956424) : new Color(0xB88048);
                border = new Color(0x8A5F29); // 深木色边框
            }

            // 3. 填充主渐变色
            GradientPaint bgGrad = new GradientPaint(0, 0, top, 0, h - 2, bottom);
            g2.setPaint(bgGrad);
            g2.fillRoundRect(1, 1, w - 3, h - 3, 10, 10);

            // 4. 绘制边框
            g2.setColor(border);
            if (isActiveState) {
                g2.setStroke(new BasicStroke(2.0f));
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

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedPanel extends JPanel {
        private Color bgColor;
        private int roundRadius;

        RoundedPanel(Color bgColor, int roundRadius) {
            this.bgColor = bgColor;
            this.roundRadius = roundRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), roundRadius, roundRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    int[][] arr2;
    int[][] win;
    int gridSize = 4;
    boolean isChallengeMode = false;
    String username = "admin";
    Timer gameTimer;
    int timeElapsed = 0;
    boolean timerStarted = false;
    boolean scoreSaved = false;
    boolean casualResultShown = false;

    // 现代化横向大按钮
    ModernButton backToModeBtn = new ModernButton("返回模式");
    ModernButton leaderboardBtn = new ModernButton("排行榜");
    ModernButton replayBtn = new ModernButton("重新游戏");
    ModernButton changeImageBtn = new ModernButton("更换图片");
    ModernButton hintBtn;
    ModernButton reLoginBtn = new ModernButton("重新登录");
    ModernButton exitBtn = new ModernButton("退出游戏");

    // 常驻界面组件
    RoundedPanel controlPanel;
    JLabel stepCountLabel;
    JLabel timerLabel;
    JPanel puzzlePanel;
    JLabel fullImageLabel;
    JLabel bgLabel;

    // 更换图片下拉菜单（用弹出菜单 JPopupMenu 代替传统菜单栏）
    JPopupMenu changeImagePopupMenu = new JPopupMenu();
    JMenuItem catItem = new JMenuItem("小猫");
    JMenuItem dogItem = new JMenuItem("小狗");
    JMenuItem emojiItem = new JMenuItem("表情包");
    JMenuItem customUploadItem = new JMenuItem("自定义上传");
    JMenu myCustomImagesMenu = new JMenu("已上传图片");

    // 智能提示开关状态
    boolean isHintEnabled = false;

    // 游戏历史路径，用来回退和提供提示
    List<BoardState> history = new ArrayList<>();
    // 是否是首次加载（控制引导弹窗）
    boolean isFirstLaunch = true;

    // 提示呼吸灯状态、共享透明度与定时器
    Timer breatheTimer;
    static double breatheAlpha = 0.5;
    static boolean breatheUp = true;

    // 胜利横幅滑落动画变量
    Timer winAnimationTimer;
    double animTime = 0.0;
    JLabel winLabel;
    boolean winAnimationPlayed = false;

    // 定义变量
    int step = 0;
    // 空白位置
    int x = 0;
    int y = 0;

    String path = "image/cat/cat1/";
    ImageIcon[] slicedImages;

    public GameFrame(String username, int gridSize, boolean isChallengeMode) {
        this.username = username;
        this.gridSize = gridSize;
        this.isChallengeMode = isChallengeMode;

        // 初始化胜利和玩家网格
        this.win = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                this.win[i][j] = i * gridSize + j + 1;
            }
        }
        this.win[gridSize - 1][gridSize - 1] = 0;

        // 初始化界面
        initJFrame();

        // 初始化控制栏按钮与弹窗菜单
        initControlBar();
        // 初始化数据
        initData();
        // 动态裁剪生成切片图片
        sliceImage();
        // 初始化图片
        initImage();

        // 初始化智能提示呼吸灯定时器 (使 Alpha 在 0.2 至 0.8 之间循环波动，更新 TileLabel 渲染)
        breatheTimer = new Timer(30, e -> {
            if (isHintEnabled && !victory()) {
                if (breatheUp) {
                    breatheAlpha += 0.035;
                    if (breatheAlpha >= 0.8) {
                        breatheAlpha = 0.8;
                        breatheUp = false;
                    }
                } else {
                    breatheAlpha -= 0.035;
                    if (breatheAlpha <= 0.2) {
                        breatheAlpha = 0.2;
                        breatheUp = true;
                    }
                }
                TileLabel.alpha = breatheAlpha;
                this.getContentPane().repaint();
            }
        });
        breatheTimer.start();

        this.setVisible(true);

        if (isFirstLaunch) {
            SwingUtilities.invokeLater(() -> {
                showTutorialDialog();
                isFirstLaunch = false;
            });
        } else {
            this.requestFocusInWindow();
        }
    }

    public GameFrame() {
        this("admin", 4, false);
    }

    private void showTutorialDialog() {
        // 自定义滚动条UI
        class ModernScrollBarUI extends BasicScrollBarUI {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new java.awt.Dimension(0, 0));
                return button;
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new java.awt.Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintTrack(java.awt.Graphics g, JComponent c, java.awt.Rectangle trackBounds) {
                g.setColor(new Color(0xFDFBF7));
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g.setColor(new Color(0xEADCC9));
                int x = trackBounds.x + trackBounds.width / 2 - 1;
                g.fillRect(x, trackBounds.y, 2, trackBounds.height);
            }

            @Override
            protected void paintThumb(java.awt.Graphics g, JComponent c, java.awt.Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xB48A53)); // 复古深金色/棕色
                int width = 6; // 窄一点，精致小巧
                int x = thumbBounds.x + (thumbBounds.width - width) / 2;
                g2.fillRoundRect(x, thumbBounds.y + 2, width, thumbBounds.height - 4, width, width);
                g2.dispose();
            }
        }

        JDialog dialog = new JDialog(this, "游戏玩法与快捷键指南", true);
        dialog.setUndecorated(true); // 去除系统边框
        dialog.setResizable(false);

        // 主面板带有复古圆角边框
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(0xFDFBF7));
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0x7B3A00), 2, true));

        // 自定义顶部标题栏
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0xF3EDE2)); // 略深色调的米色底
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18)); // 适度增加上下间距让标题更美观

        // 顶部左上角显示“拼图游戏新手指南” - 字体加大为 20 号，加粗
        JLabel titleLabel = new JLabel("拼图游戏新手指南");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x7B3A00));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 自定义关闭按钮
        JLabel closeBtn = new JLabel("×");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 24)); // 加大显示关闭按钮
        closeBtn.setForeground(new Color(0x7B3A00));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
                GameFrame.this.requestFocusInWindow();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(new Color(0x7B3A00));
            }
        });
        headerPanel.add(closeBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 支持拖拽
        final java.awt.Point dragOffset = new java.awt.Point();
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset.setLocation(e.getPoint());
            }
        });
        headerPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dialog.setLocation(e.getXOnScreen() - dragOffset.x, e.getYOnScreen() - dragOffset.y);
            }
        });

        // 内容中间面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(0xFDFBF7));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 20));

        // 文本区域显示指南内容，支持HTML样式
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(new Color(0xFDFBF7));
        textPane.setOpaque(true);

        // 整体字号调整为 14px，行高 1.6，小标题 16px，设计更为紧凑和谐
        String htmlText = "<html>"
                + "<body style='font-family: \"微软雅黑\"; color: #5A3000; background-color: #FDFBF7; margin: 0; padding: 0;'>"
                + "<div style='font-size: 13px; line-height: 1.6;'>"
                + "  <div style='margin-bottom: 12px;'>"
                + "    <span style='font-size: 14px; font-weight: bold; color: #7B3A00;'>【 基本操作 】</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>方向键</b>：使用键盘的 <b>↑ ↓ ← →</b> 键，移动空白格相邻的拼图块。</span>"
                + "  </div>"
                + "  <div style='margin-bottom: 12px;'>"
                + "    <span style='font-size: 14px; font-weight: bold; color: #7B3A00;'>【 快捷键指令 】</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>A 键</b>：长按查看完整原图，松开恢复游戏。</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>W 键</b>：一键自动完成拼图，直接触发通关（仅限休闲模式）。</span>"
                + "  </div>"
                + "  <div style='margin-bottom: 12px;'>"
                + "    <span style='font-size: 14px; font-weight: bold; color: #7B3A00;'>【 核心功能 】</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>智能提示</b>：高亮指明最优下一步。</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>图片管理</b>：在已上传图片中，<b>左键点击</b>直接游戏，<b>右键点击</b>可重命名或删除图片。</span>"
                + "  </div>"
                + "  <div style='margin-bottom: 8px;'>"
                + "    <span style='font-size: 14px; font-weight: bold; color: #7B3A00;'>【 排行榜与挑战规则 】</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>记录条件</b>：只有在<b>挑战模式</b>下通关的成绩才会被记录到排行榜。</span><br>"
                + "    <span style='padding-left: 8px; color: #6D3D00;'><b>规则限制</b>：挑战模式下将<b>禁用智能提示</b>，且<b>不能直接按 W 键</b>通关。为方便测试，可按 <b>Shift + W</b> 一键通关。</span>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";
        textPane.setText(htmlText);

        // 用 JScrollPane 包裹 textPane 增加滚轮支持（但不显示滚动条）
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(0xFDFBF7));
        scrollPane.getViewport().setBackground(new Color(0xFDFBF7));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        ModernButton btn = new ModernButton("我知道了");
        btn.addActionListener(e -> {
            dialog.dispose();
            this.requestFocusInWindow();
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 15, 0));
        btnPanel.add(btn);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        dialog.setContentPane(mainPanel);
        dialog.setSize(480, 480); // 调整窗口大小以获得完美的和谐视觉比率
        dialog.setLocationRelativeTo(this);
        dialog.setAlwaysOnTop(true);

        // 保证最一开始打开游戏时滚轮在最上方
        SwingUtilities.invokeLater(() -> {
            textPane.setCaretPosition(0);
            scrollPane.getVerticalScrollBar().setValue(0);
        });

        dialog.setVisible(true);
    }

    // 统一风格的自定义对话框基础类
    private static class CustomDialog extends JDialog {
        protected JPanel mainPanel;
        protected JPanel headerPanel;
        protected JPanel centerPanel;
        protected JPanel btnPanel;
        protected JLabel titleLabel;
        protected JLabel closeBtn;
        protected java.awt.Point dragOffset = new java.awt.Point();

        public CustomDialog(java.awt.Frame parent, String title, int width, int height) {
            super(parent, title, true);
            setUndecorated(true);
            setResizable(false);

            mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(0xFDFBF7));
            mainPanel.setBorder(BorderFactory.createLineBorder(new Color(0x7B3A00), 2, true));

            headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(0xF3EDE2));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
            titleLabel.setForeground(new Color(0x7B3A00));
            headerPanel.add(titleLabel, BorderLayout.WEST);

            closeBtn = new JLabel("×");
            closeBtn.setFont(new Font("Arial", Font.BOLD, 22));
            closeBtn.setForeground(new Color(0x7B3A00));
            closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    dispose();
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeBtn.setForeground(Color.RED);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    closeBtn.setForeground(new Color(0x7B3A00));
                }
            });
            headerPanel.add(closeBtn, BorderLayout.EAST);
            mainPanel.add(headerPanel, BorderLayout.NORTH);

            // 支持拖拽
            headerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragOffset.setLocation(e.getPoint());
                }
            });
            headerPanel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    setLocation(e.getXOnScreen() - dragOffset.x, e.getYOnScreen() - dragOffset.y);
                }
            });

            centerPanel = new JPanel();
            centerPanel.setBackground(new Color(0xFDFBF7));
            centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            mainPanel.add(btnPanel, BorderLayout.SOUTH);

            setContentPane(mainPanel);
            setSize(width, height);
            setLocationRelativeTo(parent);
            setAlwaysOnTop(true);
        }
    }

    private int showImageOptionDialog(String name) {
        final int[] result = {-1}; // -1 for cancel/close, 0 for rename, 1 for delete
        CustomDialog dialog = new CustomDialog(this, "图片操作", 380, 180);
        
        dialog.centerPanel.setLayout(new BorderLayout());
        JLabel msgLabel = new JLabel("<html><div style='text-align: center; font-family: \"微软雅黑\"; font-size: 14px; color: #5A3000;'>"
                + "请选择对自定义图片【<b>" + name + "</b>】的操作："
                + "</div></html>");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.centerPanel.add(msgLabel, BorderLayout.CENTER);

        ModernButton renameBtn = new ModernButton("重命名");
        ModernButton deleteBtn = new ModernButton("删除图片");

        renameBtn.addActionListener(e -> {
            result[0] = 0;
            dialog.dispose();
        });

        deleteBtn.addActionListener(e -> {
            result[0] = 1;
            dialog.dispose();
        });

        dialog.btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 0));
        dialog.btnPanel.add(renameBtn);
        dialog.btnPanel.add(deleteBtn);

        dialog.setVisible(true);
        return result[0];
    }

    private String showCustomInputDialog(String title, String message, String initialValue) {
        final String[] result = {null};
        CustomDialog dialog = new CustomDialog(this, title, 380, 200);

        dialog.centerPanel.setLayout(new BorderLayout(0, 10));
        JLabel msgLabel = new JLabel("<html><div style='font-family: \"微软雅黑\"; font-size: 14px; color: #5A3000;'>"
                + message + "</div></html>");
        dialog.centerPanel.add(msgLabel, BorderLayout.NORTH);

        JTextField textField = new JTextField(initialValue);
        textField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textField.setForeground(new Color(0x5A3000));
        textField.setBackground(Color.WHITE);
        textField.setCaretColor(new Color(0x7B3A00));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x7B3A00), 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        dialog.centerPanel.add(textField, BorderLayout.CENTER);

        ModernButton okBtn = new ModernButton("确定");
        ModernButton cancelBtn = new ModernButton("取消");

        okBtn.addActionListener(e -> {
            result[0] = textField.getText();
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> {
            dialog.dispose();
        });

        dialog.btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 0));
        dialog.btnPanel.add(okBtn);
        dialog.btnPanel.add(cancelBtn);

        dialog.setVisible(true);
        return result[0];
    }

    private boolean showCustomConfirmDialog(String title, String message) {
        return showGameConfirmDialog(title, message, "确定", "取消");
    }

    private boolean showGameConfirmDialog(String title, String message, String confirmText, String cancelText) {
        final boolean[] result = {false};
        CustomDialog dialog = new CustomDialog(this, title, 380, 180);

        dialog.centerPanel.setLayout(new BorderLayout());
        JLabel msgLabel = new JLabel("<html><div style='text-align: center; font-family: \"微软雅黑\"; font-size: 14px; color: #5A3000;'>"
                + message.replace("\n", "<br>") + "</div></html>");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.centerPanel.add(msgLabel, BorderLayout.CENTER);

        ModernButton okBtn = new ModernButton(confirmText);
        ModernButton cancelBtn = new ModernButton(cancelText);

        okBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> {
            dialog.dispose();
        });

        dialog.btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 0));
        dialog.btnPanel.add(okBtn);
        dialog.btnPanel.add(cancelBtn);

        dialog.setVisible(true);
        return result[0];
    }

    private void showCustomMessageDialog(String title, String message, String type) {
        CustomDialog dialog = new CustomDialog(this, title, 380, 170);

        dialog.centerPanel.setLayout(new BorderLayout());
        
        String color = "#5A3000"; // default
        if ("error".equalsIgnoreCase(type)) {
            color = "#C0392B"; // red for error
        } else if ("success".equalsIgnoreCase(type)) {
            color = "#27AE60"; // green for success
        }

        JLabel msgLabel = new JLabel("<html><div style='text-align: center; font-family: \"微软雅黑\"; font-size: 14px; color: " + color + ";'>"
                + message + "</div></html>");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.centerPanel.add(msgLabel, BorderLayout.CENTER);

        ModernButton okBtn = new ModernButton("我知道了");
        okBtn.addActionListener(e -> dialog.dispose());

        dialog.btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        dialog.btnPanel.add(okBtn);

        dialog.setVisible(true);
    }

    private void updateCustomImagesMenu() {
        myCustomImagesMenu.removeAll();
        File customDir = new File("image/custom");
        if (customDir.exists() && customDir.isDirectory()) {
            File[] files = customDir.listFiles(File::isDirectory);
            if (files != null) {
                for (File dir : files) {
                    String name = dir.getName();
                    JMenuItem item = new JMenuItem(name);
                    item.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent e) {
                            if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                                // 左键点击：直接开始游戏
                                path = "image/custom/" + name + "/";
                                step = 0;
                                resetTimer();
                                winAnimationPlayed = false;
                                scoreSaved = false;
                                sliceImage();
                                initData();
                                initImage();

                                // 关闭菜单栏的弹出状态
                                changeImagePopupMenu.setVisible(false);
                                GameFrame.this.requestFocusInWindow();
                            } else if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                                // 右键点击：显示重命名、删除图片操作
                                changeImagePopupMenu.setVisible(false);

                                int choice = showImageOptionDialog(name);

                                if (choice == 0) {
                                    // 重命名
                                    String newName = showCustomInputDialog("重命名图片", "请输入新的图片名称:", name);
                                    if (newName != null && !newName.trim().isEmpty()) {
                                        newName = newName.trim();
                                        File src = new File("image/custom/" + name);
                                        File dest = new File("image/custom/" + newName);
                                        if (dest.exists()) {
                                            showCustomMessageDialog("重命名失败", "该名称已存在，请换个名称！", "error");
                                        } else {
                                            if (src.renameTo(dest)) {
                                                // 如果重命名的正是当前游戏内使用的图片，更新路径
                                                if (path.equals("image/custom/" + name + "/")) {
                                                    path = "image/custom/" + newName + "/";
                                                }
                                                updateCustomImagesMenu();
                                                showCustomMessageDialog("成功", "图片重命名成功！", "success");
                                            } else {
                                                showCustomMessageDialog("错误", "重命名操作失败，请重试！", "error");
                                            }
                                        }
                                    }
                                } else if (choice == 1) {
                                    // 删除图片
                                    boolean confirm = showCustomConfirmDialog("确认删除", "确认要删除自定义图片【" + name + "】吗？\n该操作不可撤销！");
                                    if (confirm) {
                                        File src = new File("image/custom/" + name);
                                        if (deleteDirectory(src)) {
                                            // 如果删除的正是当前游戏内使用的图片，切回默认图片
                                            if (path.equals("image/custom/" + name + "/")) {
                                                path = "image/cat/cat1/";
                                                step = 0;
                                                resetTimer();
                                                winAnimationPlayed = false;
                                                scoreSaved = false;
                                                sliceImage();
                                                initData();
                                                initImage();
                                            }
                                            updateCustomImagesMenu();
                                            showCustomMessageDialog("成功", "图片已成功删除！", "success");
                                        } else {
                                            showCustomMessageDialog("错误", "删除失败，可能某些文件被占用！", "error");
                                        }
                                    }
                                }

                                GameFrame.this.requestFocusInWindow();
                            }
                        }
                    });
                    myCustomImagesMenu.add(item);
                }
            }
        }
        // 用户自定义图片占位提示
        if (myCustomImagesMenu.getItemCount() == 0) {
            JMenuItem placeholder = new JMenuItem("暂无自定义图片");
            placeholder.setEnabled(false);
            myCustomImagesMenu.add(placeholder);
        }
    }

    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        return dir.delete();
    }

    // ── 内存切片与计时器等核心辅助逻辑 ────────────────────────

    /**
     * 动态读取 path 下的 all.jpg，并根据当前 gridSize 进行切图存储于内存中
     */
    private void sliceImage() {
        slicedImages = new ImageIcon[gridSize * gridSize];
        BufferedImage fullImage = null;
        try {
            java.net.URL url = this.getClass().getResource("/" + path + "all.jpg");
            if (url != null) {
                fullImage = ImageIO.read(url);
            } else {
                fullImage = ImageIO.read(new File(path + "all.jpg"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 若原图加载失败，初始化为白底空图
        if (fullImage == null) {
            for (int i = 0; i < gridSize * gridSize; i++) {
                slicedImages[i] = new ImageIcon();
            }
            return;
        }

        fullImage = GameStyle.normalizePuzzleImage(fullImage, 540);

        // 动态分割
        int tilePixels = 540 / gridSize;
        int idx = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                BufferedImage sub = fullImage.getSubimage(j * tilePixels, i * tilePixels, tilePixels, tilePixels);
                // 缩放到最终渲染大小并转为 ImageIcon 存放
                Image scaled = sub.getScaledInstance(tilePixels, tilePixels, Image.SCALE_SMOOTH);
                slicedImages[idx] = new ImageIcon(scaled);
                idx++;
                if (idx >= gridSize * gridSize) {
                    break;
                }
            }
            if (idx >= gridSize * gridSize) {
                break;
            }
        }
        // 空白位置 0 索引关联的图片为空白 Icon
        slicedImages[0] = new ImageIcon();
    }

    private void startTimerIfNeeded() {
        if (!timerStarted && !victory()) {
            timerStarted = true;
            timeElapsed = 0;
            if (gameTimer != null) {
                gameTimer.stop();
            }
            gameTimer = new Timer(1000, e -> {
                if (victory()) {
                    gameTimer.stop();
                    return;
                }
                timeElapsed++;
                updateTimerLabel();
            });
            gameTimer.start();
        }
    }

    private void updateTimerLabel() {
        if (timerLabel != null) {
            int min = timeElapsed / 60;
            int sec = timeElapsed % 60;
            String modeStr = isChallengeMode ? "挑战时间: " : "游戏时间: ";
            timerLabel.setText(String.format("%s%02d:%02d", modeStr, min, sec));
        }
    }

    private void resetTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        timeElapsed = 0;
        timerStarted = false;
        updateTimerLabel();
    }

    // ── 排行榜数据存储逻辑 ─────────────────────────────────────

    static class LeaderboardRecord implements Comparable<LeaderboardRecord> {
        String username;
        int gridSize;
        int timeInSeconds;
        int steps;
        String date;

        LeaderboardRecord(String username, int gridSize, int timeInSeconds, int steps, String date) {
            this.username = username;
            this.gridSize = gridSize;
            this.timeInSeconds = timeInSeconds;
            this.steps = steps;
            this.date = date;
        }

        @Override
        public int compareTo(LeaderboardRecord o) {
            return Integer.compare(this.timeInSeconds, o.timeInSeconds);
        }
    }

    private List<LeaderboardRecord> readLeaderboard() {
        List<LeaderboardRecord> list = new ArrayList<>();
        File file = new File("leaderboard.txt");
        if (!file.exists()) {
            return list;
        }
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(
                new java.io.FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        String user = parts[0].trim();
                        int size = Integer.parseInt(parts[1].trim());
                        int seconds = Integer.parseInt(parts[2].trim());
                        int steps = Integer.parseInt(parts[3].trim());
                        String date = parts[4].trim();
                        list.add(new LeaderboardRecord(user, size, seconds, steps, date));
                    } catch (NumberFormatException e) {
                        // 跳过无效行
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveScore(String user, int size, int seconds, int steps) {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
        File file = new File("leaderboard.txt");
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file, true), java.nio.charset.StandardCharsets.UTF_8))) {
            bw.write(String.format("%s,%d,%d,%d,%s\n", user, size, seconds, steps, date));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLeaderboardDialog() {
        CustomDialog dialog = new CustomDialog(this, "排行榜", 450, 480);
        dialog.centerPanel.setLayout(new BorderLayout());

        List<LeaderboardRecord> allRecords = readLeaderboard();

        JPanel filterPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));
        filterPanel.setOpaque(false);
        ModernButton btn3 = new ModernButton("3 x 3");
        ModernButton btn4 = new ModernButton("4 x 4");
        ModernButton btn5 = new ModernButton("5 x 5");
        filterPanel.add(btn3);
        filterPanel.add(btn4);
        filterPanel.add(btn5);
        dialog.centerPanel.add(filterPanel, BorderLayout.NORTH);

        JTextPane listPane = new JTextPane();
        listPane.setContentType("text/html");
        listPane.setEditable(false);
        listPane.setBackground(new Color(0xFDFBF7));
        listPane.setOpaque(true);

        JScrollPane scroll = new JScrollPane(listPane);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEADCC9), 1, true));
        scroll.setBackground(new Color(0xFDFBF7));
        scroll.getViewport().setBackground(new Color(0xFDFBF7));
        dialog.centerPanel.add(scroll, BorderLayout.CENTER);

        class LeaderboardRefresher {
            void refresh(int size) {
                btn3.setActive(size == 3);
                btn4.setActive(size == 4);
                btn5.setActive(size == 5);

                List<LeaderboardRecord> filtered = new ArrayList<>();
                for (LeaderboardRecord r : allRecords) {
                    if (r.gridSize == size) {
                        filtered.add(r);
                    }
                }
                java.util.Collections.sort(filtered);

                StringBuilder sb = new StringBuilder();
                sb.append("<html><body style='font-family:\"微软雅黑\"; color:#5A3000; margin:5px;'>");
                sb.append("<table width='100%' border='0' cellspacing='0' cellpadding='6'>");
                sb.append("<tr style='background-color:#F3EDE2; font-weight:bold;'>");
                sb.append("<td align='center' width='15%'>排名</td>");
                sb.append("<td width='25%'>玩家</td>");
                sb.append("<td align='center' width='20%'>时间</td>");
                sb.append("<td align='center' width='15%'>步数</td>");
                sb.append("<td align='center' width='25%'>日期</td>");
                sb.append("</tr>");

                int count = Math.min(filtered.size(), 10);
                if (count == 0) {
                    sb.append("<tr><td colspan='5' align='center' style='padding:20px; color:#A07040;'>暂无该难度的挑战记录</td></tr>");
                } else {
                    for (int i = 0; i < count; i++) {
                        LeaderboardRecord r = filtered.get(i);
                        String rowBg = (i % 2 == 0) ? "#FFFFFF" : "#FDFBF7";
                        sb.append(String.format("<tr style='background-color:%s;'>", rowBg));
                        
                        String rankStr = String.valueOf(i + 1);
                        if (i == 0) rankStr = "<span style='color:#E74C3C; font-weight:bold;'>🥇 1</span>";
                        else if (i == 1) rankStr = "<span style='color:#F39C12; font-weight:bold;'>🥈 2</span>";
                        else if (i == 2) rankStr = "<span style='color:#3498DB; font-weight:bold;'>🥉 3</span>";

                        int min = r.timeInSeconds / 60;
                        int sec = r.timeInSeconds % 60;
                        String timeStr = String.format("%02d:%02d", min, sec);

                        sb.append(String.format("<td align='center'>%s</td>", rankStr));
                        sb.append(String.format("<td><b>%s</b></td>", r.username));
                        sb.append(String.format("<td align='center' style='color:#D35400;'>%s</td>", timeStr));
                        sb.append(String.format("<td align='center'>%d</td>", r.steps));
                        sb.append(String.format("<td align='center' style='font-size:10px; color:#7F8C8D;'>%s</td>", r.date.split(" ")[0]));
                        sb.append("</tr>");
                    }
                }
                sb.append("</table></body></html>");
                listPane.setText(sb.toString());
                listPane.setCaretPosition(0);
            }
        }

        LeaderboardRefresher refresher = new LeaderboardRefresher();

        btn3.addActionListener(e -> refresher.refresh(3));
        btn4.addActionListener(e -> refresher.refresh(4));
        btn5.addActionListener(e -> refresher.refresh(5));

        ModernButton closeButton = new ModernButton("关闭");
        closeButton.addActionListener(e -> dialog.dispose());
        dialog.btnPanel.add(closeButton);

        refresher.refresh(gridSize);
        dialog.setVisible(true);
    }

    private void showChallengeResultDialog(int seconds, int steps) {
        CustomDialog dialog = new CustomDialog(this, "挑战成功", 420, 220);
        dialog.centerPanel.setLayout(new BorderLayout());

        int min = seconds / 60;
        int sec = seconds % 60;
        JLabel resultLabel = new JLabel("<html><div style='text-align:center; font-family:\"微软雅黑\"; color:#5A3000;'>"
                + "<div style='font-size:17px; font-weight:bold; color:#27AE60; margin-bottom:10px;'>成绩已记录到排行榜</div>"
                + "<div style='font-size:14px; line-height:1.8;'>通关用时：<b>" + String.format("%02d:%02d", min, sec) + "</b><br>"
                + "总步数：<b>" + steps + "</b></div>"
                + "</div></html>");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.centerPanel.add(resultLabel, BorderLayout.CENTER);

        ModernButton leaderboardButton = new ModernButton("查看排行榜");
        leaderboardButton.addActionListener(e -> {
            dialog.dispose();
            showLeaderboardDialog();
        });

        ModernButton replayButton = new ModernButton("重新游戏");
        replayButton.addActionListener(e -> {
            dialog.dispose();
            restartCurrentGame();
        });

        dialog.btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 18, 0));
        dialog.btnPanel.add(leaderboardButton);
        dialog.btnPanel.add(replayButton);
        dialog.setVisible(true);
    }

    private void showCasualResultDialog(int seconds, int steps) {
        CustomDialog dialog = new CustomDialog(this, "拼图完成", 430, 230);
        dialog.centerPanel.setLayout(new BorderLayout());

        int min = seconds / 60;
        int sec = seconds % 60;
        JLabel resultLabel = new JLabel("<html><div style='text-align:center; font-family:\"微软雅黑\"; color:#5A3000;'>"
                + "<div style='font-size:17px; font-weight:bold; color:#27AE60; margin-bottom:10px;'>恭喜完成拼图！</div>"
                + "<div style='font-size:14px; line-height:1.8;'>用时：<b>" + String.format("%02d:%02d", min, sec) + "</b><br>"
                + "总步数：<b>" + steps + "</b></div>"
                + "</div></html>");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.centerPanel.add(resultLabel, BorderLayout.CENTER);

        ModernButton modeButton = new ModernButton("返回模式");
        modeButton.addActionListener(e -> {
            dialog.dispose();
            returnToModeSelection();
        });

        ModernButton replayButton = new ModernButton("重新游戏");
        replayButton.addActionListener(e -> {
            dialog.dispose();
            restartCurrentGame();
        });

        dialog.btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 22, 0));
        dialog.btnPanel.add(modeButton);
        dialog.btnPanel.add(replayButton);
        dialog.setVisible(true);
    }

    private void stopGameTimers() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (breatheTimer != null) {
            breatheTimer.stop();
        }
        if (winAnimationTimer != null) {
            winAnimationTimer.stop();
        }
    }

    private void returnToModeSelection() {
        stopGameTimers();
        this.setVisible(false);
        this.dispose();
        new SetupFrame(username);
    }

    private void restartCurrentGame() {
        step = 0;
        winAnimationPlayed = false;
        scoreSaved = false;
        casualResultShown = false;
        resetTimer();
        initData();
        initImage();
        this.requestFocusInWindow();
    }

    private void initData() {
        // 1. 初始化为胜利（正确）盘面
        arr2 = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            System.arraycopy(win[i], 0, arr2[i], 0, gridSize);
        }
        x = gridSize - 1;
        y = gridSize - 1;

        // 2. 清空历史路径，并将初始获胜状态作为回退的终点
        history.clear();
        history.add(new BoardState(arr2, x, y, gridSize));

        // 3. 通过做随机的合法移动打乱拼图，保证拼图100%可解
        int lastMove = -1; // 用来避免直接退回上一步
        for (int stepIndex = 0; stepIndex < 60; stepIndex++) {
            List<Integer> directions = new ArrayList<>();
            if (x > 0 && lastMove != 1)
                directions.add(0);
            if (x < gridSize - 1 && lastMove != 0)
                directions.add(1);
            if (y > 0 && lastMove != 3)
                directions.add(2);
            if (y < gridSize - 1 && lastMove != 2)
                directions.add(3);

            if (directions.isEmpty()) {
                if (x > 0)
                    directions.add(0);
                if (x < gridSize - 1)
                    directions.add(1);
                if (y > 0)
                    directions.add(2);
                if (y < gridSize - 1)
                    directions.add(3);
            }

            int dir = directions.get((int) (Math.random() * directions.size()));
            lastMove = dir;

            if (dir == 0) {
                arr2[x][y] = arr2[x - 1][y];
                arr2[x - 1][y] = 0;
                x--;
            } else if (dir == 1) {
                arr2[x][y] = arr2[x + 1][y];
                arr2[x + 1][y] = 0;
                x++;
            } else if (dir == 2) {
                arr2[x][y] = arr2[x][y - 1];
                arr2[x][y - 1] = 0;
                y--;
            } else if (dir == 3) {
                arr2[x][y] = arr2[x][y + 1];
                arr2[x][y + 1] = 0;
                y++;
            }

            BoardState currentState = new BoardState(arr2, x, y, gridSize);
            // 避免打乱路径中出现环路，优化提示路径长度
            int idx = history.indexOf(currentState);
            if (idx != -1) {
                history.subList(idx + 1, history.size()).clear();
            } else {
                history.add(currentState);
            }
        }
    }

    private void initImage() {
        // 1. 更新步数
        if (stepCountLabel != null) {
            stepCountLabel.setText("游戏步数: " + step);
        }

        // 2. 清空拼图面板中已有的图片块
        if (puzzlePanel != null) {
            puzzlePanel.removeAll();
        }

        // 3. 判断并播放胜利动画
        if (victory()) {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            if (!winAnimationPlayed) {
                winAnimationPlayed = true;
                if (winLabel != null) {
                    winLabel.setIcon(getIcon("image/win.png", 256, 225));
                    winLabel.setBounds(242, -250, 256, 225); // 初始在屏幕上方边界外

                    if (winAnimationTimer != null && winAnimationTimer.isRunning()) {
                        winAnimationTimer.stop();
                    }

                    // 启动 16ms 定时器驱动 60FPS 阻尼弹性滑落动画
                    animTime = 0.0;
                    winAnimationTimer = new Timer(16, e -> {
                        animTime += 0.02; // 大约 800ms
                        if (animTime >= 1.0) {
                            animTime = 1.0;
                            winAnimationTimer.stop();
                        }
                        double progress = easeOutElastic(animTime);
                        int startY = -250;
                        int targetY = 317;
                        int currentY = (int) (startY + (targetY - startY) * progress);
                        winLabel.setBounds(242, currentY, 256, 225);
                        this.getContentPane().repaint();
                    });
                    winAnimationTimer.start();
                }

                // 挑战模式结算并持久化成绩
                if (isChallengeMode && !scoreSaved) {
                    scoreSaved = true;
                    saveScore(username, gridSize, timeElapsed, step);
                    SwingUtilities.invokeLater(() -> showChallengeResultDialog(timeElapsed, step));
                } else if (!isChallengeMode && !casualResultShown) {
                    casualResultShown = true;
                    SwingUtilities.invokeLater(() -> showCasualResultDialog(timeElapsed, step));
                }
            }
        } else {
            // 未胜利时移走胜利横幅，并重置播放状态
            winAnimationPlayed = false;
            casualResultShown = false;
            if (winLabel != null) {
                winLabel.setBounds(242, -250, 256, 225);
            }
        }

        // 4. 确定需要高亮最优下一步的坐标
        int highlightX = -1;
        int highlightY = -1;
        if (isHintEnabled && history.size() > 1 && !victory()) {
            BoardState targetState = history.get(history.size() - 2);
            highlightX = targetState.x;
            highlightY = targetState.y;
        }

        // 5. 渲染 gridSize x gridSize 拼图块到 puzzlePanel
        if (puzzlePanel != null) {
            int tileSize = 540 / gridSize;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    int num = arr2[i][j];
                    TileLabel jl = new TileLabel(slicedImages[num]);
                    jl.setBounds(tileSize * j, tileSize * i, tileSize, tileSize);

                    if (i == highlightX && j == highlightY) {
                        jl.setHighlighted(true);
                    } else {
                        jl.setBorder(new BevelBorder(BevelBorder.LOWERED));
                    }
                    puzzlePanel.add(jl);
                }
            }
            // 刷新拼图面板
            puzzlePanel.revalidate();
            puzzlePanel.repaint();
        }
    }

    private double easeOutElastic(double x) {
        double c4 = (2 * Math.PI) / 3;
        return x == 0 ? 0 : x == 1 ? 1 : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
    }

    private void initControlBar() {
        // 给横向控制大按钮绑定点击事件
        backToModeBtn.addActionListener(this);
        leaderboardBtn.addActionListener(this);
        replayBtn.addActionListener(this);
        changeImageBtn.addActionListener(this);
        if (hintBtn != null) {
            hintBtn.addActionListener(this);
        }
        reLoginBtn.addActionListener(this);
        exitBtn.addActionListener(this);

        // 设置下拉弹出菜单样式与字体
        changeImagePopupMenu.setBackground(new Color(0xFDFBF7));
        changeImagePopupMenu.setBorder(BorderFactory.createLineBorder(new Color(0xC49A3C), 2));

        catItem.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dogItem.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        emojiItem.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        customUploadItem.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        myCustomImagesMenu.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        changeImagePopupMenu.add(catItem);
        changeImagePopupMenu.add(dogItem);
        changeImagePopupMenu.add(emojiItem);
        changeImagePopupMenu.addSeparator();
        changeImagePopupMenu.add(customUploadItem);
        changeImagePopupMenu.add(myCustomImagesMenu);

        // 给下拉项绑定事件
        catItem.addActionListener(this);
        dogItem.addActionListener(this);
        emojiItem.addActionListener(this);
        customUploadItem.addActionListener(this);

        // 初始化加载自定义已上传图片列表
        updateCustomImagesMenu();
    }

    private void initJFrame() {
        this.setSize(740, 845);
        // 标题
        this.setTitle("拼图游戏v1.0");
        // 界面置顶
        this.setAlwaysOnTop(true);
        // 设置界面居中
        this.setLocationRelativeTo(null);
        // 设置关闭模式
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // 取消默认的居中放置，只有取消了才会按照XY轴的形式添加组件
        this.setLayout(null);
        // 添加键盘监听
        this.addKeyListener(this);

        // 初始化所有常驻的界面元素，以固定的 z-order 层次添加到内容面板

        // 1. 胜利横幅标签 (最顶层，微调垂直落点)
        winLabel = new JLabel();
        winLabel.setBounds(242, -250, 256, 225);
        this.getContentPane().add(winLabel);
        backToModeBtn.setBounds(52, 52, 104, 34);
        this.getContentPane().add(backToModeBtn);
        leaderboardBtn.setBounds(584, 52, 104, 34);
        this.getContentPane().add(leaderboardBtn);

        // 2. 原图查看层 (向上移动 20 像素)
        fullImageLabel = new JLabel();
        fullImageLabel.setBounds(100, 140, 540, 540);
        fullImageLabel.setVisible(false);
        this.getContentPane().add(fullImageLabel);

        // 3. 底部控制栏面板（放在背景图的最下方外部，使用网格布局）
        controlPanel = new RoundedPanel(new Color(253, 251, 247, 220), 20);
        controlPanel.setLayout(new java.awt.GridLayout(1, isChallengeMode ? 4 : 5, 10, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        controlPanel.setBounds(isChallengeMode ? 105 : 75, 752, isChallengeMode ? 530 : 590, 48);
        controlPanel.add(replayBtn);
        controlPanel.add(changeImageBtn);
        if (!isChallengeMode) {
            hintBtn = new ModernButton("智能提示");
            controlPanel.add(hintBtn);
        }
        controlPanel.add(reLoginBtn);
        controlPanel.add(exitBtn);
        this.getContentPane().add(controlPanel);

        // 4. 步数统计标签
        stepCountLabel = new JLabel("游戏步数: 0");
        stepCountLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        stepCountLabel.setForeground(new Color(0x7B3A00));
        stepCountLabel.setBounds(100, 95, 200, 30);
        this.getContentPane().add(stepCountLabel);

        // 新增：计时标签
        timerLabel = new JLabel(isChallengeMode ? "挑战时间: 00:00" : "游戏时间: 00:00");
        timerLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        timerLabel.setForeground(new Color(0x7B3A00));
        timerLabel.setBounds(350, 95, 200, 30);
        this.getContentPane().add(timerLabel);

        // 5. 拼图区域面板 (向上移动 20 像素)
        puzzlePanel = new JPanel();
        puzzlePanel.setLayout(null);
        puzzlePanel.setOpaque(false);
        puzzlePanel.setBounds(100, 140, 540, 540);
        this.getContentPane().add(puzzlePanel);

        // 6. 背景图片标签 (最底层，向上移动 20 像素)
        ImageIcon icon = getIcon("image/background.png", 660, 728);
        bgLabel = new JLabel(icon);
        bgLabel.setBounds(40, 20, 660, 728);
        this.getContentPane().add(bgLabel);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    // 按下不松
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == 65) {
            // 按住 A 键，直接显示 fullImageLabel 层覆盖拼图区域，无需重构整个 UI，避免闪烁
            if (fullImageLabel != null) {
                fullImageLabel.setIcon(getIcon(path + "all.jpg", 540, 540));
                fullImageLabel.setVisible(true);
            }
            if (puzzlePanel != null) {
                puzzlePanel.setVisible(false);
            }
            this.getContentPane().repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // 1. 判断是否是松开了 A 键 (KeyCode 65)
        if (keyCode == 65) {
            // 松开 A 键，隐藏全图层，重新显示小拼图块
            if (fullImageLabel != null) {
                fullImageLabel.setVisible(false);
            }
            if (puzzlePanel != null) {
                puzzlePanel.setVisible(true);
            }
            this.getContentPane().repaint();
            return;
        }

        // 2. 作弊键：W 键 (KeyCode 87)
        if (keyCode == 87) {
            // 挑战模式下禁用直接按 W，但支持按住 Shift + W 使用
            if (isChallengeMode && !e.isShiftDown()) {
                return;
            }
            arr2 = new int[gridSize][gridSize];
            for (int i = 0; i < gridSize; i++) {
                System.arraycopy(win[i], 0, arr2[i], 0, gridSize);
            }
            x = gridSize - 1;
            y = gridSize - 1;
            initImage();
            return;
        }
        // 判断游戏是否胜利，胜利后此方法直接结束，不能再执行下面的移动代码
        if (victory())
            return;

        // 3. 向左：空白格右侧方块左移 (y < gridSize - 1)
        if (keyCode == 37) {
            if (y < gridSize - 1) {
                startTimerIfNeeded();
                arr2[x][y] = arr2[x][y + 1];
                arr2[x][y + 1] = 0;
                y++;
                step++;
                recordUserMove();
                initImage();
            }
        }

        // 4. 向右：空白格左侧方块右移 (y > 0)
        else if (keyCode == 39) {
            if (y > 0) {
                startTimerIfNeeded();
                arr2[x][y] = arr2[x][y - 1];
                arr2[x][y - 1] = 0;
                y--;
                step++;
                recordUserMove();
                initImage();
            }
        }

        // 5. 向上：空白格下方方块上移 (x < gridSize - 1)
        else if (keyCode == 38) {
            if (x < gridSize - 1) {
                startTimerIfNeeded();
                arr2[x][y] = arr2[x + 1][y];
                arr2[x + 1][y] = 0;
                x++;
                step++;
                recordUserMove();
                initImage();
            }
        }

        // 6. 向下：空白格上方方块下移 (x > 0)
        else if (keyCode == 40) {
            if (x > 0) {
                startTimerIfNeeded();
                arr2[x][y] = arr2[x - 1][y];
                arr2[x - 1][y] = 0;
                x--;
                step++;
                recordUserMove();
                initImage();
            }
        }
    }

    private void recordUserMove() {
        BoardState currentState = new BoardState(arr2, x, y, gridSize);
        int index = history.indexOf(currentState);
        if (index != -1) {
            // 如果走到了历史路径中的某一步，则说明用户在回退，截断后面的多余路径
            history.subList(index + 1, history.size()).clear();
        } else {
            // 新的一步，加入历史记录
            history.add(currentState);
        }
    }

    private ImageIcon getIcon(String path) {
        java.net.URL url = this.getClass().getResource("/" + path);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            return new ImageIcon(path);
        }
    }

    private ImageIcon getIcon(String path, int w, int h) {
        ImageIcon raw = getIcon(path);
        if (raw.getIconWidth() <= 0) {
            return new ImageIcon();
        }
        java.awt.Image scaled = raw.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public boolean victory() {
        // 判断是否成功
        for (int i = 0; i < arr2.length; i++) {
            for (int j = 0; j < arr2.length; j++) {
                if (arr2[i][j] != win[i][j]) {
                    return false;
                }
            }

        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 当前被点击的条目对象
        Object source = e.getSource();
        if (source == backToModeBtn) {
            if (showGameConfirmDialog("返回模式", "确定要返回模式选择吗？\n当前进度不会保留。", "返回模式", "取消")) {
                returnToModeSelection();
            }
        } else if (source == leaderboardBtn) {
            showLeaderboardDialog();
        } else if (source == replayBtn) {
            if (showGameConfirmDialog("重新游戏", "确定要重新开始当前拼图吗？", "重新游戏", "取消")) {
                restartCurrentGame();
            }
        } else if (source == exitBtn) {
            if (showGameConfirmDialog("退出游戏", "确定要退出拼图游戏吗？", "退出游戏", "取消")) {
                stopGameTimers();
                System.exit(0);
            }
        } else if (source == reLoginBtn) {
            if (showGameConfirmDialog("重新登录", "确定要返回登录界面吗？\n当前进度不会保留。", "重新登录", "取消")) {
                stopGameTimers();
                this.setVisible(false);
                this.dispose();
                new LoginFrame();
            }
        } else if (source == changeImageBtn) {
            // 在按钮正下方弹出下拉菜单
            changeImagePopupMenu.show(changeImageBtn, 0, changeImageBtn.getHeight());
        } else if (hintBtn != null && source == hintBtn) {
            isHintEnabled = !isHintEnabled;
            hintBtn.setActive(isHintEnabled);
            if (isHintEnabled) {
                breatheAlpha = 0.5;
                breatheUp = true;
            }
            initImage();
        } else if (source == catItem) {
            // 随机选择图片，确保与当前图片不重复
            String newPath;
            do {
                int number = (int) (Math.random() * 3 + 1);
                newPath = "image/cat/cat" + number + "/";
            } while (newPath.equals(path));
            path = newPath;
            step = 0;
            resetTimer();
            winAnimationPlayed = false;
            scoreSaved = false;
            sliceImage();
            initData();
            initImage();
        } else if (source == dogItem) {
            // 随机选择图片，确保与当前图片不重复
            String newPath;
            do {
                int number = (int) (Math.random() * 3 + 1);
                newPath = "image/dog/dog" + number + "/";
            } while (newPath.equals(path));
            path = newPath;
            step = 0;
            resetTimer();
            winAnimationPlayed = false;
            scoreSaved = false;
            sliceImage();
            initData();
            initImage();
        } else if (source == emojiItem) {
            // 随机选择图片，确保与当前图片不重复
            String newPath;
            do {
                int number = (int) (Math.random() * 3 + 1);
                newPath = "image/emoji/emoji" + number + "/";
            } while (newPath.equals(path));
            path = newPath;
            step = 0;
            resetTimer();
            winAnimationPlayed = false;
            scoreSaved = false;
            sliceImage();
            initData();
            initImage();
        } else if (source == customUploadItem) {
            handleCustomUpload();
        }

        // 动作处理完毕后，确保主窗口获取焦点，使得键盘监听继续生效
        this.requestFocusInWindow();
    }

    private void handleCustomUpload() {
        Locale.setDefault(Locale.CHINA);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择自定义拼图图片");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("图片文件(*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // 弹出提示框让用户输入自定义图片名称
            String customName = showCustomInputDialog("添加图片", "请输入自定义图片名称:", "");
            if (customName == null || customName.trim().isEmpty()) {
                return; // 空值或取消，放弃上传
            }
            customName = customName.trim();

            try {
                // 读取图片
                BufferedImage original = ImageIO.read(selectedFile);
                if (original == null) {
                    showCustomMessageDialog("格式不支持", "图片读取失败，请选择正确的图片格式！", "error");
                    return;
                }

                // 裁剪
                int w = original.getWidth();
                int h = original.getHeight();
                int s = Math.min(w, h);
                int xStart = (w - s) / 2;
                int yStart = (h - s) / 2;
                BufferedImage cropped = original.getSubimage(xStart, yStart, s, s);

                // 缩放为 540x540
                BufferedImage scaled = new BufferedImage(540, 540, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaled.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(cropped, 0, 0, 540, 540, null);
                g.dispose();

                // 写入文件夹
                File customDir = new File("image/custom/" + customName);
                if (!customDir.exists()) {
                    customDir.mkdirs();
                }

                File allFile = new File(customDir, "all.jpg");
                ImageIO.write(scaled, "jpg", allFile);

                // 切割为 4x4
                int index = 1;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        BufferedImage piece = scaled.getSubimage(j * 135, i * 135, 135, 135);
                        File pieceFile = new File(customDir, index + ".jpg");
                        ImageIO.write(piece, "jpg", pieceFile);
                        index++;
                    }
                }

                // 刷新自定义菜单
                updateCustomImagesMenu();

                // 刷新当前游戏图片
                path = "image/custom/" + customName + "/";
                step = 0;
                resetTimer();
                winAnimationPlayed = false;
                scoreSaved = false;
                sliceImage();
                initData();
                initImage();

                showCustomMessageDialog("上传成功", "自定义图片上传成功，拼图游戏已就绪！", "success");

            } catch (IOException e) {
                e.printStackTrace();
                showCustomMessageDialog("错误", "图片上传失败: " + e.getMessage(), "error");
            }
        }
    }
}
