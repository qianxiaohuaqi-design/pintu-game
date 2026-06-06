package ui;

import javax.swing.*;
import java.awt.*;

import static ui.GameStyle.*;

public class SetupFrame extends JFrame {

    private final String username;
    private final Image backgroundImage = GameStyle.loadRaw("image/login/background.png").getImage();

    private int selectedGridSize = 4;
    private boolean isChallengeMode = false;
    private boolean modeSelected = false;

    private JLabel difficultyLabel;
    private ToggleButton btn3x3;
    private ToggleButton btn4x4;
    private ToggleButton btn5x5;
    private ToggleButton btnCasual;
    private ToggleButton btnChallenge;
    private ActionButton startGameBtn;
    private ActionButton backBtn;

    public SetupFrame(String username) {
        this.username = username;
        initFrame();
        initComponents();
        this.setVisible(true);
    }

    private void initFrame() {
        this.setTitle("拼图游戏 · 模式选择");
        this.setSize(488, 420);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.getContentPane().setLayout(null);
    }

    private void initComponents() {
        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                }
                // 遮挡背景图自带的 "拼图游戏" 木牌
                g2.setColor(new Color(0xEED19C));
                g2.fillRect(120, 0, 232, 45);

                paintWoodTitle(g2, "拼图游戏设置", 86, 26, 300, 72, 27);

                int boxHeight = modeSelected ? 230 : 110;
                g2.setColor(new Color(255, 255, 255, 110));
                g2.fillRoundRect(50, 128, 372, boxHeight, 22, 22);
                g2.setColor(new Color(0xC79A55));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(50, 128, 372, boxHeight, 22, 22);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        JLabel modeLabel = createSectionLabel("选择模式");
        modeLabel.setBounds(74, 156, 85, 28);
        root.add(modeLabel);

        btnCasual = new ToggleButton("休闲模式");
        btnCasual.setBounds(167, 152, 112, 40);
        btnCasual.addActionListener(e -> selectMode(false));
        root.add(btnCasual);

        btnChallenge = new ToggleButton("挑战模式");
        btnChallenge.setBounds(292, 152, 112, 40);
        btnChallenge.addActionListener(e -> selectMode(true));
        root.add(btnChallenge);

        difficultyLabel = createSectionLabel("选择难度");
        difficultyLabel.setBounds(74, 224, 85, 28);
        root.add(difficultyLabel);

        btn3x3 = new ToggleButton("3 x 3");
        btn3x3.setBounds(167, 220, 74, 40);
        btn3x3.addActionListener(e -> selectGridSize(3));
        root.add(btn3x3);

        btn4x4 = new ToggleButton("4 x 4");
        btn4x4.setBounds(251, 220, 74, 40);
        btn4x4.addActionListener(e -> selectGridSize(4));
        root.add(btn4x4);

        btn5x5 = new ToggleButton("5 x 5");
        btn5x5.setBounds(335, 220, 74, 40);
        btn5x5.addActionListener(e -> selectGridSize(5));
        root.add(btn5x5);

        startGameBtn = new ActionButton("开始游戏", true);
        startGameBtn.setBounds(102, 302, 132, 46);
        startGameBtn.addActionListener(e -> startGame());
        root.add(startGameBtn);

        backBtn = new ActionButton("返回登录", false);
        backBtn.setBounds(170, 260, 132, 46);
        backBtn.addActionListener(e -> backToLogin());
        root.add(backBtn);

        selectGridSize(4);
        setDifficultyAndStartVisible(false);
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text + "：");
        label.setFont(GameStyle.getFont(Font.BOLD, 15));
        label.setForeground(TEXT_LIGHT_BROWN);
        return label;
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
        btn3x3.setSelectedState(size == 3);
        btn4x4.setSelectedState(size == 4);
        btn5x5.setSelectedState(size == 5);
    }

    private void selectMode(boolean challenge) {
        this.isChallengeMode = challenge;
        this.modeSelected = true;
        btnCasual.setSelectedState(!challenge);
        btnChallenge.setSelectedState(challenge);
        setDifficultyAndStartVisible(true);
        backBtn.setBounds(254, 302, 132, 46);
        this.getContentPane().revalidate();
        this.getContentPane().repaint();
    }

    private void startGame() {
        if (!modeSelected) {
            showMsg(this, "请先选择游戏模式！", "提示");
            return;
        }
        startGameBtn.setEnabled(false);
        try {
            this.setVisible(false);
            this.dispose();
            new GameFrame(username, selectedGridSize, isChallengeMode);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            this.setVisible(true);
            startGameBtn.setEnabled(true);
            showMsg(this,
                    "启动游戏失败：\n" + ex.getClass().getSimpleName() + " - " + ex.getMessage(),
                    "启动失败");
        }
    }

    private void backToLogin() {
        this.setVisible(false);
        this.dispose();
        new LoginFrame();
    }

    private static class ToggleButton extends JButton {
        private boolean selectedState = false;

        ToggleButton(String text) {
            super(text);
            setFont(GameStyle.getFont(Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setSelectedState(boolean selected) {
            this.selectedState = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();

            Color top;
            Color bottom;
            Color border;
            if (selectedState) {
                top = pressed ? new Color(0x32884A) : rollover ? new Color(0x52C271) : new Color(0x47B264);
                bottom = pressed ? new Color(0x1E5E2F) : new Color(0x26773B);
                border = new Color(0xF7CE5B);
            } else {
                top = pressed ? new Color(0xA56E33) : rollover ? new Color(0xDB9B4F) : new Color(0xCE893F);
                bottom = pressed ? new Color(0x72431A) : new Color(0x7D4715);
                border = new Color(0x5E3610);
            }

            g2.setColor(new Color(0, 0, 0, 42));
            g2.fillRoundRect(3, 4, w - 6, h - 6, 12, 12);

            g2.setPaint(new GradientPaint(0, 1, top, 0, h - 2, bottom));
            g2.fillRoundRect(1, 1, w - 3, h - 4, 12, 12);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(selectedState ? 2.2f : 1.5f));
            g2.drawRoundRect(1, 1, w - 3, h - 4, 12, 12);

            if (selectedState) {
                g2.setColor(new Color(255, 255, 255, 210));
                g2.setStroke(new BasicStroke(2.1f));
                g2.drawLine(w - 16, 12, w - 12, 16);
                g2.drawLine(w - 12, 16, w - 7, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ActionButton extends JButton {
        private final boolean primary;

        ActionButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(GameStyle.getFont(Font.BOLD, 16));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();

            Color top;
            Color bottom;
            if (primary) {
                top = pressed ? new Color(0x32884A) : rollover ? new Color(0x52C271) : new Color(0x47B264);
                bottom = pressed ? new Color(0x1E5E2F) : new Color(0x26773B);
            } else {
                top = pressed ? new Color(0xA56E33) : rollover ? new Color(0xDB9B4F) : new Color(0xCE893F);
                bottom = pressed ? new Color(0x72431A) : new Color(0x7D4715);
            }

            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRoundRect(4, 5, w - 8, h - 8, 16, 16);

            g2.setPaint(new GradientPaint(0, 1, top, 0, h - 4, bottom));
            g2.fillRoundRect(1, 1, w - 4, h - 6, 16, 16);

            g2.setColor(primary ? new Color(0xF7CE5B) : new Color(0x5E3610));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 4, h - 6, 16, 16);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
