package UI.LeTan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Bảng màu, phông chữ và các hàm dựng giao diện nhỏ dùng chung cho toàn bộ trang Lễ tân.
 * Các class vẽ riêng trong package này đều dùng chung màu ở đây để giao diện thống nhất.
 */
public final class GiaoDien {

    private GiaoDien() { }

    public static final Color PRIMARY = new Color(0x0284C7);
    public static final Color PRIMARY_DARK = new Color(0x0369A1);
    public static final Color PRIMARY_LIGHT = new Color(0xE0F2FE);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color BACKGROUND = new Color(0xF1F5F9);
    public static final Color ROW_ALT = new Color(0xF8FAFC);
    public static final Color ROW_HOVER = new Color(0xF0F9FF);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color DANGER = new Color(0xEF4444);

    /** Màu avatar xoay vòng theo tên. */
    public static final Color[] AVATAR_COLORS = {
        new Color(0x0284C7), new Color(0x10B981), new Color(0xF59E0B),
        new Color(0x8B5CF6), new Color(0xEC4899), new Color(0x14B8A6)
    };

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    public static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setForeground(bold ? TEXT : MUTED);
        label.setFont(font(bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    public static JLabel tinted(JLabel label, Color color) {
        label.setForeground(color);
        return label;
    }

    public static void datKichThuoc(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    /** Thẻ trắng bo góc có đệm sẵn. */
    public static KhungBoGoc card(LayoutManager layout) {
        KhungBoGoc panel = new KhungBoGoc(layout);
        panel.setBorder(new EmptyBorder(17, 18, 20, 18));
        return panel;
    }

    /** Tiêu đề khu vực: tên + mô tả nhỏ bên trái, thành phần (nhãn đếm, nút...) bên phải. */
    public static JPanel sectionTitle(String title, String subtitle, JComponent count) {
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(label(title, 16, true));
        texts.add(Box.createVerticalStrut(4));
        texts.add(label(subtitle, 11, false));

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        east.setOpaque(false);
        east.add(count);

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.add(texts, BorderLayout.CENTER);
        panel.add(east, BorderLayout.EAST);
        return panel;
    }

    /** Bật khử răng cưa cho nét vẽ và chữ. */
    public static void khuRangCua(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
