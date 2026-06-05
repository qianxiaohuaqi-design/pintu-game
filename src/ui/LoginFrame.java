package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class LoginFrame extends JFrame {

    // ── 控件 ──────────────────────────────────────────────
    private JTextField     userField;
    private JPasswordField passField;
    private JTextField     codeField;
    private JLabel         codeLabel;     // 显示验证码文字
    private JLabel         eyeLabel;      // 显示/隐藏密码

    private JLabel loginBtn;
    private JLabel registerBtn;

    // 当前验证码
    private String currentCode;

    // 图标路径前缀
    private static final String IMG = "image/login/";

    // 静态用户数据库，预置一些默认账号
    public static final java.util.Map<String, String> userDatabase = new java.util.HashMap<>();
    static {
        userDatabase.put("zhangsan", "123");
        userDatabase.put("lisi", "123456");
        userDatabase.put("admin", "admin");
        userDatabase.put("123", "123"); // 方便测试的默认账号
    }

    public LoginFrame() {
        initFrame();
        initComponents();

        // 注册全局 Shift+A 快捷键以便测试时快速进入游戏（免登录）
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (!LoginFrame.this.isShowing()) {
                    return false;
                }
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_A) {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                        LoginFrame.this.setVisible(false);
                        LoginFrame.this.dispose();
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
        this.setTitle("拼图游戏 · 登录");
        this.setSize(488, 430);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        // 使用 null 布局以精确定位（与 GameFrame 一致）
        this.getContentPane().setLayout(null);
    }

    // ── 布局所有组件 ──────────────────────────────────────
    private void initComponents() {
        // 窗口客户区：488×430（去掉标题栏约 30px → 内容区约 400 高）
        // background.png 实际像素：469×421，缩放填满整个内容区

        // ── 1. 用户名图标 ──
        JLabel userIcon = makeIcon(IMG + "用户名.png", 47, 17);
        userIcon.setBounds(116, 135, 47, 17);
        add(userIcon);

        // ── 2. 用户名输入框 ──
        userField = makeTextField("请输入用户名");
        userField.setBounds(195, 127, 200, 32);
        add(userField);

        // ── 3. 密码图标 ──
        JLabel passIcon = makeIcon(IMG + "密码.png", 32, 16);
        passIcon.setBounds(130, 195, 32, 16);
        add(passIcon);

        // ── 4. 密码输入框 ──
        passField = new JPasswordField();
        styleTextField(passField);
        passField.setEchoChar('●');
        passField.setBounds(195, 187, 200, 32);
        add(passField);

        // ── 5. 显示密码眼睛按钮 ──
        eyeLabel = makeIcon(IMG + "显示密码.png", 18, 29);
        eyeLabel.setBounds(400, 188, 18, 29);
        eyeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeLabel.addMouseListener(new MouseAdapter() {
            boolean showing = false;
            @Override public void mouseClicked(MouseEvent e) {
                showing = !showing;
                passField.setEchoChar(showing ? (char) 0 : '●');
                String imgPath = showing ? IMG + "显示密码按下.png" : IMG + "显示密码.png";
                eyeLabel.setIcon(loadScaled(imgPath, showing ? 21 : 18, showing ? 32 : 29));
                if (showing) {
                    eyeLabel.setBounds(398, 187, 21, 32);
                } else {
                    eyeLabel.setBounds(400, 188, 18, 29);
                }
            }
        });
        add(eyeLabel);

        // ── 6. 验证码图标 ──
        JLabel codeIcon = makeIcon(IMG + "验证码.png", 56, 21);
        codeIcon.setBounds(107, 255, 56, 21);
        add(codeIcon);

        // ── 7. 验证码输入框 ──
        codeField = makeTextField("请输入验证码");
        codeField.setBounds(195, 247, 100, 32);
        add(codeField);

        // ── 8. 验证码显示标签（点击刷新）──
        currentCode = generateCode();
        codeLabel = new JLabel(currentCode);
        codeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        codeLabel.setForeground(new Color(0x7B3A00));
        codeLabel.setOpaque(true);
        codeLabel.setBackground(new Color(0xFFF5D0));
        codeLabel.setBorder(BorderFactory.createLineBorder(new Color(0xC49A3C), 2, true));
        codeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        codeLabel.setBounds(305, 247, 90, 32);
        codeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        codeLabel.setToolTipText("点击刷新验证码");
        codeLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentCode = generateCode();
                codeLabel.setText(currentCode);
            }
        });
        add(codeLabel);

        // ── 9. 登录按钮 ──
        loginBtn = makeButton(IMG + "登录按钮.png", IMG + "登录按下.png", 128, 47);
        loginBtn.setBounds(80, 310, 128, 47);
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { doLogin(); }
        });
        add(loginBtn);

        // ── 10. 注册按钮 ──
        registerBtn = makeButton(IMG + "注册按钮.png", IMG + "注册按下.png", 128, 47);
        registerBtn.setBounds(260, 310, 128, 47);
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { doRegister(); }
        });
        add(registerBtn);

        // ── 11. 背景图（最后添加，渲染在最底层）──
        JLabel bg = new JLabel(loadScaled(IMG + "background.png", 470, 390));
        bg.setBounds(0, 0, 470, 390);
        add(bg);
    }

    // ── 登录逻辑 ──────────────────────────────────────────
    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String code = codeField.getText().trim();

        if (user.isEmpty()) {
            showMsg("请输入用户名！", "提示");
            return;
        }
        if (pass.isEmpty()) {
            showMsg("请输入密码！", "提示");
            return;
        }
        if (!code.equalsIgnoreCase(currentCode)) {
            showMsg("验证码错误，请重新输入！", "验证码错误");
            currentCode = generateCode();
            codeLabel.setText(currentCode);
            codeField.setText("");
            return;
        }
        // 验证用户名和密码
        if (!userDatabase.containsKey(user)) {
            showMsg("用户名不存在，请先注册！", "提示");
            return;
        }
        if (!userDatabase.get(user).equals(pass)) {
            showMsg("密码错误，请重新输入！", "提示");
            return;
        }

        showMsg("登录成功！欢迎：" + user, "登录成功");
        this.setVisible(false);
        this.dispose();
        new SetupFrame(user);
    }

    // ── 注册逻辑 ──────────────────────────────────────────
    private void doRegister() {
        this.setVisible(false);
        new RegisterFrame();
    }

    // ── 工具方法 ──────────────────────────────────────────

    /** 生成4位随机字母数字验证码 */
    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /** 创建带占位提示的文本框 */
    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
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
                new EmptyBorder(2, 8, 2, 8)
        ));
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
