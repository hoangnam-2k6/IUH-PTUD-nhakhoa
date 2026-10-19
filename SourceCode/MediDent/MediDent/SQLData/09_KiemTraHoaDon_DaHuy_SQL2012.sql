-- Truy vấn đối chiếu các hóa đơn và thanh toán đã hủy, không tự thay dữ liệu.

USE MediDent;
GO

SELECT h.maHD AS MaHoaDon,
       h.maTT AS MaThanhToan,
       h.trangThai AS TrangThaiHoaDon,
       t.trangThai AS TrangThaiThanhToan,
       b.maBN AS MaBenhNhan,
       b.hoTen AS TenBenhNhan,
       h.tongTien AS SoTien
FROM dbo.HoaDon h
JOIN dbo.ThanhToan t ON t.maTT = h.maTT
JOIN dbo.PhieuKham pk ON pk.maPK = h.maPK
JOIN dbo.BenhNhan b ON b.maBN = pk.maBN
WHERE h.maHD IN (1, 30)
ORDER BY h.maHD;
GO

SELECT TOP 50 y.maYC AS MaYeuCau,
       h.maHD AS MaHoaDon,
       y.maTT AS MaThanhToan,
       y.ngayYeuCau AS NgayGui,
       y.lyDo AS LyDo,
       y.trangThai AS TrangThaiYeuCau,
       h.trangThai AS TrangThaiHoaDon
FROM dbo.YeuCauHuyPhieuThu y
JOIN dbo.HoaDon h ON h.maTT=y.maTT
WHERE h.maHD IN (1, 30)
ORDER BY y.maYC DESC;
GO

SELECT maTT AS MaThanhToan, COUNT(*) AS SoHoaDon
FROM dbo.HoaDon
GROUP BY maTT
HAVING COUNT(*) > 1;
GO
