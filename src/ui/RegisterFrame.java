package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterFrame extends JFrame {

    // ── 控件 ──────────────────────────────────────────────
    private JTextField userField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;

    private JLabel registerBtn;
    private JLabel resetBtn;

    // 图标路径前缀
    private static final String IMG = "image/register/";

    public RegisterFrame() {
        initFrame();
        initComponents();

        // 注册全局 Shift+A 快捷键以便测试时快速进入游戏（免登录）
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (!RegisterFrame.this.isShowing()) {
                    return false;
                }
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_A) {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                        RegisterFrame.this.setVisible(false);
                        RegisterFrame.this.dispose();
                        new SetupFrame("admin");
                        return true;
                    }
                }
                return false;
            }
        });

        this.setVisible(true);
    }

    // ── 窗口基础设置 ──────────────────────────────────────
    private void initFrame() {
        this.setTitle("拼图游戏 · 注册");
        this.setSize(488, 430);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 如果有关闭返回登录的需求，可以改成销毁并打开登录
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        // 使用 null 布局以精确定位
        this.getContentPane().setLayout(null);
    }

    // ── 布局所有组件 ──────────────────────────────────────
    private void initComponents() {
        // ── 1. 用户名图标 ──
        JLabel userIcon = makeIcon(IMG + "注册用户名.png", 79, 17);
        userIcon.setBounds(85, 135, 79, 17);
        add(userIcon);

        // ── 2. 用户名输入框 ──
        userField = makeTextField("请输入注册的用户名");
        userField.setBounds(195, 127, 200, 32);
        add(userField);

        // ── 3. 密码图标 ──
        JLabel passIcon = makeIcon(IMG + "注册密码.png", 64, 16);
        passIcon.setBounds(100, 195, 64, 16);
        add(passIcon);

        // ── 4. 密码输入框 ──
        passField = new JPasswordField();
        styleTextField(passField);
        passField.setEchoChar('●');
        passField.setBounds(195, 187, 200, 32);
        add(passField);

        // ── 5. 确认密码图标 ──
        JLabel confirmPassIcon = makeIcon(IMG + "再次输入密码.png", 96, 17);
        confirmPassIcon.setBounds(68, 255, 96, 17);
        add(confirmPassIcon);

        // ── 6. 确认密码输入框 ──
        confirmPassField = new JPasswordField();
        styleTextField(confirmPassField);
        confirmPassField.setEchoChar('●');
        confirmPassField.setBounds(195, 247, 200, 32);
        add(confirmPassField);

        // ── 7. 注册按钮 ──
        registerBtn = makeButton(IMG + "注册按钮.png", IMG + "注册按下.png", 128, 47);
        registerBtn.setBounds(80, 310, 128, 47);
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doRegister();
            }
        });
        add(registerBtn);

        // ── 8. 重置按钮 ──
        resetBtn = makeButton(IMG + "重置按钮.png", IMG + "重置按下.png", 128, 47);
        resetBtn.setBounds(260, 310, 128, 47);
        resetBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doReset();
            }
        });
        add(resetBtn);

        // ── 9. 返回登录按钮（额外添加一个方便操作的小按钮） ──
        JButton backBtn = new JButton("返回登录");
        backBtn.setBounds(10, 10, 90, 30);
        backBtn.setFocusPainted(false);
        backBtn.setBackground(new Color(0xFFFAE8));
        backBtn.setForeground(new Color(0x7B3A00));
        backBtn.setFont(new Font("微软雅黑", Font.BOLD, 12));
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(0xC49A3C), 2, true));
        backBtn.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            new LoginFrame();
        });
        add(backBtn);

        // ── 10. 背景图（最后添加，渲染在最底层）──
        JLabel bg = new JLabel(loadScaled(IMG + "background.png", 468, 460));
        bg.setBounds(0, 0, 470, 390);
        add(bg);
    }

    // ── 注册逻辑 ──────────────────────────────────────────
    private void doRegister() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();

        if (user.isEmpty()) {
            showMsg("用户名不能为空！", "提示");
            return;
        }
        if (pass.isEmpty()) {
            showMsg("密码不能为空！", "提示");
            return;
        }
        if (confirmPass.isEmpty()) {
            showMsg("请再次输入密码！", "提示");
            return;
        }
        if (!pass.equals(confirmPass)) {
            showMsg("两次输入的密码不一致！", "错误");
            return;
        }

        // 验证用户名是否已被占用
        if (LoginFrame.userDatabase.containsKey(user)) {
            showMsg("用户名已存在，请直接登录或换一个用户名！", "提示");
            return;
        }

        // 保存注册的用户信息
        LoginFrame.userDatabase.put(user, pass);

        // 模拟注册成功
        showMsg("注册成功！马上为您跳转到登录界面", "成功");
        this.setVisible(false);
        this.dispose();
        new LoginFrame();
    }

    // ── 重置逻辑 ──────────────────────────────────────────
    private void doReset() {
        userField.setText("");
        passField.setText("");
        confirmPassField.setText("");
        userField.requestFocus();
    }

    // ── 工具方法 ──────────────────────────────────────────

    /** 创建带占位提示的文本框 */
    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(new Color(0xAA9070));
                    g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                    Insets ins = getInsets();
                    g.drawString(placeholder, ins.left + 4, getHeight() / 2 + 5);
                }
            }
        };
        styleTextField(tf);
        return tf;
    }

    /** 统一输入框样式 */
    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        tf.setForeground(new Color(0x5A3000));
        tf.setBackground(new Color(0xFFFAE8, false));
        tf.setCaretColor(new Color(0x7B3A00));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC49A3C), 2, true),
                new EmptyBorder(2, 8, 2, 8)));
        tf.setOpaque(true);
    }

    /** 加载并缩放图标为 JLabel */
    private JLabel makeIcon(String path, int w, int h) {
        JLabel lbl = new JLabel(loadScaled(path, w, h));
        lbl.setOpaque(false);
        return lbl;
    }

    /** 创建可按压效果的按钮 JLabel */
    private JLabel makeButton(String normalPath, String pressedPath, int w, int h) {
        ImageIcon normalIcon = loadScaled(normalPath, w, h);
        ImageIcon pressedIcon = loadScaled(pressedPath, w, h);
        JLabel btn = new JLabel(normalIcon);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                btn.setIcon(pressedIcon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setIcon(normalIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setIcon(normalIcon);
            }
        });
        return btn;
    }

    /** 加载图片并缩放到指定尺寸 */
    private ImageIcon loadScaled(String path, int w, int h) {
        java.net.URL url = this.getClass().getResource("/" + path);
        ImageIcon raw;
        if (url != null) {
            raw = new ImageIcon(url);
        } else {
            raw = new ImageIcon(path);
        }
        if (raw.getIconWidth() <= 0) {
            // 图片加载失败时返回空图标
            return new ImageIcon();
        }
        Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /** 弹出信息对话框 */
    private void showMsg(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
