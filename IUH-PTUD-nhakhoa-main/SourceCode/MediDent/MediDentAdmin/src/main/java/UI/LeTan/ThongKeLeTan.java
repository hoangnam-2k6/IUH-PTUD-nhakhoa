package UI.LeTan;

import static UI.LeTan.GiaoDien.GREEN;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.WARNING;
import static UI.LeTan.GiaoDien.card;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.label;

import Class.DuLieuTrangLeTan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Hàng bốn thẻ thống kê ở đầu trang Lễ tân. */
public final class ThongKeLeTan extends JPanel {

    private final JLabel tongBenhNhan = label("—", 25, true);
    private final JLabel choTiepNhan = label("—", 25, true);
    private final JLabel daTiepNhan = label("—", 25, true);
    private final JLabel soPhieuKham = label("—", 25, true);

    public ThongKeLeTan() {
        super(new GridLayout(1, 4, 14, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(700, 92));
        add(theThongKe("Tổng bệnh nhân", tongBenhNhan, PRIMARY, 0));
        add(theThongKe("Chưa nhận hôm nay", choTiepNhan, WARNING, 1));
        add(theThongKe("Đã tiếp nhận hôm nay", daTiepNhan, GREEN, 2));
        add(theThongKe("Phiếu khám hôm nay", soPhieuKham, new Color(0x8B5CF6), 3));
    }

    public void capNhat(DuLieuTrangLeTan data) {
        tongBenhNhan.setText(String.valueOf(data.totalPatients));
        choTiepNhan.setText(String.valueOf(data.waitingToday));
        daTiepNhan.setText(String.valueOf(data.receivedToday));
        soPhieuKham.setText(String.valueOf(data.examsToday));
    }

    /** Thẻ thống kê: biểu tượng trong ô màu nhạt bên trái, tiêu đề mờ và số liệu lớn. */
    private static JPanel theThongKe(String title, JLabel value, Color accent, int iconKind) {
        KhungBoGoc card = card(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 16, 18, 14));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new HuyHieuBieuTuong(iconKind, accent));

        JPanel texts = new JPanel(new BorderLayout(0, 0));
        texts.setOpaque(false);
        value.setForeground(TEXT);
        value.setFont(font(Font.BOLD, 28));
        texts.add(label(title, 12, false), BorderLayout.NORTH);
        texts.add(value, BorderLayout.CENTER);
        card.add(iconWrap, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        return card;
    }
}
