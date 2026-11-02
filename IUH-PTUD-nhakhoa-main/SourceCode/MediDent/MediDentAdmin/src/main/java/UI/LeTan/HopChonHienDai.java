package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;
import static UI.LeTan.GiaoDien.ROW_ALT;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/** Ô chọn (Giới tính, Phòng khám): bo tròn, viền đổi màu khi focus, mũi tên nhỏ, danh sách thả gọn. */
public final class HopChonHienDai extends JComboBox<String> {

    public HopChonHienDai(String[] items) {
        super(items);
        setFont(font(Font.PLAIN, 13));
        setForeground(TEXT);
        setBackground(Color.WHITE);
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(120, 38));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, false);
                setFont(font(Font.PLAIN, 13));
                if (index < 0) {                       // phần đang hiển thị trong ô
                    setOpaque(false);
                    setForeground(HopChonHienDai.this.isEnabled() ? TEXT : MUTED);
                    setBorder(new EmptyBorder(0, 12, 0, 4));
                } else {                               // các dòng trong danh sách thả
                    setOpaque(true);
                    setBackground(selected ? PRIMARY_LIGHT : Color.WHITE);
                    setForeground(TEXT);
                    setBorder(new EmptyBorder(8, 12, 8, 12));
                }
                return this;
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) { repaint(); }
            @Override public void focusLost(FocusEvent event) { repaint(); }
        });
    }

    @Override public void updateUI() {
        setUI(new ComboUI());
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(isEnabled() ? Color.WHITE : ROW_ALT);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
        g2.dispose();

        super.paintComponent(graphics);   // giá trị đang chọn + mũi tên

        g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        boolean active = isEnabled() && (hasFocus() || isPopupVisible());
        g2.setColor(active ? PRIMARY : BORDER);
        g2.setStroke(new BasicStroke(active ? 1.8f : 1.2f));
        float o = active ? 0.9f : 0.6f;
        g2.draw(new RoundRectangle2D.Float(o, o, w - 2 * o - 1, h - 2 * o - 1, 12, 12));
        g2.dispose();
    }

    /** Giao diện ô chọn: không tự tô nền, mũi tên chữ V mảnh, danh sách thả viền mảnh. */
    private static final class ComboUI extends BasicComboBoxUI {
        @Override protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override protected void paintComponent(Graphics graphics) {
                    Graphics2D g2 = (Graphics2D) graphics.create();
                    khuRangCua(g2);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.setColor(comboBox.isEnabled() ? MUTED : new Color(0xCBD5E1));
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawPolyline(new int[] {cx - 4, cx, cx + 4}, new int[] {cy - 2, cy + 2, cy - 2}, 3);
                    g2.dispose();
                }
            };
            button.setBorder(null);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            return button;
        }

        @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Nền do HopChonHienDai tự vẽ (bo góc) nên ở đây không tô gì.
        }

        @Override protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(2, 2, 2, 2)));
            return popup;
        }
    }
}
