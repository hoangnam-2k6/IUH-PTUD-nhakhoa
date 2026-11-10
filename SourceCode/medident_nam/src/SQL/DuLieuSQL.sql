
/* ==========================================================
   FILE: DuLieuSQL.sql
   DATABASE: MediDent
   NAP DU LIEU MAU CHO 13 BANG
   CHAY SAU MediDent_TaoDatabase.sql

   Du lieu BN901-BN907 la DU LIEU GIA LAP.
   Khong chay tren he thong dang co giao dich that.
   Khong xoa, khong ghi de hoa don / thanh toan cu.
   ========================================================== */

USE MediDent;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    -- ======================================================
    -- 1. DANH MUC PHONG KHAM
    -- ======================================================

    INSERT dbo.PhongKham(tenPhong,trangThai,ghiChu)
    SELECT v.ten,N'Hoạt động',N'Phòng khám nha khoa'
    FROM (VALUES
        (N'Phòng 01'),(N'Phòng 02'),
        (N'Phòng 03'),(N'Phòng 04')
    ) v(ten)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.PhongKham p WHERE p.tenPhong=v.ten
    );

    -- ======================================================
    -- 2. TAI KHOAN MAU BI KHOA
    -- Khong tao tai khoan admin dang nhap mac dinh.
    -- ======================================================

    INSERT dbo.TaiKhoan(maTK,tenDangNhap,matKhauHash,vaiTro,trangThai)
    SELECT v.ma,v.ten,v.hash,v.vaiTro,N'Bị khóa'
    FROM (VALUES
        (N'TK901',N'demo_bacsi_901',N'KHONG_SU_DUNG_DANG_NHAP_MAU',N'Bác sĩ'),
        (N'TK902',N'demo_letan_902',N'KHONG_SU_DUNG_DANG_NHAP_MAU',N'Lễ tân')
    ) v(ma,ten,hash,vaiTro)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.TaiKhoan t WHERE t.maTK=v.ma
    );

    -- ======================================================
    -- 3. NHAN VIEN MAU BAN DA CO
    -- ======================================================

    INSERT dbo.NhanVien
        (maNV,hoten,ngaysinh,gioitinh,sdt,email,diachi,chucvu,trangthai,maTK)
    SELECT v.ma,v.ten,v.ns,v.gt,v.sdt,v.email,v.diachi,v.cv,v.tt,NULL
    FROM (VALUES
        (N'NV001',N'Nguyễn Văn An',CAST('1990-03-15' AS DATE),N'NAM',N'0901000001',N'an.nv@example.com',N'Quận 1, TP. Hồ Chí Minh',N'Bác sĩ',N'DANGLAMVIEC'),
        (N'NV002',N'Trần Thị Bình',CAST('1994-07-22' AS DATE),N'NU',N'0901000002',N'binh.nv@example.com',N'TP. Thủ Đức, TP. Hồ Chí Minh',N'Lễ tân',N'DANGLAMVIEC'),
        (N'NV003',N'Lê Quốc Cường',CAST('1988-11-09' AS DATE),N'NAM',N'0901000003',N'cuong.nv@example.com',N'Gò Vấp, TP. Hồ Chí Minh',N'Bác sĩ',N'DANGLAMVIEC'),
        (N'NV004',N'Phạm Ngọc Dung',CAST('1996-01-30' AS DATE),N'NU',N'0901000004',N'dung.nv@example.com',N'Bình Thạnh, TP. Hồ Chí Minh',N'Trợ lý nha khoa',N'TAMNGHI'),
        (N'NV005',N'Võ Minh Khang',CAST('1992-05-18' AS DATE),N'NAM',N'0901000005',N'khang.nv@example.com',N'Quận 7, TP. Hồ Chí Minh',N'Kỹ thuật viên',N'DANGLAMVIEC'),
        (N'NV006',N'Đặng Thu Hà',CAST('1991-09-05' AS DATE),N'NU',N'0901000006',N'ha.nv@example.com',N'Quận 10, TP. Hồ Chí Minh',N'Quản lý',N'DANGLAMVIEC')
    ) v(ma,ten,ns,gt,sdt,email,diachi,cv,tt)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.NhanVien n WHERE n.maNV=v.ma
    );

    -- Nhan vien DEMO cho quy trinh kham va thanh toan.
    INSERT dbo.NhanVien
        (maNV,hoten,gioitinh,sdt,chucvu,trangthai,maTK)
    SELECT v.ma,v.ten,v.gt,v.sdt,v.cv,N'DANGLAMVIEC',v.tk
    FROM (VALUES
        (N'NV901',N'Bác sĩ mẫu số 1',N'NAM',N'0999000901',N'Bác sĩ',N'TK901'),
        (N'NV902',N'Lễ tân mẫu',N'NU',N'0999000902',N'Lễ tân',N'TK902'),
        (N'NV903',N'Bác sĩ mẫu số 2',N'NU',N'0999000903',N'Bác sĩ',CAST(NULL AS NVARCHAR(20)))
    ) v(ma,ten,gt,sdt,cv,tk)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.NhanVien n WHERE n.maNV=v.ma
    );

    -- ======================================================
    -- 4. BENH NHAN MAU BAN DA CO
    -- ======================================================

    INSERT dbo.BenhNhan
        (maBN,hoten,ngaysinh,gioitinh,sdt,email,diachi,tiensubenh,trangthai)
    SELECT v.ma,v.ten,v.ns,v.gt,v.sdt,v.email,v.diachi,v.tienSu,v.tt
    FROM (VALUES
        (N'BN001',N'Nguyễn Thị Mai',CAST('2001-02-12' AS DATE),N'NU',N'0912000001',N'mai.bn@example.com',N'TP. Thủ Đức, TP. Hồ Chí Minh',N'Chưa ghi nhận',N'DANGDIEUTRI'),
        (N'BN002',N'Trần Văn Nam',CAST('1985-09-25' AS DATE),N'NAM',N'0912000002',N'nam.bn@example.com',N'Quận 3, TP. Hồ Chí Minh',N'Tiền sử viêm lợi',N'DANGDIEUTRI'),
        (N'BN003',N'Lê Hoàng Yến',CAST('1998-04-07' AS DATE),N'NU',N'0912000003',N'yen.bn@example.com',N'Gò Vấp, TP. Hồ Chí Minh',N'Dị ứng thuốc (cần xác nhận)',N'MOI'),
        (N'BN004',N'Phạm Gia Bảo',CAST('2010-12-19' AS DATE),N'NAM',N'0912000004',N'bao.bn@example.com',N'Bình Thạnh, TP. Hồ Chí Minh',N'Chưa ghi nhận',N'DANGDIEUTRI'),
        (N'BN005',N'Võ Thanh Trúc',CAST('1979-06-03' AS DATE),N'NU',N'0912000005',N'truc.bn@example.com',N'Quận 7, TP. Hồ Chí Minh',N'Tiền sử cao huyết áp',N'HOANTAT'),
        (N'BN006',N'Hoàng Đức Minh',CAST('1995-08-14' AS DATE),N'NAM',N'0912000006',N'minh.bn@example.com',N'Quận 5, TP. Hồ Chí Minh',N'Chưa ghi nhận',N'MOI')
    ) v(ma,ten,ns,gt,sdt,email,diachi,tienSu,tt)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.BenhNhan b WHERE b.maBN=v.ma
    );

    -- Benh nhan GIA LAP de thu nghiem nghiep vu.
    INSERT dbo.BenhNhan
        (maBN,hoten,ngaysinh,gioitinh,sdt,tiensubenh,trangthai)
    SELECT v.ma,v.ten,CAST('2000-01-15' AS DATE),v.gt,v.sdt,
           N'Dữ liệu giả lập phục vụ kiểm thử',N'DANGDIEUTRI'
    FROM (VALUES
        (N'BN901',N'Bệnh nhân mẫu 901',N'NAM',N'0999100901'),
        (N'BN902',N'Bệnh nhân mẫu 902',N'NU',N'0999100902'),
        (N'BN903',N'Bệnh nhân mẫu 903',N'NAM',N'0999100903'),
        (N'BN904',N'Bệnh nhân mẫu 904',N'NU',N'0999100904'),
        (N'BN905',N'Bệnh nhân mẫu 905',N'NAM',N'0999100905'),
        (N'BN906',N'Bệnh nhân mẫu 906',N'NU',N'0999100906'),
        (N'BN907',N'Bệnh nhân mẫu 907',N'NAM',N'0999100907')
    ) v(ma,ten,gt,sdt)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.BenhNhan b WHERE b.maBN=v.ma
    );

    -- ======================================================
    -- 5. DICH VU NHA KHOA
    -- ======================================================

    INSERT dbo.DichVu(maDV,tenDV,mota,dongia,trangthai)
    SELECT v.ma,v.ten,v.moTa,v.gia,v.tt
    FROM (VALUES
        (N'DV001',N'Khám răng tổng quát',N'Kiểm tra tình trạng răng miệng',CAST(100000 AS DECIMAL(18,2)),N'DANGAPDUNG'),
        (N'DV002',N'Lấy cao răng',N'Làm sạch cao răng',CAST(250000 AS DECIMAL(18,2)),N'DANGAPDUNG'),
        (N'DV003',N'Trám răng',N'Phục hồi răng',CAST(350000 AS DECIMAL(18,2)),N'DANGAPDUNG'),
        (N'DV004',N'Tẩy trắng răng',N'Dịch vụ thẩm mỹ',CAST(1800000 AS DECIMAL(18,2)),N'DANGAPDUNG'),
        (N'DV005',N'Nhổ răng',N'Nhổ răng theo chỉ định',CAST(500000 AS DECIMAL(18,2)),N'DANGAPDUNG'),
        (N'DV006',N'Tư vấn chăm sóc răng miệng',N'Tư vấn chăm sóc tại nhà',CAST(0 AS DECIMAL(18,2)),N'NGUNGAPDUNG')
    ) v(ma,ten,moTa,gia,tt)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.DichVu d WHERE d.maDV=v.ma
    );

    -- Gia dich vu co dinh de kiem tra hoa don mau.
    INSERT dbo.DichVu(maDV,tenDV,mota,dongia,trangthai)
    SELECT v.ma,v.ten,N'Dịch vụ giả lập',v.gia,N'DANGAPDUNG'
    FROM (VALUES
        (N'DV901',N'Khám mẫu',CAST(100000 AS DECIMAL(18,2))),
        (N'DV902',N'Lấy cao răng mẫu',CAST(250000 AS DECIMAL(18,2))),
        (N'DV903',N'Trám răng mẫu',CAST(350000 AS DECIMAL(18,2))),
        (N'DV904',N'Nhổ răng mẫu',CAST(500000 AS DECIMAL(18,2)))
    ) v(ma,ten,gia)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.DichVu d WHERE d.maDV=v.ma
    );

    -- ======================================================
    -- 6. LICH LAM VIEC
    -- ======================================================

    -- Ca cho bac si mau trong ngay mai.
    INSERT dbo.LichLamViec
        (maNV,ngayLamViec,gioBatDau,gioKetThuc,phongKham,trangThai)
    SELECT v.nv,DATEADD(DAY,v.ngay,CAST(GETDATE() AS DATE)),
           CAST(v.batDau AS TIME(0)),CAST(v.ketThuc AS TIME(0)),
           v.phong,N'Hoạt động'
    FROM (VALUES
        (N'NV901',1,N'08:00',N'12:00',N'Phòng 01'),
        (N'NV903',1,N'08:00',N'12:00',N'Phòng 02'),
        (N'NV901',2,N'13:00',N'17:00',N'Phòng 01')
    ) v(nv,ngay,batDau,ketThuc,phong)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.LichLamViec l
        WHERE l.maNV=v.nv
        AND l.ngayLamViec=DATEADD(DAY,v.ngay,CAST(GETDATE() AS DATE))
        AND l.gioBatDau=CAST(v.batDau AS TIME(0))
    );

    -- ======================================================
    -- 7. LICH HEN
    -- BN901-BN905, BN907: da kham.
    -- BN906: lich sap toi.
    -- ======================================================

    INSERT dbo.LichHen
        (maBN,maNV,ngayHen,gioHen,thoiLuongPhut,phongKham,trangThai)
    SELECT v.bn,v.nv,DATEADD(DAY,v.ngay,CAST(GETDATE() AS DATE)),
           CAST(v.gio AS TIME(0)),v.phut,v.phong,v.tt
    FROM (VALUES
        (N'BN901',N'NV901',-3,N'09:00',60,N'Phòng 01',N'Đã khám'),
        (N'BN902',N'NV903',-8,N'09:00',60,N'Phòng 02',N'Đã khám'),
        (N'BN903',N'NV901',-15,N'10:00',60,N'Phòng 01',N'Đã khám'),
        (N'BN904',N'NV903',-45,N'09:30',60,N'Phòng 02',N'Đã khám'),
        (N'BN905',N'NV901',-400,N'10:00',60,N'Phòng 01',N'Đã khám'),
        (N'BN907',N'NV903',-1,N'11:00',60,N'Phòng 02',N'Đã khám'),
        (N'BN906',N'NV901',1,N'09:00',60,N'Phòng 01',N'Chờ xác nhận')
    ) v(bn,nv,ngay,gio,phut,phong,tt)
    WHERE NOT EXISTS(
        SELECT 1 FROM dbo.LichHen l
        WHERE l.maBN=v.bn AND l.maNV=v.nv
        AND l.ngayHen=DATEADD(DAY,v.ngay,CAST(GETDATE() AS DATE))
        AND l.gioHen=CAST(v.gio AS TIME(0))
    );

    -- ======================================================
    -- 8. PHIEU KHAM
    -- ======================================================

    INSERT dbo.PhieuKham
        (maBN,maNV,maLichHen,ngayKham,trangThai,ghiChu)
    SELECT l.maBN,l.maNV,l.maLichHen,
           DATEADD(MINUTE,
               DATEDIFF(MINUTE,CAST('00:00' AS TIME),l.gioHen),
               CAST(l.ngayHen AS DATETIME2(0))),
           N'Hoàn thành',N'Phiếu khám giả lập'
    FROM dbo.LichHen l
    WHERE l.trangThai=N'Đã khám'
      AND l.maBN IN(N'BN901',N'BN902',N'BN903',N'BN904',N'BN905',N'BN907')
      AND NOT EXISTS(
          SELECT 1 FROM dbo.PhieuKham p WHERE p.maLichHen=l.maLichHen
      );

    -- ======================================================
    -- 9. CHI TIET PHIEU KHAM
    -- BN907 co chi tiet nhung chua lap hoa don.
    -- ======================================================

    INSERT dbo.ChiTietPhieuKham(maPK,maDV,soLuong,donGia)
    SELECT p.maPK,d.maDV,1,d.dongia
    FROM dbo.PhieuKham p
    JOIN dbo.DichVu d ON d.maDV=CASE p.maBN
        WHEN N'BN901' THEN N'DV901'
        WHEN N'BN902' THEN N'DV902'
        WHEN N'BN903' THEN N'DV903'
        WHEN N'BN904' THEN N'DV904'
        WHEN N'BN905' THEN N'DV901'
        WHEN N'BN907' THEN N'DV902'
    END
    WHERE p.maBN IN(N'BN901',N'BN902',N'BN903',N'BN904',N'BN905',N'BN907')
      AND p.trangThai=N'Hoàn thành'
      AND NOT EXISTS(
          SELECT 1 FROM dbo.ChiTietPhieuKham c WHERE c.maPK=p.maPK
      );

    -- ======================================================
    -- 10. THANH TOAN
    -- BN901/902/903: thanh cong.
    -- BN904: cho xu ly.
    -- BN905: da huy.
    -- BN907: chua tao thanh toan.
    -- ======================================================

    INSERT dbo.ThanhToan
        (maPK,soTien,phuongThuc,trangThai,ngayThanhToan)
    SELECT p.maPK,SUM(c.thanhTien),
           CASE WHEN p.maBN IN(N'BN902',N'BN905')
                THEN N'Chuyển khoản' ELSE N'Tiền mặt' END,
           CASE WHEN p.maBN=N'BN904' THEN N'Chờ xử lý'
                WHEN p.maBN=N'BN905' THEN N'Đã hủy'
                ELSE N'Thành công' END,
           CASE WHEN p.maBN IN(N'BN904',N'BN905')
                THEN NULL ELSE p.ngayKham END
    FROM dbo.PhieuKham p
    JOIN dbo.LichHen l ON l.maLichHen=p.maLichHen
    JOIN dbo.ChiTietPhieuKham c ON c.maPK=p.maPK
    WHERE l.maBN IN(N'BN901',N'BN902',N'BN903',N'BN904',N'BN905')
      AND NOT EXISTS(
          SELECT 1 FROM dbo.ThanhToan t WHERE t.maPK=p.maPK
      )
    GROUP BY p.maPK,p.maBN,p.ngayKham
    HAVING SUM(c.thanhTien)>0;

    -- ======================================================
    -- 11. HOA DON
    -- Lay maTT tu ban ghi ThanhToan thuc su.
    -- Khong tu gan maTT = 1,2,3...
    -- ======================================================

    INSERT dbo.HoaDon(maTT,maPK,ngayLap,tongTien,trangThai)
    SELECT t.maTT,t.maPK,p.ngayKham,t.soTien,
           CASE WHEN t.trangThai=N'Thành công' THEN N'Đã thanh toán'
                WHEN t.trangThai=N'Đã hủy' THEN N'Đã hủy'
                ELSE N'Chờ thanh toán' END
    FROM dbo.ThanhToan t
    JOIN dbo.PhieuKham p ON p.maPK=t.maPK
    JOIN dbo.LichHen l ON l.maLichHen=p.maLichHen
    WHERE l.maBN IN(N'BN901',N'BN902',N'BN903',N'BN904',N'BN905')
      AND NOT EXISTS(
          SELECT 1 FROM dbo.HoaDon h WHERE h.maPK=t.maPK
      );

    -- ======================================================
    -- 12. YEU CAU HUY PHIEU THU
    -- BN904: cho duyet.
    -- BN903: tu choi (vi da thanh toan).
    -- BN905: da duyet (giao dich da huy).
    -- ======================================================

    INSERT dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai,ngayYeuCau)
    SELECT t.maTT,
           CASE p.maBN
               WHEN N'BN904' THEN N'Khách yêu cầu hủy trước thanh toán'
               WHEN N'BN903' THEN N'Yêu cầu hủy sau khi đã thanh toán'
               ELSE N'Khách đã hủy giao dịch chưa thực thu'
           END,
           CASE p.maBN
               WHEN N'BN904' THEN N'Chờ duyệt'
               WHEN N'BN903' THEN N'Đã từ chối'
               ELSE N'Đã duyệt'
           END,
           DATEADD(MINUTE,5,p.ngayKham)
    FROM dbo.ThanhToan t
    JOIN dbo.PhieuKham p ON p.maPK=t.maPK
    JOIN dbo.LichHen l ON l.maLichHen=p.maLichHen
    WHERE l.maBN IN(N'BN903',N'BN904',N'BN905')
      AND NOT EXISTS(
          SELECT 1 FROM dbo.YeuCauHuyPhieuThu y WHERE y.maTT=t.maTT
      );

    -- ======================================================
    -- 13. PHIEU DUYET HUY PHIEU THU
    -- ======================================================

    INSERT dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet)
    SELECT y.maYC,DATEADD(MINUTE,10,y.ngayYeuCau),
           CASE y.trangThai
               WHEN N'Đã duyệt' THEN N'Đồng ý hủy giao dịch chưa thu'
               ELSE N'Từ chối vì giao dịch đã thanh toán'
           END
    FROM dbo.YeuCauHuyPhieuThu y
    JOIN dbo.ThanhToan t ON t.maTT=y.maTT
    JOIN dbo.PhieuKham p ON p.maPK=t.maPK
    WHERE p.maBN IN(N'BN903',N'BN905')
      AND y.trangThai IN(N'Đã duyệt',N'Đã từ chối')
      AND NOT EXISTS(
          SELECT 1 FROM dbo.PhieuDuyetHuyPhieuThu d WHERE d.maYC=y.maYC
      );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- ==========================================================
-- 14. KIEM TRA SO LUONG DU LIEU
-- ==========================================================

SELECT N'TaiKhoan' AS TenBang,COUNT(*) AS SoDong FROM dbo.TaiKhoan
UNION ALL SELECT N'NhanVien',COUNT(*) FROM dbo.NhanVien
UNION ALL SELECT N'BenhNhan',COUNT(*) FROM dbo.BenhNhan
UNION ALL SELECT N'DichVu',COUNT(*) FROM dbo.DichVu
UNION ALL SELECT N'PhongKham',COUNT(*) FROM dbo.PhongKham
UNION ALL SELECT N'LichLamViec',COUNT(*) FROM dbo.LichLamViec
UNION ALL SELECT N'LichHen',COUNT(*) FROM dbo.LichHen
UNION ALL SELECT N'PhieuKham',COUNT(*) FROM dbo.PhieuKham
UNION ALL SELECT N'ChiTietPhieuKham',COUNT(*) FROM dbo.ChiTietPhieuKham
UNION ALL SELECT N'ThanhToan',COUNT(*) FROM dbo.ThanhToan
UNION ALL SELECT N'HoaDon',COUNT(*) FROM dbo.HoaDon
UNION ALL SELECT N'YeuCauHuyPhieuThu',COUNT(*) FROM dbo.YeuCauHuyPhieuThu
UNION ALL SELECT N'PhieuDuyetHuyPhieuThu',COUNT(*) FROM dbo.PhieuDuyetHuyPhieuThu;
GO

-- ==========================================================
-- 15. KIEM TRA HOA DON VA THANH TOAN
-- ==========================================================

SELECT h.maHD,h.maPK,h.maTT,h.tongTien,
       t.phuongThuc,t.trangThai AS TrangThaiThanhToan,
       h.trangThai AS TrangThaiHoaDon
FROM dbo.HoaDon h
JOIN dbo.ThanhToan t ON t.maTT=h.maTT AND t.maPK=h.maPK
ORDER BY h.maHD;
GO

-- ==========================================================
-- 16. KIEM TRA DUYET HUY
-- ==========================================================

SELECT y.maYC,y.maTT,y.lyDo,y.trangThai,
       d.maPD,d.ngayPheDuyet,d.lyDoPheDuyet
FROM dbo.YeuCauHuyPhieuThu y
LEFT JOIN dbo.PhieuDuyetHuyPhieuThu d ON d.maYC=y.maYC
ORDER BY y.maYC;
GO

-- ==========================================================
-- 17. KIEM TRA THONG KE DOANH THU
-- ==========================================================

SELECT CONVERT(VARCHAR(7),t.ngayThanhToan,23) AS Thang,
       COUNT(*) AS SoGiaoDich,
       SUM(t.soTien) AS DoanhThu
FROM dbo.ThanhToan t
JOIN dbo.HoaDon h ON h.maTT=t.maTT AND h.maPK=t.maPK
WHERE t.trangThai=N'Thành công'
  AND h.trangThai=N'Đã thanh toán'
GROUP BY CONVERT(VARCHAR(7),t.ngayThanhToan,23)
ORDER BY Thang;
GO

-- ==========================================================
-- 18. KIEM TRA PHIEU KHAM CHUA LAP HOA DON
-- ==========================================================

SELECT p.maPK,p.maBN,p.trangThai,
       COUNT(c.maCT) AS SoDichVu,
       COALESCE(SUM(c.thanhTien),0) AS TongTien
FROM dbo.PhieuKham p
JOIN dbo.ChiTietPhieuKham c ON c.maPK=p.maPK
WHERE p.trangThai IN(N'Hoàn thành',N'Đã khám')
  AND NOT EXISTS(
      SELECT 1 FROM dbo.HoaDon h WHERE h.maPK=p.maPK
  )
GROUP BY p.maPK,p.maBN,p.trangThai
ORDER BY p.maPK;
GO


SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'dbo'
AND TABLE_NAME = 'PhongKham';


-- BANG PHONG KHAM
IF OBJECT_ID(N'dbo.PhongKham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhongKham (
        tenPhong NVARCHAR(50) NOT NULL,
        trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
        ghiChu NVARCHAR(255) NULL,

        CONSTRAINT PK_PhongKham PRIMARY KEY (tenPhong),
        CONSTRAINT CK_PhongKham_TrangThai CHECK (
            trangThai IN (N'Hoạt động', N'Ngừng sử dụng')
        )
    );
END;
GO

-- THEM PHONG KHAM MAU
INSERT INTO dbo.PhongKham(tenPhong, trangThai, ghiChu)
SELECT v.tenPhong, N'Hoạt động', N'Phòng khám nha khoa'
FROM (VALUES
    (N'Phòng 01'),
    (N'Phòng 02'),
    (N'Phòng 03'),
    (N'Phòng 04')
) v(tenPhong)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.PhongKham p
    WHERE p.tenPhong = v.tenPhong
);
GO

-- THEM PHONG DA CO TRONG LICH HEN VA LICH LAM VIEC
INSERT INTO dbo.PhongKham(tenPhong, trangThai)
SELECT DISTINCT x.tenPhong, N'Hoạt động'
FROM (
    SELECT LTRIM(RTRIM(phongKham)) AS tenPhong
    FROM dbo.LichHen
    UNION
    SELECT LTRIM(RTRIM(phongKham))
    FROM dbo.LichLamViec
) x
WHERE x.tenPhong IS NOT NULL
AND x.tenPhong <> N''
AND NOT EXISTS (
    SELECT 1 FROM dbo.PhongKham p
    WHERE p.tenPhong = x.tenPhong
);
GO

-- KHOA NGOAI: LICH LAM VIEC -> PHONG KHAM
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_LLV_Phong'
    AND parent_object_id = OBJECT_ID(N'dbo.LichLamViec')
)
BEGIN
    ALTER TABLE dbo.LichLamViec WITH CHECK
    ADD CONSTRAINT FK_LLV_Phong
    FOREIGN KEY (phongKham)
    REFERENCES dbo.PhongKham(tenPhong);
END;
GO

-- KHOA NGOAI: LICH HEN -> PHONG KHAM
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_LH_Phong'
    AND parent_object_id = OBJECT_ID(N'dbo.LichHen')
)
BEGIN
    ALTER TABLE dbo.LichHen WITH CHECK
    ADD CONSTRAINT FK_LH_Phong
    FOREIGN KEY (phongKham)
    REFERENCES dbo.PhongKham(tenPhong);
END;
GO

SELECT t.name AS TenBang,
       COUNT(fk.object_id) AS SoKhoaNgoai
FROM sys.tables t
LEFT JOIN sys.foreign_keys fk
    ON fk.parent_object_id = t.object_id
WHERE t.schema_id = SCHEMA_ID(N'dbo')
GROUP BY t.name
ORDER BY t.name;