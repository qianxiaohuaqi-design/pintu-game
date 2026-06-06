package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static ui.GameStyle.*;

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
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.getContentPane().setLayout(null);
    }

    // ── 布局所有组件 ──────────────────────────────────────
    private void initComponents() {
        // ── 1. 用户名图标 ──
        JLabel userIcon = makeIcon(IMG + "注册用户名.png", 79, 17);
        userIcon.setBounds(85, 122, 79, 17);
        add(userIcon);

        // ── 2. 用户名输入框 ──
        userField = makeTextField("请输入注册的用户名");
        userField.setBounds(195, 114, 200, 32);
        add(userField);

        // ── 3. 密码图标 ──
        JLabel passIcon = makeIcon(IMG + "注册密码.png", 64, 16);
        passIcon.setBounds(100, 178, 64, 16);
        add(passIcon);

        // ── 4. 密码输入框 ──
        passField = new JPasswordField();
        styleTextField(passField);
        passField.setEchoChar('●');
        passField.setBounds(195, 170, 200, 32);
        add(passField);

        // ── 5. 确认密码图标 ──
        JLabel confirmPassIcon = makeIcon(IMG + "再次输入密码.png", 96, 17);
        confirmPassIcon.setBounds(68, 234, 96, 17);
        add(confirmPassIcon);

        // ── 6. 确认密码输入框 ──
        confirmPassField = new JPasswordField();
        styleTextField(confirmPassField);
        confirmPassField.setEchoChar('●');
        confirmPassField.setBounds(195, 226, 200, 32);
        add(confirmPassField);

        // ── 7. 注册按钮 ──
        registerBtn = makeImageButton(IMG + "注册按钮.png", IMG + "注册按下.png", 128, 47, () -> doRegister());
        registerBtn.setBounds(80, 298, 128, 47);
        add(registerBtn);

        // ── 8. 重置按钮 ──
        resetBtn = makeImageButton(IMG + "重置按钮.png", IMG + "重置按下.png", 128, 47, () -> doReset());
        resetBtn.setBounds(260, 298, 128, 47);
        add(resetBtn);

        // ── 9. 返回登录按钮（使用统一样式的小型按钮）──
        JLabel backBtn = makeStyledLabelButton("返回登录", 90, 30, () -> {
            this.setVisible(false);
            this.dispose();
            new LoginFrame();
        });
        backBtn.setBounds(12, 12, 92, 30);
        add(backBtn);

        // ── 10. 背景图（最后添加，渲染在最底层）──
        JLabel bg = new JLabel(loadScaled(IMG + "background.png", 470, 390));
        bg.setBounds(0, 0, 470, 390);
        add(bg);
    }

    // ── 注册逻辑 ──────────────────────────────────────────
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

        // 验证用户名是否已被占用
        if (LoginFrame.userDatabase.containsKey(user)) {
            showMsg(this, "用户名已存在，请直接登录或换一个用户名！", "提示");
            return;
        }

        // 保存注册的用户信息
        LoginFrame.userDatabase.put(user, pass);

        // 模拟注册成功
        showMsg(this, "注册成功！马上为您跳转到登录界面", "成功");
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
}
