package UI.Chung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Bộ icon dùng chung được VẼ bằng Java2D.
 *
 * Lý do: ký tự Unicode/emoji có thể hiện ô vuông trên JDK và máy trường.
 * Icon này không cần font biểu tượng, không cần Internet hay thư viện ngoài.
 * Các icon đều vẽ trên hệ tọa độ chuẩn 24 x 24 và tự co theo kích thước mong muốn.
 */
public final class BieuTuong implements Icon {

    public enum Loai {
        TRANG_CHU, TAI_KHOAN, PHAN_QUYEN, HE_THONG, BAO_CAO,
        DANG_XUAT, THEM, SUA, XOA, LUU, TAI_LAI, TIM_KIEM,
        THONG_BAO, KIEM_TRA, HUY, TAI_XUONG, DANG_NHAP
    }

    private final Loai loai;
    private final Color mau;
    private final int kichThuoc;

    public BieuTuong(Loai loai, Color mau, int kichThuoc) {
        this.loai = loai;
        this.mau = mau;
        this.kichThuoc = Math.max(12, kichThuoc);
    }

    @Override
    public int getIconWidth() {
        return kichThuoc;
    }

    @Override
    public int getIconHeight() {
        return kichThuoc;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x, y);
            g.scale(kichThuoc / 24.0, kichThuoc / 24.0);
            g.setColor(mau);
            g.setStroke(new BasicStroke(1.85f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            ve(g);
        } finally {
            g.dispose();
        }
    }

    /** Các hình được vẽ từ đường thẳng/đường cong, không dùng ký tự emoji. */
    private void ve(Graphics2D g) {
        switch (loai) {
            case TRANG_CHU:
                duong(g, 3, 10, 12, 3, 21, 10);
                duong(g, 5.5, 9.5, 5.5, 21, 18.5, 21, 18.5, 9.5);
                duong(g, 10, 21, 10, 14, 14, 14, 14, 21);
                break;
            case TAI_KHOAN:
                g.draw(new Ellipse2D.Double(8, 3, 7, 7));
                g.draw(new Arc2D.Double(4, 11, 15, 11, 0, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(15.5, 11.5, 7, 9, 0, 95, Arc2D.OPEN));
                g.draw(new Arc2D.Double(15.5, 4.7, 6, 6, -105, 190, Arc2D.OPEN));
                break;
            case PHAN_QUYEN:
                Path2D shield = new Path2D.Double();
                shield.moveTo(12, 2.5);
                shield.lineTo(19.5, 5.3);
                shield.lineTo(19.5, 11.3);
                shield.curveTo(19.5, 16.2, 16.5, 19.4, 12, 21.5);
                shield.curveTo(7.5, 19.4, 4.5, 16.2, 4.5, 11.3);
                shield.lineTo(4.5, 5.3);
                shield.closePath();
                g.draw(shield);
                duong(g, 8.3, 11.7, 11, 14.3, 16, 9);
                break;
            case HE_THONG:
                g.draw(new Ellipse2D.Double(6.5, 6.5, 11, 11));
                g.drawOval(10, 10, 4, 4);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * i / 4;
                    g.drawLine((int) (12 + 6.8 * Math.cos(angle)),
                            (int) (12 + 6.8 * Math.sin(angle)),
                            (int) (12 + 9.5 * Math.cos(angle)),
                            (int) (12 + 9.5 * Math.sin(angle)));
                }
                break;
            case BAO_CAO:
                duong(g, 4, 4, 4, 20, 21, 20);
                g.fillRoundRect(7, 13, 3, 6, 1, 1);
                g.fillRoundRect(12, 9, 3, 10, 1, 1);
                g.fillRoundRect(17, 5, 3, 14, 1, 1);
                break;
            case DANG_XUAT:
                duong(g, 10, 4, 4, 4, 4, 20, 10, 20);
                duong(g, 12, 12, 21, 12, 17, 8);
                duong(g, 21, 12, 17, 16);
                break;
            case THEM:
                duong(g, 12, 4, 12, 20);
                duong(g, 4, 12, 20, 12);
                break;
            case SUA:
                duong(g, 5, 17, 4, 21, 8, 20);
                duong(g, 5, 17, 16, 6, 19, 9, 8, 20);
                duong(g, 16, 6, 18, 4, 21, 7, 19, 9);
                break;
            case XOA:
                duong(g, 4, 7, 20, 7);
                duong(g, 9, 4, 15, 4);
                duong(g, 6, 7, 7, 21, 17, 21, 18, 7);
                duong(g, 10, 11, 10, 18);
                duong(g, 14, 11, 14, 18);
                break;
            case LUU:
            case KIEM_TRA:
                duong(g, 4, 12, 9.5, 17.5, 20, 6);
                break;
            case TAI_LAI:
                g.draw(new Arc2D.Double(4, 4, 16, 16, 35, 295, Arc2D.OPEN));
                duong(g, 18.8, 3.5, 20, 9, 14, 8.5);
                break;
            case TIM_KIEM:
                g.drawOval(4, 4, 12, 12);
                duong(g, 15, 15, 21, 21);
                break;
            case THONG_BAO:
                Path2D bell = new Path2D.Double();
                bell.moveTo(7, 9.5);
                bell.curveTo(7, 3, 17, 3, 17, 9.5);
                bell.lineTo(17, 14.2);
                bell.lineTo(19.5, 18);
                bell.lineTo(4.5, 18);
                bell.lineTo(7, 14.2);
                bell.closePath();
                g.draw(bell);
                g.draw(new Arc2D.Double(9.5, 17.2, 5, 4.5, 180, 180, Arc2D.OPEN));
                break;
            case HUY:
                duong(g, 5, 5, 19, 19);
                duong(g, 19, 5, 5, 19);
                break;
            case TAI_XUONG:
                duong(g, 12, 3, 12, 16, 7, 11);
                duong(g, 12, 16, 17, 11);
                duong(g, 4, 18, 4, 21, 20, 21, 20, 18);
                break;
            case DANG_NHAP:
                duong(g, 3, 12, 20, 12, 15, 7);
                duong(g, 20, 12, 15, 17);
                break;
            default:
                break;
        }
    }

    private static void duong(Graphics2D g, double... toaDo) {
        Path2D path = new Path2D.Double();
        path.moveTo(toaDo[0], toaDo[1]);
        for (int i = 2; i < toaDo.length; i += 2) {
            path.lineTo(toaDo[i], toaDo[i + 1]);
        }
        g.draw(path);
    }
}
