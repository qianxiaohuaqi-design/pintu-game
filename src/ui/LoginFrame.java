package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import static ui.GameStyle.*;

public class LoginFrame extends JFrame {

    // ── 控件 ──────────────────────────────────────────────
    private JTextField     userField;
    private JPasswordField passField;
    private JTextField     codeField;
    private JLabel         codeLabel;     // 显示验证码文字
    private JLabel         eyeLabel;      // 显示/隐藏密码
    private boolean        passwordVisible = false;

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
        this.getContentPane().setLayout(null);
    }

    // ── 布局所有组件 ──────────────────────────────────────
    private void initComponents() {
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
        passField.setEchoChar('●'); // 默认用圆点遮盖密码
        passField.setBounds(195, 187, 190, 32);
        add(passField);

        // ── 5. 显示/隐藏密码按钮 ──
        eyeLabel = makeStyledLabelButton("显", 42, 32, () -> togglePasswordVisible());
        eyeLabel.setFont(GameStyle.getFont(Font.BOLD, 14));
        eyeLabel.setBounds(392, 187, 42, 32);
        eyeLabel.setToolTipText("显示或隐藏密码");
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
        codeLabel.setFont(GameStyle.getFont(Font.BOLD, 18));
        codeLabel.setForeground(TEXT_BROWN);
        codeLabel.setOpaque(true);
        codeLabel.setBackground(CODE_BG);
        codeLabel.setBorder(BorderFactory.createLineBorder(BORDER_GOLD, 2, true));
        codeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        codeLabel.setBounds(305, 247, 90, 32);
        codeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        codeLabel.setToolTipText("点击刷新验证码");
        codeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && codeLabel.contains(e.getPoint())) {
                    currentCode = generateCode();
                    codeLabel.setText(currentCode);
                }
            }
        });
        add(codeLabel);

        // ── 9. 登录按钮 ──
        loginBtn = makeImageButton(IMG + "登录按钮.png", IMG + "登录按下.png", 128, 47, () -> doLogin());
        loginBtn.setBounds(80, 310, 128, 47);
        add(loginBtn);

        // ── 10. 注册按钮 ──
        registerBtn = makeImageButton(IMG + "注册按钮.png", IMG + "注册按下.png", 128, 47, () -> doRegister());
        registerBtn.setBounds(260, 310, 128, 47);
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
            showMsg(this, "请输入用户名！", "提示");
            return;
        }
        if (pass.isEmpty()) {
            showMsg(this, "请输入密码！", "提示");
            return;
        }
        if (!code.equalsIgnoreCase(currentCode)) {
            showMsg(this, "验证码错误，请重新输入！", "验证码错误");
            currentCode = generateCode();
            codeLabel.setText(currentCode);
            codeField.setText("");
            return;
        }
        // 验证用户名和密码
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

    // ── 注册逻辑 ──────────────────────────────────────────
    private void doRegister() {
        this.setVisible(false);
        new RegisterFrame();
    }

    private void togglePasswordVisible() {
        passwordVisible = !passwordVisible;
        passField.setEchoChar(passwordVisible ? (char) 0 : '●');
        eyeLabel.setText(passwordVisible ? "隐" : "显");
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
}
