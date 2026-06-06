package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static ui.GameStyle.*;

public class RegisterFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;

    public RegisterFrame() {
        initFrame();
        initComponents();
        registerQuickStart();
        this.setVisible(true);
    }

    private void initFrame() {
        this.setTitle("拼图游戏 · 注册");
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

        JLabel userLabel = makeIcon("image/register/注册用户名.png", 79, 17);
        userLabel.setBounds(95, 125, 79, 17);
        root.add(userLabel);

        userField = makeTextField("请输入注册的用户名");
        userField.setBounds(192, 118, 200, 32);
        root.add(userField);

        JLabel passLabel = makeIcon("image/register/注册密码.png", 64, 16);
        passLabel.setBounds(110, 188, 64, 16);
        root.add(passLabel);

        passField = new JPasswordField();
        styleTextField(passField);
        passField.setEchoChar('●');
        passField.setBounds(192, 180, 200, 32);
        root.add(passField);

        JLabel confirmLabel = makeIcon("image/register/再次输入密码.png", 96, 17);
        confirmLabel.setBounds(78, 249, 96, 17);
        root.add(confirmLabel);

        confirmPassField = new JPasswordField();
        styleTextField(confirmPassField);
        confirmPassField.setEchoChar('●');
        confirmPassField.setBounds(192, 242, 200, 32);
        root.add(confirmPassField);

        JLabel registerBtn = makeImageButton("image/register/注册按钮.png", "image/register/注册按下.png", 128, 47, () -> doRegister());
        registerBtn.setBounds(96, 312, 128, 47);
        root.add(registerBtn);

        JLabel resetBtn = makeImageButton("image/register/重置按钮.png", "image/register/重置按下.png", 128, 47, () -> doReset());
        resetBtn.setBounds(260, 312, 128, 47);
        root.add(resetBtn);

        ThemedButton backBtn = new ThemedButton("返回登录", false);
        backBtn.setFont(GameStyle.getFont(Font.BOLD, 13));
        backBtn.setBounds(16, 18, 96, 32);
        backBtn.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            new LoginFrame();
        });
        root.add(backBtn);
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(GameStyle.getFont(Font.BOLD, 15));
        label.setForeground(themeTextColor());
        return label;
    }

    private void doRegister() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();

        if (user.isEmpty()) {
            showMsg(this, "用户名不能为空！", "提示");
            return;
        }
        if (pass.isEmpty()) {
            showMsg(this, "密码不能为空！", "提示");
            return;
        }
        if (confirmPass.isEmpty()) {
            showMsg(this, "请再次输入密码！", "提示");
            return;
        }
        if (!pass.equals(confirmPass)) {
            showMsg(this, "两次输入的密码不一致！", "错误");
            return;
        }
        if (LoginFrame.userDatabase.containsKey(user)) {
            showMsg(this, "用户名已存在，请直接登录或换一个用户名！", "提示");
            return;
        }

        LoginFrame.userDatabase.put(user, pass);
        showMsg(this, "注册成功！马上为您跳转到登录界面", "成功");
        this.setVisible(false);
        this.dispose();
        new LoginFrame();
    }

    private void doReset() {
        userField.setText("");
        passField.setText("");
        confirmPassField.setText("");
        userField.requestFocus();
    }

    private void registerQuickStart() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (!RegisterFrame.this.isShowing()) {
                return false;
            }
            if (e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_A) {
                RegisterFrame.this.setVisible(false);
                RegisterFrame.this.dispose();
                new SetupFrame("admin");
                return true;
            }
            return false;
        });
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
    }
}
