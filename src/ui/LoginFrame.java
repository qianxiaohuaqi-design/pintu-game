package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static ui.GameStyle.*;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private IconButton passwordToggleBtn;
    private boolean passwordVisible = false;

    public static final java.util.Map<String, String> userDatabase = new java.util.HashMap<>();
    static {
        userDatabase.put("zhangsan", "123");
        userDatabase.put("lisi", "123456");
        userDatabase.put("admin", "admin");
        userDatabase.put("123", "123");
    }

    public LoginFrame() {
        initFrame();
        initComponents();
        registerQuickStart();
        this.setVisible(true);
    }

    private void initFrame() {
        this.setTitle("拼图游戏 · 登录");
        this.setSize(488, 430);
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
                ImageIcon bg = loadScaled(themedPath("image/register/background.png", "image/UI2/register.png"), 470, 390);
                g.drawImage(bg.getImage(), 0, 0, null);
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        JLabel userLabel = makeIcon("image/login/用户名.png", 47, 17);
        userLabel.setBounds(83, 153, 47, 17);
        root.add(userLabel);

        userField = makeTextField("请输入用户名");
        userField.setBounds(148, 146, 200, 32);
        root.add(userField);

        JLabel passLabel = makeIcon("image/login/密码.png", 32, 16);
        passLabel.setBounds(98, 222, 32, 16);
        root.add(passLabel);

        passField = new JPasswordField();
        styleTextField(passField);
        passField.setEchoChar('●');
        passField.setBounds(148, 214, 190, 32);
        root.add(passField);

        passwordToggleBtn = new IconButton(() -> togglePasswordVisible());
        passwordToggleBtn.setBounds(344, 208, 44, 44);
        root.add(passwordToggleBtn);

        JLabel loginBtn = makeImageButton("image/login/登录按钮.png", "image/login/登录按下.png", 128, 47, () -> doLogin());
        loginBtn.setBounds(96, 302, 128, 47);
        root.add(loginBtn);

        JLabel registerBtn = makeImageButton("image/login/注册按钮.png", "image/login/注册按下.png", 128, 47, () -> doRegister());
        registerBtn.setBounds(252, 302, 128, 47);
        root.add(registerBtn);
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(GameStyle.getFont(Font.BOLD, 16));
        label.setForeground(themeTextColor());
        return label;
    }

    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty()) {
            showMsg(this, "请输入用户名！", "提示");
            return;
        }
        if (pass.isEmpty()) {
            showMsg(this, "请输入密码！", "提示");
            return;
        }
        if (!userDatabase.containsKey(user)) {
            showMsg(this, "用户名不存在，请先注册！", "提示");
            return;
        }
        if (!userDatabase.get(user).equals(pass)) {
            showMsg(this, "密码错误，请重新输入！", "提示");
            return;
        }

        showMsg(this, "登录成功！欢迎：" + user, "登录成功");
        this.setVisible(false);
        this.dispose();
        new SetupFrame(user);
    }

    private void doRegister() {
        this.setVisible(false);
        this.dispose();
        new RegisterFrame();
    }

    private void togglePasswordVisible() {
        passwordVisible = !passwordVisible;
        passField.setEchoChar(passwordVisible ? (char) 0 : '●');
        passwordToggleBtn.repaint();
    }


    private void registerQuickStart() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (!LoginFrame.this.isShowing()) {
                return false;
            }
            if (e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_A) {
                LoginFrame.this.setVisible(false);
                LoginFrame.this.dispose();
                new SetupFrame("admin");
                return true;
            }
            return false;
        });
    }

    private class IconButton extends JButton {
        private final Runnable action;

        IconButton(Runnable action) {
            this.action = action;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> {
                if (action != null) {
                    action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ImageIcon icon = loadRaw(themedPath("image/login/mima.png", "image/UI2/mima.png"));
            Image image = icon.getImage();
            int sourceWidth = Math.max(1, icon.getIconWidth());
            int sourceHeight = Math.max(1, icon.getIconHeight());
            int halfWidth = sourceWidth / 2;
            int baseX = passwordVisible ? halfWidth : 0;
            int cropX = baseX + (int) (halfWidth * 0.12);
            int cropY = (int) (sourceHeight * 0.27);
            int cropW = (int) (halfWidth * 0.76);
            int cropH = (int) (sourceHeight * 0.50);
            g2.drawImage(image,
                    0, 0, getWidth(), getHeight(),
                    cropX, cropY, cropX + cropW, cropY + cropH,
                    null);
            g2.dispose();
        }
    }

    private static class ThemedButton extends JButton {
        private final boolean primary;

        ThemedButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(GameStyle.getFont(Font.BOLD, 18));
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
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();
            Color[] colors = themeButtonColors(primary, pressed, rollover);
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, 16, 16);
            g2.setPaint(new GradientPaint(0, 1, colors[0], 0, getHeight() - 4, colors[1]));
            g2.fillRoundRect(1, 1, getWidth() - 4, getHeight() - 6, 16, 16);
            g2.setColor(colors[2]);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 4, getHeight() - 6, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }}
