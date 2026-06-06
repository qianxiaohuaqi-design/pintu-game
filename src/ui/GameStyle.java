package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * 共享的游戏 UI 样式工具类。
 * 集中管理颜色、字体、图标加载、按钮创建等通用逻辑，避免在各 Frame 中重复定义。
 */
public final class GameStyle {
    private static boolean ui2Theme = false;

    private GameStyle() {
        // 工具类不允许实例化
    }

    // ═══════════════════════════════════════════════════════════
    // 颜色常量
    // ═══════════════════════════════════════════════════════════
    public static boolean isUi2Theme() {
        return ui2Theme;
    }

    public static void setUi2Theme(boolean enabled) {
        ui2Theme = enabled;
    }

    public static String themedPath(String classicPath, String ui2Path) {
        return ui2Theme ? ui2Path : classicPath;
    }

    public static Color themeTextColor() {
        return ui2Theme ? new Color(0x8B2F5B) : TEXT_LIGHT_BROWN;
    }

    public static Color themeBorderColor() {
        return ui2Theme ? new Color(0xDA6F9E) : BORDER_GOLD;
    }

    public static Color themeInputBackground() {
        return ui2Theme ? new Color(0xFFF0F7) : new Color(0xFFFAE8);
    }

    public static Color[] themeButtonColors(boolean primary, boolean pressed, boolean rollover) {
        if (ui2Theme) {
            if (primary) {
                return new Color[] {
                        pressed ? new Color(0xCF4B86) : rollover ? new Color(0xFFA4C8) : new Color(0xF27AAA),
                        pressed ? new Color(0xA32D66) : new Color(0xC7417B),
                        new Color(0xFFF0F7)
                };
            }
            return new Color[] {
                    pressed ? new Color(0xB8427A) : rollover ? new Color(0xEE80B0) : new Color(0xD9689D),
                    pressed ? new Color(0x7E2452) : new Color(0xA7386D),
                    new Color(0x7B1B4C)
            };
        }

        if (primary) {
            return new Color[] {
                    pressed ? new Color(0x32884A) : rollover ? new Color(0x52C271) : new Color(0x47B264),
                    pressed ? new Color(0x1E5E2F) : new Color(0x26773B),
                    new Color(0xF7CE5B)
            };
        }
        return new Color[] {
                pressed ? new Color(0xA56E33) : rollover ? new Color(0xDB9B4F) : new Color(0xCE893F),
                pressed ? new Color(0x72431A) : new Color(0x7D4715),
                new Color(0x5E3610)
        };
    }

    public static final Color TEXT_DARK        = new Color(0x5A3000);
    public static final Color TEXT_BROWN       = new Color(0x7B3A00);
    public static final Color TEXT_LIGHT_BROWN = new Color(0x6A3A10);
    public static final Color TEXT_WARM        = new Color(0x8B6B43);
    public static final Color BORDER_GOLD      = new Color(0xC49A3C);
    public static final Color BG_CREAM         = new Color(0xFFFAE8);
    public static final Color BG_WARM          = new Color(0xFDFBF7);
    public static final Color CARET_BROWN      = new Color(0x7B3A00);
    public static final Color PLACEHOLDER      = new Color(0xAA9070);
    public static final Color CODE_BG          = new Color(0xFFF5D0);
    public static final Color HEADER_BG        = new Color(0xF3EDE2);

    // ═══════════════════════════════════════════════════════════
    // 字体
    // ═══════════════════════════════════════════════════════════
    public static final String FONT_NAME = "微软雅黑";

    public static Font getFont(int style, int size) {
        return new Font(FONT_NAME, style, size);
    }

    // ═══════════════════════════════════════════════════════════
    // 通用输入框
    // ═══════════════════════════════════════════════════════════

    /** 创建带占位提示的文本框 */
    public static JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(PLACEHOLDER);
                    g.setFont(GameStyle.getFont(Font.PLAIN, 12));
                    Insets ins = getInsets();
                    g.drawString(placeholder, ins.left + 4, getHeight() / 2 + 5);
                }
            }
        };
        styleTextField(tf);
        return tf;
    }

    /** 统一输入框样式 */
    public static void styleTextField(JTextField tf) {
        tf.setFont(getFont(Font.PLAIN, 14));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(themeInputBackground());
        tf.setCaretColor(CARET_BROWN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeBorderColor(), 2, true),
                new EmptyBorder(2, 8, 2, 8)));
        tf.setOpaque(true);
    }

    // ═══════════════════════════════════════════════════════════
    // 图标
    // ═══════════════════════════════════════════════════════════

    /** 加载并缩放图标为 JLabel */
    public static JLabel makeIcon(String path, int w, int h) {
        JLabel lbl = new JLabel(loadScaled(path, w, h));
        lbl.setOpaque(false);
        return lbl;
    }

    /** 加载图片并缩放到指定尺寸 */
    public static ImageIcon loadScaled(String path, int w, int h) {
        ImageIcon raw = loadRaw(path);
        if (raw.getIconWidth() <= 0) {
            return new ImageIcon();
        }
        Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /** 加载原始图片（不缩放） */
    public static ImageIcon loadRaw(String path) {
        java.net.URL url = GameStyle.class.getResource("/" + path);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            return new ImageIcon(path);
        }
    }

    /** 加载图标（快捷方法，无缩放） */
    public static ImageIcon getIcon(String path) {
        return loadRaw(path);
    }

    /** 加载图标并缩放（快捷方法） */
    public static ImageIcon getIcon(String path, int w, int h) {
        return loadScaled(path, w, h);
    }

    // ═══════════════════════════════════════════════════════════
    // 图片按钮
    // ═══════════════════════════════════════════════════════════

    /** 创建可按压效果的图片按钮 JLabel */
    public static JLabel makeImageButton(String normalPath, String pressedPath,
                                          int w, int h, Runnable action) {
        ImageIcon normalIcon  = loadScaled(normalPath, w, h);
        ImageIcon pressedIcon = loadScaled(pressedPath, w, h);
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

    /** 创建带文字的小型按钮 JLabel（不上浮、无图片，仅用背景色+圆角边框） */
    public static JLabel makeStyledLabelButton(String text, int w, int h, Runnable action) {
        JLabel btn = new JLabel(text, SwingConstants.CENTER);
        btn.setFont(getFont(Font.BOLD, 12));
        btn.setForeground(TEXT_BROWN);
        btn.setOpaque(true);
        btn.setBackground(BG_CREAM);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_GOLD, 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBounds(0, 0, w, h);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && btn.contains(e.getPoint()) && action != null) {
                    action.run();
                }
            }
        });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // 通用弹框
    // ═══════════════════════════════════════════════════════════

    public static void paintWoodFrameBackground(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0xEED19C));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(0xF8E58A));
        g2.fillRoundRect(8, 8, width - 16, height - 16, 28, 28);

        g2.setColor(new Color(0xFFF1A5));
        g2.fillRoundRect(20, 20, width - 40, height - 40, 28, 28);

        g2.setStroke(new BasicStroke(9f));
        g2.setColor(new Color(0x9B5C18));
        g2.drawRoundRect(8, 8, width - 16, height - 16, 28, 28);
        g2.setStroke(new BasicStroke(5f));
        g2.setColor(new Color(0xCE9136));
        g2.drawRoundRect(13, 13, width - 26, height - 26, 24, 24);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0x6C3B11));
        g2.drawRoundRect(4, 4, width - 8, height - 8, 30, 30);

        g2.setColor(new Color(0xD7, 0xA3, 0x46, 70));
        g2.fillOval(18, 50, 42, 42);
        g2.fillOval(width - 68, 72, 58, 58);
        g2.fillOval(22, height - 90, 84, 60);
        g2.fillOval(width - 112, height - 98, 92, 68);

        g2.setColor(new Color(0x7B, 0x3A, 0x00, 85));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(24, 22, 44, 18);
        g2.drawLine(31, 25, 36, 42);
        g2.drawLine(width - 58, 31, width - 38, 43);
        g2.drawLine(width - 50, 44, width - 62, 55);
        g2.drawLine(12, height / 2, 34, height / 2 - 6);
        g2.drawLine(width - 44, height - 80, width - 24, height - 92);
    }

    public static void paintWoodTitle(Graphics2D g2, String text, int x, int y, int width, int height, int fontSize) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRoundRect(x + 4, y + 8, width - 8, height - 12, 28, 28);

        g2.setPaint(new GradientPaint(x, y, new Color(0xFFC94E), x, y + height, new Color(0xB56617)));
        g2.fillRoundRect(x, y, width, height - 8, 28, 28);

        g2.setPaint(new GradientPaint(x, y + 6, new Color(0xFF, 0xE8, 0x87, 160), x, y + height / 2, new Color(0xFF, 0xFF, 0xFF, 0)));
        g2.fillRoundRect(x + 12, y + 8, width - 24, Math.max(18, height / 3), 18, 18);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(0x7B3A00));
        g2.drawRoundRect(x + 1, y + 1, width - 3, height - 11, 28, 28);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0xF7D66E));
        g2.drawRoundRect(x + 7, y + 7, width - 15, height - 23, 20, 20);

        Font font = getFont(Font.BOLD, fontSize);
        FontMetrics metrics = g2.getFontMetrics(font);
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + ((height - 8 - metrics.getHeight()) / 2) + metrics.getAscent() + 1;
        drawOutlinedText(g2, text, font, textX, textY);
    }

    private static void drawOutlinedText(Graphics2D g2, String text, Font font, int x, int y) {
        g2.setFont(font);
        g2.setColor(Color.WHITE);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.abs(dx) + Math.abs(dy) <= 3 && (dx != 0 || dy != 0)) {
                    g2.drawString(text, x + dx, y + dy);
                }
            }
        }
        g2.setColor(new Color(0x4F2600));
        g2.drawString(text, x + 2, y + 2);
        g2.setColor(new Color(0x7B3A00));
        g2.drawString(text, x, y);
    }

    public static void showMsg(Component parent, String msg, String title) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════
    // 图像处理
    // ═══════════════════════════════════════════════════════════

    /**
     * 将任意尺寸的图片居中裁剪为正方形后缩放到目标尺寸。
     * 用于拼图原图的标准化处理。
     */
    public static BufferedImage normalizePuzzleImage(BufferedImage source, int targetSize) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int xStart = (source.getWidth() - side) / 2;
        int yStart = (source.getHeight() - side) / 2;

        BufferedImage normalized = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = normalized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source,
                0, 0, targetSize, targetSize,
                xStart, yStart, xStart + side, yStart + side,
                null);
        g.dispose();
        return normalized;
    }
}
