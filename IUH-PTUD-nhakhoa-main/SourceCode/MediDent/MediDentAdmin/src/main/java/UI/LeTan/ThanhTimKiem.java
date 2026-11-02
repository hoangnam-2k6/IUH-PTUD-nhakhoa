package UI.LeTan;

import static UI.LeTan.GiaoDien.font;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.text.Normalizer;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Thanh tìm kiếm có nút chọn tiêu chí lọc (biểu tượng phễu) và ô nhập từ khóa.
 *
 * Lọc nhanh có kiểm soát từng cột:
 * - Nhập toàn chữ số: khớp CHÍNH XÁC cột mã; cột SĐT chỉ xét khi nhập từ 6 số.
 * - Còn lại: tìm chứa (không phân biệt hoa thường và dấu) trong các cột văn bản.
 * Không quét toàn bộ cột nên "6" không khớp nhầm mã NV, ngày khám hay STT.
 */
public final class ThanhTimKiem {

    private ThanhTimKiem() { }

    /** Một tiêu chí lọc: tên hiển thị trong menu và các cột (theo chỉ số model) được xét. */
    public static final class TieuChi {
        final String label;
        final int[] ids;     // cột mã: khớp chính xác
        final int[] texts;   // cột văn bản: tìm chứa, không phân biệt hoa thường/dấu
        final int[] phones;  // cột SĐT: chỉ xét khi nhập từ 6 số (chế độ Tất cả)

        public TieuChi(String label, int[] ids, int[] texts, int[] phones) {
            this.label = label;
            this.ids = ids;
            this.texts = texts;
            this.phones = phones;
        }
    }

    /** Viết tắt để tạo mảng chỉ số cột: cols(1, 2) tương đương new int[] {1, 2}. */
    public static int[] cols(int... columns) {
        return columns;
    }

    /** Thanh tìm kiếm: nút lọc bên trái, ô nhập từ khóa bên phải. Tiêu chí đầu tiên là mặc định ("Tất cả"). */
    public static JPanel taoThanh(TableRowSorter<DefaultTableModel> sorter, TieuChi[] criteria) {
        OTimKiem field = new OTimKiem("Tìm trong tất cả các trường...");
        NutLoc filterButton = new NutLoc();
        filterButton.setToolTipText("Lọc theo: " + criteria[0].label);

        // Mảng một phần tử để hàm lambda bên dưới đọc và đổi được tiêu chí đang chọn.
        TieuChi[] chosen = {criteria[0]};
        Runnable apply = () -> loc(sorter, field.getText(), chosen[0]);
        field.getDocument().addDocumentListener(new BoLangNgheTimKiem(apply));

        JPopupMenu menu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < criteria.length; i++) {
            TieuChi criterion = criteria[i];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(criterion.label, i == 0);
            item.setFont(font(Font.PLAIN, 12));
            item.addActionListener(event -> {
                chosen[0] = criterion;
                boolean all = criterion == criteria[0];
                filterButton.setActive(!all);
                filterButton.setToolTipText("Lọc theo: " + criterion.label);
                field.setPrompt(all ? "Tìm trong tất cả các trường..."
                        : "Nhập " + criterion.label + " cần lọc...");
                apply.run();
            });
            group.add(item);
            menu.add(item);
            if (i == 0) menu.addSeparator();
        }
        filterButton.addActionListener(event ->
                menu.show(filterButton, 0, filterButton.getHeight() + 4));

        // Bọc nút trong GridBagLayout để nút nhỏ nằm giữa theo chiều dọc, không bị kéo cao bằng ô tìm kiếm.
        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);
        holder.add(filterButton);

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bar.add(holder, BorderLayout.WEST);
        bar.add(field, BorderLayout.CENTER);
        return bar;
    }

    private static void loc(TableRowSorter<DefaultTableModel> sorter, String text, TieuChi criterion) {
        String query = text == null ? "" : text.trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        boolean digits = query.chars().allMatch(Character::isDigit);
        String id = query.replaceFirst("^0+(?!$)", ""); // bỏ số 0 đứng đầu: "007" -> "7"
        String folded = fold(query);

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (digits) {
                    if (khopMa(entry, criterion.ids, id)) return true;
                    if (query.length() >= 6 && khopSdt(entry, criterion.phones, query)) return true;
                }
                return khopChu(entry, criterion.texts, folded);
            }
        });
    }

    private static boolean khopMa(RowFilter.Entry<?, ?> entry, int[] columns, String id) {
        for (int column : columns) {
            if (String.valueOf(entry.getValue(column)).trim().equals(id)) return true;
        }
        return false;
    }

    private static boolean khopSdt(RowFilter.Entry<?, ?> entry, int[] columns, String query) {
        for (int column : columns) {
            String phone = String.valueOf(entry.getValue(column)).replaceAll("\\D", "");
            if (phone.contains(query)) return true;
        }
        return false;
    }

    private static boolean khopChu(RowFilter.Entry<?, ?> entry, int[] columns, String folded) {
        for (int column : columns) {
            if (fold(String.valueOf(entry.getValue(column))).contains(folded)) return true;
        }
        return false;
    }

    /** Bỏ dấu tiếng Việt và hạ chữ thường để tìm kiếm không phụ thuộc dấu. */
    private static String fold(String value) {
        String plain = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return plain.toLowerCase(Locale.ROOT);
    }
}
