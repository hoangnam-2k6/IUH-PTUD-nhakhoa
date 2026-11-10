package Entity;

import java.math.BigDecimal;

/** Thành tiền được SQL Server tính từ số lượng và đơn giá. */
public record ChiTietPhieuKham(int maCT, int maPK, String maDV,
        int soLuong, BigDecimal donGia, BigDecimal thanhTien) { }
