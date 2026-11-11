import ConnectDB.KetNoi;
import Dao.BacSiDao;
import Dao.BenhNhanDao;
import Dao.DichVuDao;
import Dao.HoaDonDao;
import Dao.KetQuaKhamDao;
import Dao.LichHenDao;
import Dao.MaGiamGiaDao;
import Dao.PhieuKhamDao;
import Dao.PhongKhamDao;
import Dao.TaiKhoanDao;
import Entity.BacSi;
import Entity.BenhNhan;
import Entity.ChiTietHoaDon;
import Entity.DichVu;
import Entity.HoaDon;
import Entity.KetQuaKham;
import Entity.LichHen;
import Entity.MaGiamGia;
import Entity.PhieuKham;
import Entity.PhongKham;
import Entity.TaiKhoan;
import UI.LeTan.XuatHoaDon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Kiểm tra nhanh toàn bộ luồng DAO -> SQL Server của phân hệ Lễ tân. */
public class SmokeTest {
    private static int soKiemTra = 0;

    public static void main(String[] args) {
        int maLoi = 0;
        try {
            chay();
            System.out.println("\n=== TẤT CẢ " + soKiemTra + " KIỂM TRA ĐỀU ĐẠT ===");
        } catch (Throwable e) {
            System.err.println("\n*** SMOKE TEST THẤT BẠI ***");
            e.printStackTrace();
            maLoi = 1;
        } finally {
            // Tự dọn dữ liệu test để CSDL luôn sạch, không cần script SQL riêng.
            try {
                donDepDuLieuTest();
                System.out.println("Đã dọn dữ liệu test (CCCD 999999999991, 999999999992).");
            } catch (Exception e) {
                System.err.println("Không dọn được dữ liệu test: " + e.getMessage());
                maLoi = 1;
            }
        }
        if (maLoi != 0) {
            System.exit(maLoi);
        }
    }

    /** Xóa toàn bộ dữ liệu do SmokeTest tạo ra (bệnh nhân test và dữ liệu liên quan). */
    private static void donDepDuLieuTest() throws Exception {
        String[] lenh = {
                "DECLARE @bn TABLE (MaBN INT PRIMARY KEY);"
                        + " INSERT INTO @bn SELECT MaBN FROM dbo.BenhNhan"
                        + " WHERE CCCD IN ('999999999991', '999999999992');"
                        + " DECLARE @pk TABLE (MaPK INT PRIMARY KEY);"
                        + " INSERT INTO @pk SELECT MaPK FROM dbo.PhieuKham"
                        + " WHERE MaBN IN (SELECT MaBN FROM @bn);"
                        + " DELETE FROM dbo.ChiTietHoaDon WHERE MaHD IN"
                        + " (SELECT MaHD FROM dbo.HoaDon WHERE MaPK IN (SELECT MaPK FROM @pk));",
                "DECLARE @bn TABLE (MaBN INT PRIMARY KEY);"
                        + " INSERT INTO @bn SELECT MaBN FROM dbo.BenhNhan"
                        + " WHERE CCCD IN ('999999999991', '999999999992');"
                        + " DECLARE @pk TABLE (MaPK INT PRIMARY KEY);"
                        + " INSERT INTO @pk SELECT MaPK FROM dbo.PhieuKham"
                        + " WHERE MaBN IN (SELECT MaBN FROM @bn);"
                        + " DELETE FROM dbo.HoaDon WHERE MaPK IN (SELECT MaPK FROM @pk);"
                        + " DELETE FROM dbo.KetQuaKham WHERE MaPK IN (SELECT MaPK FROM @pk);"
                        + " DELETE FROM dbo.PhieuKhamDichVu WHERE MaPK IN (SELECT MaPK FROM @pk);"
                        + " DELETE FROM dbo.PhieuKham WHERE MaPK IN (SELECT MaPK FROM @pk);",
                "DELETE FROM dbo.LichHen WHERE MaBN IN"
                        + " (SELECT MaBN FROM dbo.BenhNhan WHERE CCCD IN ('999999999991', '999999999992'));",
                "DELETE FROM dbo.BenhNhan WHERE CCCD IN ('999999999991', '999999999992');"
        };
        try (Connection conn = KetNoi.getConnection();
             Statement st = conn.createStatement()) {
            for (String sql : lenh) {
                st.executeUpdate(sql);
            }
        }
    }

    private static void chay() throws Exception {
        TaiKhoanDao taiKhoanDao = new TaiKhoanDao();
        BenhNhanDao benhNhanDao = new BenhNhanDao();
        LichHenDao lichHenDao = new LichHenDao();
        PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
        KetQuaKhamDao ketQuaKhamDao = new KetQuaKhamDao();
        HoaDonDao hoaDonDao = new HoaDonDao();
        BacSiDao bacSiDao = new BacSiDao();
        PhongKhamDao phongKhamDao = new PhongKhamDao();
        DichVuDao dichVuDao = new DichVuDao();
        MaGiamGiaDao maGiamGiaDao = new MaGiamGiaDao();

        // ---------- Đăng nhập ----------
        TaiKhoan tk = taiKhoanDao.dangNhap("letan", "123456");
        kiemTra(tk != null && tk.getVaiTro().equals("Lễ tân"), "Đăng nhập letan/123456");
        kiemTra(taiKhoanDao.bamSha256("123456").equals(
                "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"), "Băm SHA-256 đúng");
        kiemTraNem(() -> taiKhoanDao.dangNhap("letan", "sai-mat-khau"), "Sai mật khẩu bị từ chối");
        kiemTraNem(() -> taiKhoanDao.dangNhap("quantri", "123456"),
                "Chặn tài khoản quản trị đăng nhập phân hệ Lễ tân");

        // ---------- Danh mục ----------
        kiemTra(benhNhanDao.demTatCa() >= 6, "Có dữ liệu bệnh nhân mẫu");
        kiemTra(benhNhanDao.demDangHoatDong() > 0, "Đếm bệnh nhân đang hoạt động");
        List<BacSi> dsBacSi = bacSiDao.findDangHoatDong();
        List<PhongKham> dsPhong = phongKhamDao.findDangHoatDong();
        List<DichVu> dsDichVu = dichVuDao.findDangHoatDong();
        kiemTra(dsBacSi.size() >= 3, "Có bác sĩ đang hoạt động");
        kiemTra(dsPhong.size() >= 3, "Có phòng khám đang hoạt động");
        kiemTra(dsDichVu.size() >= 10, "Có bảng giá dịch vụ");
        List<MaGiamGia> dsMaGiam = maGiamGiaDao.findDangHoatDong();
        kiemTra(dsMaGiam.size() >= 4, "Có danh mục mã giảm giá đang hoạt động");
        MaGiamGia maPhanTram = dsMaGiam.stream()
                .filter(m -> MaGiamGia.PHAN_TRAM.equals(m.getLoaiGiam()))
                .findFirst().orElseThrow();
        kiemTra(maPhanTram.tinhGiam(new BigDecimal("1000000"))
                        .compareTo(new BigDecimal("1000000")
                                .multiply(maPhanTram.getGiaTri())
                                .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP)) == 0,
                "Tính tiền giảm theo phần trăm");
        MaGiamGia maCoDinh = dsMaGiam.stream()
                .filter(m -> MaGiamGia.SO_TIEN.equals(m.getLoaiGiam()))
                .findFirst().orElseThrow();
        kiemTra(maCoDinh.tinhGiam(new BigDecimal("1000000"))
                        .compareTo(maCoDinh.getGiaTri()) == 0,
                "Tính tiền giảm theo số tiền");

        // ---------- Bệnh nhân ----------
        BenhNhan bn1 = taoBenhNhan("999999999991", "0909999991", "Bệnh nhân Smoke Test A");
        int maBN1 = benhNhanDao.them(bn1);
        kiemTra(maBN1 > 0, "Thêm bệnh nhân A");
        BenhNhan docBn1 = benhNhanDao.findById(maBN1);
        kiemTra(docBn1 != null && "Bệnh nhân Smoke Test A".equals(docBn1.getHoTen()), "Đọc lại bệnh nhân A");
        kiemTra(!benhNhanDao.search("0909999991").isEmpty(), "Tìm bệnh nhân theo số điện thoại");

        docBn1.setGhiChu("Đã cập nhật bởi smoke test");
        benhNhanDao.capNhat(docBn1);
        kiemTra("Đã cập nhật bởi smoke test".equals(benhNhanDao.findById(maBN1).getGhiChu()),
                "Cập nhật hồ sơ bệnh nhân");

        Object[] thongKe = benhNhanDao.thongKeKham(maBN1);
        kiemTra((Integer) thongKe[0] == 0, "Bệnh nhân mới chưa có lượt khám");

        benhNhanDao.doiTrangThai(maBN1, BenhNhan.NGUNG_HOAT_DONG);
        kiemTra(!benhNhanDao.findById(maBN1).dangHoatDong(), "Ngừng hoạt động hồ sơ");
        benhNhanDao.doiTrangThai(maBN1, BenhNhan.HOAT_DONG);
        kiemTra(benhNhanDao.findById(maBN1).dangHoatDong(), "Kích hoạt lại hồ sơ");

        // ---------- Lịch hẹn ----------
        LocalDateTime gioHen = LocalDate.now().plusDays(3).atTime(10, 0);
        int maLH = lichHenDao.tao(maBN1, dsBacSi.get(0).getMaBS(), gioHen, "Lịch smoke test", tk.getMaTK());
        kiemTra(maLH > 0, "Tạo lịch hẹn");
        kiemTra(lichHenDao.findById(maLH).getTrangThai().equals(LichHenDao.CHO_XAC_NHAN),
                "Lịch hẹn mới ở trạng thái chờ xác nhận");

        lichHenDao.xacNhan(maLH);
        kiemTra(lichHenDao.findById(maLH).getTrangThai().equals(LichHenDao.DA_XAC_NHAN), "Xác nhận lịch hẹn");

        lichHenDao.guiYeuCauDoiLich(maLH, gioHen.plusDays(1), "Xin đổi lịch");
        kiemTra(lichHenDao.findById(maLH).coYeuCau(), "Gửi yêu cầu đổi lịch");
        lichHenDao.xuLyYeuCau(maLH, true);
        LichHen sauDoi = lichHenDao.findById(maLH);
        kiemTra(sauDoi.getTrangThai().equals(LichHenDao.DA_XAC_NHAN) && !sauDoi.coYeuCau(),
                "Duyệt yêu cầu đổi lịch");

        // ---------- Tiếp đón / phiếu khám ----------
        PhieuKham pk1 = phieuKhamDao.capSoThuTu(maBN1, dsPhong.get(0).getMaPhong(), maLH);
        kiemTra(pk1.getMaPK() > 0 && pk1.getSoThuTu() > 0, "Check-in và cấp số thứ tự");
        kiemTra(lichHenDao.findById(maLH).getTrangThai().equals(LichHenDao.DA_DEN),
                "Lịch hẹn chuyển sang đã đến");
        kiemTraNem(() -> phieuKhamDao.capSoThuTu(maBN1, dsPhong.get(0).getMaPhong(), null),
                "Chặn cấp số trùng trong ngày");

        // Dịch vụ chọn ngay khi check-in -> lưu vào phiếu khám để nạp sẵn vào hóa đơn.
        List<Integer> maDichVuChon = new ArrayList<>();
        maDichVuChon.add(dsDichVu.get(0).getMaDV());
        maDichVuChon.add(dsDichVu.get(1).getMaDV());
        phieuKhamDao.luuDichVu(pk1.getMaPK(), maDichVuChon);
        List<DichVu> dvDaChon = phieuKhamDao.findDichVu(pk1.getMaPK());
        kiemTra(dvDaChon.size() == 2, "Lưu và đọc dịch vụ chỉ định khi check-in");
        phieuKhamDao.luuDichVu(pk1.getMaPK(), List.of(dsDichVu.get(0).getMaDV()));
        kiemTra(phieuKhamDao.findDichVu(pk1.getMaPK()).size() == 1,
                "Cập nhật lại danh sách dịch vụ chỉ định");

        // ---------- Kết quả khám ----------
        KetQuaKham kq = new KetQuaKham();
        kq.setMaPK(pk1.getMaPK());
        kq.setMaBS(dsBacSi.get(0).getMaBS());
        kq.setTrieuChung("Đau răng hàm dưới");
        kq.setKetQuaLamSang("Sâu răng mức độ trung bình");
        kq.setChanDoan("Sâu ngà");
        kq.setHuongDieuTri("Trám composite");
        kq.setDonThuoc("Paracetamol 500mg");
        kq.setGhiChu("Tái khám sau 1 tuần");
        kq.setNgayTaiKham(LocalDate.now().plusWeeks(1));
        ketQuaKhamDao.luu(kq);
        KetQuaKham docKq = ketQuaKhamDao.findByMaPK(pk1.getMaPK());
        kiemTra(docKq != null && "Sâu ngà".equals(docKq.getChanDoan()), "Lưu và đọc kết quả khám");

        phieuKhamDao.batDauKham(pk1.getMaPK());
        kiemTra(phieuKhamDao.findById(pk1.getMaPK()).dangKham(), "Bắt đầu khám");
        phieuKhamDao.hoanThanh(pk1.getMaPK());
        kiemTra(phieuKhamDao.findById(pk1.getMaPK()).hoanThanh(), "Hoàn thành phiếu khám");
        kiemTra(lichHenDao.findById(maLH).getTrangThai().equals(LichHenDao.HOAN_THANH),
                "Lịch hẹn hoàn thành theo phiếu khám");

        // ---------- Hóa đơn ----------
        List<PhieuKham> chuaLap = hoaDonDao.findPhieuChuaLapHoaDon();
        kiemTra(chuaLap.stream().anyMatch(p -> p.getMaPK() == pk1.getMaPK()),
                "Phiếu khám xuất hiện trong danh sách chưa lập hóa đơn");

        List<ChiTietHoaDon> chiTiet = new ArrayList<>();
        chiTiet.add(new ChiTietHoaDon(dsDichVu.get(0).getMaDV(), dsDichVu.get(0).getTenDichVu(),
                1, dsDichVu.get(0).getDonGia()));
        chiTiet.add(new ChiTietHoaDon(dsDichVu.get(1).getMaDV(), dsDichVu.get(1).getTenDichVu(),
                2, dsDichVu.get(1).getDonGia()));
        BigDecimal tamTinh = dsDichVu.get(0).getDonGia()
                .add(dsDichVu.get(1).getDonGia().multiply(BigDecimal.valueOf(2)));
        BigDecimal giam = new BigDecimal("50000");
        int maHD = hoaDonDao.taoHoaDon(pk1.getMaPK(), tk.getMaTK(), chiTiet,
                maCoDinh.getMaGiam(), giam, "Hóa đơn smoke test");
        kiemTra(maHD > 0, "Lập hóa đơn");

        HoaDon hd = hoaDonDao.findDayDu(maHD);
        kiemTra(hd != null && hd.getTongTien().compareTo(tamTinh.subtract(giam)) == 0,
                "Tổng tiền hóa đơn = dịch vụ - giảm giá");
        kiemTra(hd.getMaGiam() != null && maCoDinh.getMa().equals(hd.getMaGiamCode()),
                "Hóa đơn lưu đúng mã giảm giá đã chọn");
        kiemTra(hd.getChiTiet().size() == 2, "Đọc chi tiết hóa đơn");
        kiemTraNem(() -> hoaDonDao.taoHoaDon(pk1.getMaPK(), tk.getMaTK(), chiTiet, BigDecimal.ZERO, null),
                "Chặn lập trùng hóa đơn cho một phiếu khám");

        List<ChiTietHoaDon> chiTietMoi = new ArrayList<>();
        chiTietMoi.add(new ChiTietHoaDon(dsDichVu.get(2).getMaDV(), dsDichVu.get(2).getTenDichVu(),
                1, dsDichVu.get(2).getDonGia()));
        hoaDonDao.capNhatHoaDon(maHD, chiTietMoi, BigDecimal.ZERO, "Sửa hóa đơn smoke test");
        HoaDon sauSua = hoaDonDao.findDayDu(maHD);
        kiemTra(sauSua.getChiTiet().size() == 1
                && sauSua.getTongTien().compareTo(dsDichVu.get(2).getDonGia()) == 0,
                "Sửa hóa đơn chưa thanh toán");

        // ---------- Thu tiền ----------
        BigDecimal tong = sauSua.getTongTien();
        BigDecimal khachDua = tong.add(new BigDecimal("100000"));
        hoaDonDao.thanhToan(maHD, HoaDon.TIEN_MAT, khachDua, "Khách đưa dư");
        HoaDon daThu = hoaDonDao.findDayDu(maHD);
        kiemTra(daThu.daThanhToan(), "Xác nhận thanh toán");
        kiemTra(daThu.getTienThua().compareTo(new BigDecimal("100000")) == 0, "Tính tiền thừa");
        kiemTraNem(() -> hoaDonDao.thanhToan(maHD, HoaDon.TIEN_MAT, tong, null),
                "Chặn thu tiền lần hai");
        kiemTraNem(() -> hoaDonDao.capNhatHoaDon(maHD, chiTietMoi, BigDecimal.ZERO, null),
                "Chặn sửa hóa đơn đã thanh toán");
        kiemTraNem(() -> hoaDonDao.thanhToan(maHD, HoaDon.TIEN_MAT, tong.subtract(BigDecimal.ONE), null),
                "Chặn thu thiếu tiền (hóa đơn đã thanh toán)");

        // ---------- Lọc hóa đơn ----------
        kiemTra(!hoaDonDao.tim("Smoke", null, null, null).isEmpty()
                        || !hoaDonDao.tim("HD" + maHD, null, null, null).isEmpty(),
                "Tìm hóa đơn theo từ khóa");
        kiemTra(!hoaDonDao.tim(null, HoaDon.DA_THANH_TOAN, LocalDate.now(), LocalDate.now()).isEmpty(),
                "Lọc hóa đơn theo trạng thái và ngày");

        // ---------- Xuất hóa đơn ----------
        String docx = XuatHoaDon.taoThanDocx(daThu);
        kiemTra(docx.contains("HÓA ĐƠN THANH TOÁN") && docx.contains("Bệnh nhân Smoke Test A"),
                "Tạo nội dung Word hóa đơn");
        kiemTra(docx.contains("Tiền khách đưa") && docx.contains("Tiền thừa trả khách"),
                "Word hóa đơn có tiền khách đưa / tiền thừa");
        kiemTra(XuatHoaDon.taoThanDocx(hd).contains(maCoDinh.getMa()), "Word hóa đơn hiển thị mã giảm giá");
        kiemTra(XuatHoaDon.docSoThanhChu(new BigDecimal("1250000"))
                .equals("Một triệu hai trăm năm mươi nghìn đồng chẵn."), "Đọc số tiền thành chữ");

        // ---------- Hủy hóa đơn ----------
        BenhNhan bn2 = taoBenhNhan("999999999992", "0909999992", "Bệnh nhân Smoke Test B");
        int maBN2 = benhNhanDao.them(bn2);
        int maLH2 = lichHenDao.tao(maBN2, dsBacSi.get(2).getMaBS(),
                LocalDate.now().plusDays(4).atTime(14, 0), "Lịch hủy hóa đơn", tk.getMaTK());
        lichHenDao.xacNhan(maLH2);
        PhieuKham pk2 = phieuKhamDao.capSoThuTu(maBN2, dsPhong.get(2).getMaPhong(), maLH2);
        List<ChiTietHoaDon> ct2 = new ArrayList<>();
        ct2.add(new ChiTietHoaDon(dsDichVu.get(3).getMaDV(), dsDichVu.get(3).getTenDichVu(),
                1, dsDichVu.get(3).getDonGia()));
        int maHD2 = hoaDonDao.taoHoaDon(pk2.getMaPK(), tk.getMaTK(), ct2, BigDecimal.ZERO, null);
        hoaDonDao.huy(maHD2, "Bệnh nhân đổi kế hoạch");
        HoaDon hdHuy = hoaDonDao.findDayDu(maHD2);
        kiemTra(hdHuy.daHuy() && "Bệnh nhân đổi kế hoạch".equals(hdHuy.getLyDoHuy()), "Hủy hóa đơn kèm lý do");
        kiemTra(hoaDonDao.findByMaPK(pk2.getMaPK()) == null, "Hóa đơn đã hủy không còn hiệu lực");

        // ---------- Hủy lịch hẹn ----------
        int maLH3 = lichHenDao.tao(maBN2, dsBacSi.get(1).getMaBS(),
                LocalDate.now().plusDays(5).atTime(15, 30), "Lịch cần hủy", tk.getMaTK());
        lichHenDao.huy(maLH3, "Bận việc");
        LichHen huy = lichHenDao.findById(maLH3);
        kiemTra(huy.getTrangThai().equals(LichHenDao.DA_HUY), "Hủy lịch hẹn");

        System.out.println("\nĐã tạo bệnh nhân test: BN" + maBN1 + ", BN" + maBN2);
        System.out.println("Dữ liệu test sẽ được tự động dọn khi kết thúc.");
    }

    private static BenhNhan taoBenhNhan(String cccd, String sdt, String ten) {
        BenhNhan bn = new BenhNhan();
        bn.setHoTen(ten);
        bn.setNgaySinh(LocalDate.of(1992, 5, 20));
        bn.setGioiTinh("Nam");
        bn.setSoDienThoai(sdt);
        bn.setCccd(cccd);
        bn.setDiaChi("Địa chỉ smoke test");
        bn.setEmail("smoke" + cccd.substring(cccd.length() - 3) + "@example.com");
        bn.setDiUng("Không");
        bn.setTienSuBenh("Khỏe mạnh");
        return bn;
    }

    private static void kiemTra(boolean dieuKien, String moTa) {
        soKiemTra++;
        if (!dieuKien) {
            throw new AssertionError("Không đạt: " + moTa);
        }
        System.out.printf("[%02d] OK  %s%n", soKiemTra, moTa);
    }

    private static void kiemTraNem(ThaoTac thaoTac, String moTa) {
        soKiemTra++;
        try {
            thaoTac.chay();
        } catch (IllegalArgumentException e) {
            System.out.printf("[%02d] OK  %s (%s)%n", soKiemTra, moTa, e.getMessage());
            return;
        } catch (Exception e) {
            throw new AssertionError("Không đạt: " + moTa + " - ném sai loại lỗi: " + e, e);
        }
        throw new AssertionError("Không đạt: " + moTa + " - đáng lẽ phải bị từ chối");
    }

    @FunctionalInterface
    private interface ThaoTac {
        void chay() throws Exception;
    }
}
