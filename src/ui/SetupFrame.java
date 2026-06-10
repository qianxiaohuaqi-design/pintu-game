package ui;

import javax.swing.*;
import java.awt.*;

import static ui.GameStyle.*;

public class SetupFrame extends JFrame {

    private final String username;
    private final Image classicBackground = GameStyle.loadRaw("image/Settings/Settings.png").getImage();
    private final Image ui2Background = GameStyle.loadRaw("image/UI2/Settings.png").getImage();

    private int selectedGridSize = 4;
    private boolean isChallengeMode = false;
    private boolean modeSelected = false;
    private boolean ui2Style = GameStyle.isUi2Theme();

    private JLabel difficultyLabel;
    private ToggleButton btn3x3;
    private ToggleButton btn4x4;
    private ToggleButton btn5x5;
    private ToggleButton btnCasual;
    private ToggleButton btnChallenge;
    private ActionButton startGameBtn;
    private ActionButton backBtn;
    private ThemeSwitchButton themeSwitchBtn;

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
                Image background = ui2Style ? ui2Background : classicBackground;
                if (background != null) {
                    g2.drawImage(background, 0, 0, getWidth(), getHeight(), null);
                }

                int boxHeight = modeSelected ? 230 : 110;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ui2Style ? new Color(255, 255, 255, 78) : new Color(255, 255, 255, 105));
                g2.fillRoundRect(50, 128, 372, boxHeight, 22, 22);
                g2.setColor(ui2Style ? new Color(0xDA6F9E) : new Color(0xC79A55));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(50, 128, 372, boxHeight, 22, 22);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        themeSwitchBtn = new ThemeSwitchButton();
        themeSwitchBtn.setBounds(394, 52, 66, 30);
        themeSwitchBtn.addActionListener(e -> toggleTheme());
        root.add(themeSwitchBtn);

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
        JLabel label = new JLabel(text + "：") {
            @Override
            public void paint(Graphics g) {
                setForeground(ui2Style ? new Color(0x8B2F5B) : TEXT_LIGHT_BROWN);
                super.paint(g);
            }
        };
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

    private void toggleTheme() {
        ui2Style = !ui2Style;
        GameStyle.setUi2Theme(ui2Style);
        themeSwitchBtn.setSelectedState(ui2Style);
        repaintAllControls();
    }

    private void repaintAllControls() {
        for (Component component : getContentPane().getComponents()) {
            component.repaint();
        }
        getContentPane().repaint();
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

    private class ToggleButton extends JButton {
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

            ButtonColors colors = getToggleColors(selectedState, pressed, rollover);
            paintButtonBody(g2, w, h, colors, selectedState ? 2.2f : 1.5f, 12);

            if (selectedState) {
                g2.setColor(new Color(255, 255, 255, 220));
                g2.setStroke(new BasicStroke(2.1f));
                g2.drawLine(w - 16, 12, w - 12, 16);
                g2.drawLine(w - 12, 16, w - 7, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ActionButton extends JButton {
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
            ButtonColors colors = getActionColors(primary, pressed, rollover);
            paintButtonBody(g2, w, h, colors, 2f, 16);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ThemeSwitchButton extends JButton {
        private boolean selectedState = false;

        ThemeSwitchButton() {
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("切换界面风格");
        }

        void setSelectedState(boolean selected) {
            selectedState = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            Color trackTop = selectedState ? new Color(0xF7A6C8) : new Color(0xD8A84A);
            Color trackBottom = selectedState ? new Color(0xD65C96) : new Color(0x9D641E);
            Color border = selectedState ? new Color(0x8B2F5B) : new Color(0x6A3A10);

            g2.setColor(new Color(0, 0, 0, 42));
            g2.fillRoundRect(2, 4, w - 4, h - 6, h, h);
            g2.setPaint(new GradientPaint(0, 1, trackTop, 0, h - 2, trackBottom));
            g2.fillRoundRect(1, 1, w - 3, h - 4, h, h);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, w - 3, h - 4, h, h);

            int knob = h - 10;
            int knobX = selectedState ? w - knob - 7 : 6;
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillOval(knobX, 5, knob, knob);
            g2.setColor(selectedState ? new Color(0xD65C96) : new Color(0xB97C2D));
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawOval(knobX, 5, knob, knob);

            g2.dispose();
        }
    }

    private ButtonColors getToggleColors(boolean selected, boolean pressed, boolean rollover) {
        if (ui2Style) {
            if (selected) {
                return new ButtonColors(
                        pressed ? new Color(0xCF4B86) : rollover ? new Color(0xFFA4C8) : new Color(0xF27AAA),
                        pressed ? new Color(0xA32D66) : new Color(0xC7417B),
                        new Color(0xFFF0F7));
            }
            return new ButtonColors(
                    pressed ? new Color(0xB8427A) : rollover ? new Color(0xEE80B0) : new Color(0xD9689D),
                    pressed ? new Color(0x7E2452) : new Color(0xA7386D),
                    new Color(0x7B1B4C));
        }

        if (selected) {
            return new ButtonColors(
                    pressed ? new Color(0x32884A) : rollover ? new Color(0x52C271) : new Color(0x47B264),
                    pressed ? new Color(0x1E5E2F) : new Color(0x26773B),
                    new Color(0xF7CE5B));
        }
        return new ButtonColors(
                pressed ? new Color(0xA56E33) : rollover ? new Color(0xDB9B4F) : new Color(0xCE893F),
                pressed ? new Color(0x72431A) : new Color(0x7D4715),
                new Color(0x5E3610));
    }

    private ButtonColors getActionColors(boolean primary, boolean pressed, boolean rollover) {
        if (ui2Style) {
            if (primary) {
                return new ButtonColors(
                        pressed ? new Color(0xCF4B86) : rollover ? new Color(0xFFA4C8) : new Color(0xF27AAA),
                        pressed ? new Color(0xA32D66) : new Color(0xC7417B),
                        new Color(0xFFF0F7));
            }
            return new ButtonColors(
                    pressed ? new Color(0xB8427A) : rollover ? new Color(0xEE80B0) : new Color(0xD9689D),
                    pressed ? new Color(0x7E2452) : new Color(0xA7386D),
                    new Color(0x7B1B4C));
        }

        if (primary) {
            return new ButtonColors(
                    pressed ? new Color(0x32884A) : rollover ? new Color(0x52C271) : new Color(0x47B264),
                    pressed ? new Color(0x1E5E2F) : new Color(0x26773B),
                    new Color(0xF7CE5B));
        }
        return new ButtonColors(
                pressed ? new Color(0xA56E33) : rollover ? new Color(0xDB9B4F) : new Color(0xCE893F),
                pressed ? new Color(0x72431A) : new Color(0x7D4715),
                new Color(0x5E3610));
    }

    private void paintButtonBody(Graphics2D g2, int w, int h, ButtonColors colors, float borderWidth, int radius) {
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRoundRect(4, 5, w - 8, h - 8, radius, radius);

        g2.setPaint(new GradientPaint(0, 1, colors.top, 0, h - 4, colors.bottom));
        g2.fillRoundRect(1, 1, w - 4, h - 6, radius, radius);

        g2.setColor(colors.border);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRoundRect(1, 1, w - 4, h - 6, radius, radius);
    }

    private static class ButtonColors {
        final Color top;
        final Color bottom;
        final Color border;

        ButtonColors(Color top, Color bottom, Color border) {
            this.top = top;
            this.bottom = bottom;
            this.border = border;
        }
    }
}
