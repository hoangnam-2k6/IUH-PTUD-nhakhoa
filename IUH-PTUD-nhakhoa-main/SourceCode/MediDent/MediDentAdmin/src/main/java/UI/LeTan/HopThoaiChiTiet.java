package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.ROW_ALT;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.escapeHtml;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.label;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Hộp thoại chi tiết một dòng của bảng: mỗi thuộc tính là một khối (nhãn nhỏ + giá trị), có cuộn dọc. */
public final class HopThoaiChiTiet {

    private HopThoaiChiTiet() { }

    /**
     * @param model   model của bảng (cột 0 là STT nên bị bỏ qua)
     * @param row     chỉ số dòng theo model
     * @param titles  tên các thuộc tính, theo thứ tự cột từ cột 1
     * @param caption tiêu đề hộp thoại
     */
    public static void hien(Component parent, DefaultTableModel model, int row, String[] titles, String caption) {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        for (int i = 0; i < titles.length; i++) {
            Object raw = model.getValueAt(row, i + 1); // Bỏ cột STT.
            boolean empty = raw == null || String.valueOf(raw).isBlank();
            String text = empty ? "Chưa cập nhật" : String.valueOf(raw);

            JLabel name = label(titles[i].toUpperCase(), 10, false);
            name.setFont(font(Font.BOLD, 10));
            JLabel value = new JLabel("<html><body style='width:430px'>"
                    + escapeHtml(text).replace("\n", "<br>") + "</body></html>");
            value.setFont(font(empty ? Font.ITALIC : Font.PLAIN, 13));
            value.setForeground(empty ? new Color(0x94A3B8) : TEXT);

            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setBackground(i % 2 == 0 ? Color.WHITE : ROW_ALT);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(10, 18, 11, 18)));
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            value.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.add(name);
            item.add(Box.createVerticalStrut(4));
            item.add(value);
            gc.gridy = i;
            body.add(item, gc);
        }
        // Khối trống ở cuối để các thuộc tính dồn lên trên.
        gc.gridy = titles.length;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setBackground(Color.WHITE);
        body.add(filler, gc);

        JScrollPane view = new JScrollPane(body);
        view.setBorder(BorderFactory.createLineBorder(BORDER));
        view.getVerticalScrollBar().setUnitIncrement(18);
        view.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        view.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        view.setPreferredSize(new Dimension(520, 420));
        JOptionPane.showMessageDialog(parent, view, caption, JOptionPane.PLAIN_MESSAGE);
    }
}
