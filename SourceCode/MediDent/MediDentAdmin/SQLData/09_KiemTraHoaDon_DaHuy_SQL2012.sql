-- CHỈ ĐỌC: đối chiếu hóa đơn #1, #30 và các yêu cầu hủy gần đây.
-- KHÔNG UPDATE / DELETE: tránh khôi phục nhầm hóa đơn đã có hoàn tiền.
USE MediDent;
GO

-- Kiểm tra hai hóa đơn người dùng báo có khả năng nhầm khi chọn STT.
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

-- Yêu cầu hủy liên quan những hóa đơn trên: kiểm tra đã gửi/duyệt hay chưa.
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

-- Phát hiện giao dịch liên kết nhiều hóa đơn (nghiệp vụ duyệt từ chối để tránh hủy nhầm).
SELECT maTT AS MaThanhToan, COUNT(*) AS SoHoaDon
FROM dbo.HoaDon
GROUP BY maTT
HAVING COUNT(*) > 1;
GO
