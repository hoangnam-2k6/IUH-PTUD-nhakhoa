/* ============================================================
   SQL Server - Du lieu mau cho 5 bang:
       dbo.NhanVien, dbo.BenhNhan, dbo.DichVu,
       dbo.HoaDon,   dbo.LichHen

   Phu thuoc vao schema trong file:
       MediDent_TaoDatabase_VaBang.sql

   Luu y:
   - Gia tri enum (gioitinh, trangthai cua NhanVien/BenhNhan/DichVu)
     duoc luu CHU HOA, khop ten enum Java, vi Enum.valueOf()
     phan biet hoa/thuong. Neu truoc do da chay ban du lieu mau
     cu (chu thuong), buoc 0 se doi sang chu hoa.
   - LichHen va HoaDon co ma tu tang, nen chi chen du lieu mau
     khi bang dang trong.
   - Ngay cua LichHen/HoaDon tinh theo ngay chay script
     (lich hen sap toi + lich da kham truoc do).
   - HoaDon.maTT, maPK la ma giu cho cho 2 bang ThanhToan,
     PhieuKham (chua co trong schema).

   Script co the chay lai nhieu lan.
   File nen duoc luu UTF-8 (co BOM) de SSMS doc dung tieng Viet.
   ============================================================ */
USE MediDent;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* --------------------------------------------------------
       0. DOI GIA TRI ENUM CU (CHU THUONG) SANG CHU HOA
       -------------------------------------------------------- */
    UPDATE dbo.NhanVien SET gioitinh = UPPER(gioitinh), trangthai = UPPER(trangthai);
    UPDATE dbo.BenhNhan SET gioitinh = UPPER(gioitinh), trangthai = UPPER(trangthai);
    UPDATE dbo.DichVu   SET trangthai = UPPER(trangthai);

    /* --------------------------------------------------------
       1. NHANVIEN
       Enum: gioitinh (NAM, NU, KHAC);
             trangthai (DANGLAMVIEC, TAMNGHI, NGHIVIEC)
       -------------------------------------------------------- */
    INSERT INTO dbo.NhanVien
        (maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
    SELECT s.maNV, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
           s.email, s.diachi, s.chucvu, s.trangthai, s.maTK
    FROM (VALUES
        (N'NV001', N'Nguyễn Văn An',  CONVERT(date, '1990-03-15', 23), N'NAM', N'0901000001', N'an.nv@example.com',    N'Quận 1, TP. Hồ Chí Minh',    N'Bác sĩ',          N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV002', N'Trần Thị Bình',  CONVERT(date, '1994-07-22', 23), N'NU',  N'0901000002', N'binh.nv@example.com',  N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Lễ tân',        N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV003', N'Lê Quốc Cường',  CONVERT(date, '1988-11-09', 23), N'NAM', N'0901000003', N'cuong.nv@example.com', N'Gò Vấp, TP. Hồ Chí Minh',   N'Bác sĩ',          N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV004', N'Phạm Ngọc Dung', CONVERT(date, '1996-01-30', 23), N'NU',  N'0901000004', N'dung.nv@example.com',  N'Bình Thạnh, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'TAMNGHI',     CAST(NULL AS nvarchar(20))),
        (N'NV005', N'Võ Minh Khang',  CONVERT(date, '1992-05-18', 23), N'NAM', N'0901000005', N'khang.nv@example.com', N'Quận 7, TP. Hồ Chí Minh',   N'Kỹ thuật viên',   N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV006', N'Đặng Thu Hà',    CONVERT(date, '1991-09-05', 23), N'NU',  N'0901000006', N'ha.nv@example.com',    N'Quận 10, TP. Hồ Chí Minh',  N'Quản lý',         N'DANGLAMVIEC', CAST(NULL AS nvarchar(20)))
    ) AS s(maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.NhanVien AS t WHERE t.maNV = s.maNV
    );

    /* --------------------------------------------------------
       2. BENHNHAN
       Enum: gioitinh (NAM, NU, KHAC);
             trangthai (MOI, DANGDIEUTRI, HOANTAT)
       -------------------------------------------------------- */
    INSERT INTO dbo.BenhNhan
        (maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
    SELECT s.maBN, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
           s.email, s.diachi, s.tiensubenh, s.trangthai
    FROM (VALUES
        (N'BN001', N'Nguyễn Thị Mai', CONVERT(date, '2001-02-12', 23), N'NU',  N'0912000001', N'mai.bn@example.com',  N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Chưa ghi nhận',              N'DANGDIEUTRI'),
        (N'BN002', N'Trần Văn Nam',   CONVERT(date, '1985-09-25', 23), N'NAM', N'0912000002', N'nam.bn@example.com',  N'Quận 3, TP. Hồ Chí Minh',      N'Tiền sử viêm lợi',          N'DANGDIEUTRI'),
        (N'BN003', N'Lê Hoàng Yến',   CONVERT(date, '1998-04-07', 23), N'NU',  N'0912000003', N'yen.bn@example.com',  N'Gò Vấp, TP. Hồ Chí Minh',      N'Dị ứng thuốc (cần xác nhận)', N'MOI'),
        (N'BN004', N'Phạm Gia Bảo',   CONVERT(date, '2010-12-19', 23), N'NAM', N'0912000004', N'bao.bn@example.com',  N'Bình Thạnh, TP. Hồ Chí Minh',  N'Chưa ghi nhận',              N'DANGDIEUTRI'),
        (N'BN005', N'Võ Thanh Trúc',  CONVERT(date, '1979-06-03', 23), N'NU',  N'0912000005', N'truc.bn@example.com', N'Quận 7, TP. Hồ Chí Minh',      N'Tiền sử cao huyết áp',      N'HOANTAT'),
        (N'BN006', N'Hoàng Đức Minh', CONVERT(date, '1995-08-14', 23), N'NAM', N'0912000006', N'minh.bn@example.com', N'Quận 5, TP. Hồ Chí Minh',      N'Chưa ghi nhận',              N'MOI')
    ) AS s(maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.BenhNhan AS t WHERE t.maBN = s.maBN
    );

    /* --------------------------------------------------------
       3. DICHVU
       Enum: trangthai (DANGAPDUNG, NGUNGAPDUNG)
       -------------------------------------------------------- */
    INSERT INTO dbo.DichVu (maDV, tenDV, mota, dongia, trangthai)
    SELECT s.maDV, s.tenDV, s.mota, s.dongia, s.trangthai
    FROM (VALUES
        (N'DV001', N'Khám răng tổng quát',        N'Kiểm tra tình trạng răng miệng tổng thể',          CAST(100000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV002', N'Lấy cao răng',               N'Làm sạch cao răng và mảng bám',                    CAST(250000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV003', N'Trám răng',                  N'Trám phục hồi răng sau khi làm sạch',              CAST(350000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV004', N'Tẩy trắng răng',             N'Dịch vụ tẩy trắng răng thẩm mỹ',                   CAST(1800000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV005', N'Nhổ răng',                   N'Nhổ răng theo chỉ định của bác sĩ',                CAST(500000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV006', N'Tư vấn chăm sóc răng miệng', N'Tư vấn vệ sinh và chăm sóc răng miệng tại nhà',   CAST(0.00 AS decimal(18,2)),       N'NGUNGAPDUNG')
    ) AS s(maDV, tenDV, mota, dongia, trangthai)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.DichVu AS t WHERE t.maDV = s.maDV
    );

    /* --------------------------------------------------------
       4. LICHHEN (chi chen khi bang dang trong)
       trangthai: Chờ xác nhận, Đã xác nhận, Đã khám, Đã hủy
       Bac si: NV001, NV003 (chuc vu "Bác sĩ")
       -------------------------------------------------------- */
    IF NOT EXISTS (SELECT 1 FROM dbo.LichHen)
    BEGIN
        INSERT INTO dbo.LichHen
            (maBN, maNV, ngayHen, gioHen, thoiLuongPhut, phongKham, trangthai)
        SELECT s.maBN, s.maNV, DATEADD(DAY, s.soNgay, CAST(GETDATE() AS date)),
               CAST(s.gioHen AS time(0)), s.thoiLuongPhut, s.phongKham, s.trangthai
        FROM (VALUES
            (N'BN001', N'NV001',  1, N'09:00', 60, N'Phòng 01', N'Chờ xác nhận'),
            (N'BN002', N'NV003',  1, N'10:00', 45, N'Phòng 02', N'Đã xác nhận'),
            (N'BN003', N'NV001',  2, N'14:00', 60, N'Phòng 01', N'Chờ xác nhận'),
            (N'BN004', N'NV003',  3, N'08:30', 30, N'Phòng 02', N'Đã xác nhận'),
            (N'BN005', N'NV001', -3, N'09:00', 60, N'Phòng 01', N'Đã khám'),
            (N'BN001', N'NV003', -7, N'15:00', 90, N'Phòng 02', N'Đã khám'),
            (N'BN002', N'NV001', -2, N'10:30', 60, N'Phòng 01', N'Đã hủy')
        ) AS s(maBN, maNV, soNgay, gioHen, thoiLuongPhut, phongKham, trangthai);
    END;

    /* --------------------------------------------------------
       5. HOADON (chi chen khi bang dang trong)
       trangthai: Chờ thanh toán, Đã thanh toán, Đã hủy
       maTT, maPK la ma giu cho (chua co bang ThanhToan, PhieuKham)
       -------------------------------------------------------- */
    IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon)
    BEGIN
        INSERT INTO dbo.HoaDon (maTT, maPK, ngayLap, tongTien, trangthai)
        SELECT s.maTT, s.maPK, CAST(DATEADD(DAY, s.soNgay, SYSDATETIME()) AS datetime2(0)),
               s.tongTien, s.trangthai
        FROM (VALUES
            (1, 1,  -3, CAST(250000.00 AS decimal(18,2)),  N'Đã thanh toán'),
            (2, 2,  -7, CAST(1800000.00 AS decimal(18,2)), N'Đã thanh toán'),
            (3, 3,  -2, CAST(350000.00 AS decimal(18,2)),  N'Chờ thanh toán'),
            (4, 4,  -1, CAST(500000.00 AS decimal(18,2)),  N'Chờ thanh toán'),
            (5, 5, -10, CAST(100000.00 AS decimal(18,2)),  N'Đã hủy')
        ) AS s(maTT, maPK, soNgay, tongTien, trangthai);
    END;



/* ============================================================
   1B. BO SUNG NHANVIEN (NV007 - NV030)
   Du lieu bo sung khong trung maNV, co the chay lai an toan.
   ============================================================ */
INSERT INTO dbo.NhanVien
    (maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
SELECT s.maNV, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
       s.email, s.diachi, s.chucvu, s.trangthai, s.maTK
FROM (VALUES
        (N'NV007', N'Ngô Hoàng Phúc', CONVERT(date, '1987-02-14', 23), N'NAM', N'0901000007', N'nv007@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV008', N'Đỗ Thị Lan Anh', CONVERT(date, '1993-06-21', 23), N'NU', N'0901000008', N'nv008@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV009', N'Bùi Minh Nhật', CONVERT(date, '1989-10-03', 23), N'NAM', N'0901000009', N'nv009@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV010', N'Dương Ngọc Ánh', CONVERT(date, '1995-12-11', 23), N'NU', N'0901000010', N'nv010@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV011', N'Hồ Thanh Tùng', CONVERT(date, '1990-04-27', 23), N'NAM', N'0901000011', N'nv011@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV012', N'Đinh Thu Trang', CONVERT(date, '1992-08-16', 23), N'NU', N'0901000012', N'nv012@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV013', N'Lý Quốc Bảo', CONVERT(date, '1986-01-09', 23), N'NAM', N'0901000013', N'nv013@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV014', N'Vũ Khánh Linh', CONVERT(date, '1997-03-25', 23), N'NU', N'0901000014', N'nv014@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Lễ tân', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV015', N'Mai Hoài Nam', CONVERT(date, '1998-07-13', 23), N'NAM', N'0901000015', N'nv015@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Lễ tân', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV016', N'Phan Thùy Dương', CONVERT(date, '1996-11-20', 23), N'NU', N'0901000016', N'nv016@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Lễ tân', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV017', N'Trịnh Gia Hân', CONVERT(date, '1999-05-08', 23), N'NU', N'0901000017', N'nv017@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV018', N'Cao Đức Anh', CONVERT(date, '1994-09-17', 23), N'NAM', N'0901000018', N'nv018@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV019', N'Tạ Minh Châu', CONVERT(date, '1998-02-28', 23), N'NU', N'0901000019', N'nv019@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV020', N'Hà Quốc Việt', CONVERT(date, '1991-06-06', 23), N'NAM', N'0901000020', N'nv020@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Kỹ thuật viên', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV021', N'Nguyễn Thùy My', CONVERT(date, '1995-10-22', 23), N'NU', N'0901000021', N'nv021@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Kỹ thuật viên', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV022', N'Lâm Anh Tuấn', CONVERT(date, '1984-12-01', 23), N'NAM', N'0901000022', N'nv022@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Quản lý', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV023', N'Đoàn Ngọc Huyền', CONVERT(date, '1990-03-19', 23), N'NU', N'0901000023', N'nv023@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Kế toán', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV024', N'Huỳnh Minh Đức', CONVERT(date, '1993-07-29', 23), N'NAM', N'0901000024', N'nv024@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Kế toán', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV025', N'Châu Mỹ Linh', CONVERT(date, '1997-11-14', 23), N'NU', N'0901000025', N'nv025@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Lễ tân', N'TAMNGHI', CAST(NULL AS nvarchar(20))),
        (N'NV026', N'Vương Nhật Long', CONVERT(date, '1988-05-31', 23), N'NAM', N'0901000026', N'nv026@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV027', N'Phùng Bảo Ngọc', CONVERT(date, '1996-01-18', 23), N'NU', N'0901000027', N'nv027@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV028', N'Tôn Gia Bảo', CONVERT(date, '1992-04-12', 23), N'NAM', N'0901000028', N'nv028@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Kỹ thuật viên', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV029', N'Đặng Phương Thảo', CONVERT(date, '1994-08-24', 23), N'NU', N'0901000029', N'nv029@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'NGHIVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV030', N'Trần Đức Thịnh', CONVERT(date, '1985-10-10', 23), N'NAM', N'0901000030', N'nv030@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Bác sĩ', N'DANGLAMVIEC', CAST(NULL AS nvarchar(20)))
) AS s(maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
WHERE NOT EXISTS (SELECT 1 FROM dbo.NhanVien t WHERE t.maNV = s.maNV);

/* ============================================================
   2B. BO SUNG BENHNHAN (BN007 - BN086)
   80 benh nhan bo sung, khong trung maBN.
   ============================================================ */
INSERT INTO dbo.BenhNhan
    (maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
SELECT s.maBN, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
       s.email, s.diachi, s.tiensubenh, s.trangthai
FROM (VALUES
        (N'BN007', N'Nguyễn Văn An', CONVERT(date, '2007-12-24', 23), N'NAM', N'0912000007', N'bn007@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'DANGDIEUTRI'),
        (N'BN008', N'Trần Văn Hiếu', CONVERT(date, '1961-05-08', 23), N'NAM', N'0912000008', N'bn008@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'HOANTAT'),
        (N'BN009', N'Lê Văn Nam', CONVERT(date, '1968-10-19', 23), N'NAM', N'0912000009', N'bn009@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'MOI'),
        (N'BN010', N'Phạm Văn Bảo', CONVERT(date, '1975-03-03', 23), N'NAM', N'0912000010', N'bn010@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'DANGDIEUTRI'),
        (N'BN011', N'Hoàng Văn Hoàng', CONVERT(date, '1982-08-14', 23), N'NAM', N'0912000011', N'bn011@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'HOANTAT'),
        (N'BN012', N'Võ Văn Nghĩa', CONVERT(date, '1989-01-25', 23), N'NAM', N'0912000012', N'bn012@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'MOI'),
        (N'BN013', N'Đặng Văn Cường', CONVERT(date, '1996-06-09', 23), N'NAM', N'0912000013', N'bn013@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'DANGDIEUTRI'),
        (N'BN014', N'Bùi Văn Hùng', CONVERT(date, '2003-11-20', 23), N'NAM', N'0912000014', N'bn014@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'HOANTAT'),
        (N'BN015', N'Đỗ Văn Phong', CONVERT(date, '2010-04-04', 23), N'NAM', N'0912000015', N'bn015@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'MOI'),
        (N'BN016', N'Ngô Văn Dũng', CONVERT(date, '1964-09-15', 23), N'NAM', N'0912000016', N'bn016@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'DANGDIEUTRI'),
        (N'BN017', N'Nguyễn Minh Khôi', CONVERT(date, '1971-02-26', 23), N'NAM', N'0912000017', N'bn017@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'HOANTAT'),
        (N'BN018', N'Trần Minh Quân', CONVERT(date, '1978-07-10', 23), N'NAM', N'0912000018', N'bn018@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'MOI'),
        (N'BN019', N'Lê Minh Đức', CONVERT(date, '1985-12-21', 23), N'NAM', N'0912000019', N'bn019@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'DANGDIEUTRI'),
        (N'BN020', N'Phạm Minh Long', CONVERT(date, '1992-05-05', 23), N'NAM', N'0912000020', N'bn020@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'HOANTAT'),
        (N'BN021', N'Hoàng Minh Tài', CONVERT(date, '1999-10-16', 23), N'NAM', N'0912000021', N'bn021@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'MOI'),
        (N'BN022', N'Võ Minh Hải', CONVERT(date, '2006-03-27', 23), N'NAM', N'0912000022', N'bn022@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'DANGDIEUTRI'),
        (N'BN023', N'Đặng Minh Minh', CONVERT(date, '1960-08-11', 23), N'NAM', N'0912000023', N'bn023@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'HOANTAT'),
        (N'BN024', N'Bùi Minh An', CONVERT(date, '1967-01-22', 23), N'NAM', N'0912000024', N'bn024@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'MOI'),
        (N'BN025', N'Đỗ Minh Hiếu', CONVERT(date, '1974-06-06', 23), N'NAM', N'0912000025', N'bn025@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'DANGDIEUTRI'),
        (N'BN026', N'Ngô Minh Nam', CONVERT(date, '1981-11-17', 23), N'NAM', N'0912000026', N'bn026@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'HOANTAT'),
        (N'BN027', N'Nguyễn Quốc Cường', CONVERT(date, '1988-04-01', 23), N'NAM', N'0912000027', N'bn027@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'MOI'),
        (N'BN028', N'Trần Quốc Hùng', CONVERT(date, '1995-09-12', 23), N'NAM', N'0912000028', N'bn028@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'DANGDIEUTRI'),
        (N'BN029', N'Lê Quốc Phong', CONVERT(date, '2002-02-23', 23), N'NAM', N'0912000029', N'bn029@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'HOANTAT'),
        (N'BN030', N'Phạm Quốc Dũng', CONVERT(date, '2009-07-07', 23), N'NAM', N'0912000030', N'bn030@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'MOI'),
        (N'BN031', N'Hoàng Quốc Khang', CONVERT(date, '1963-12-18', 23), N'NAM', N'0912000031', N'bn031@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'DANGDIEUTRI'),
        (N'BN032', N'Võ Quốc Phúc', CONVERT(date, '1970-05-02', 23), N'NAM', N'0912000032', N'bn032@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'HOANTAT'),
        (N'BN033', N'Đặng Quốc Đạt', CONVERT(date, '1977-10-13', 23), N'NAM', N'0912000033', N'bn033@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'MOI'),
        (N'BN034', N'Bùi Quốc Khôi', CONVERT(date, '1984-03-24', 23), N'NAM', N'0912000034', N'bn034@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'DANGDIEUTRI'),
        (N'BN035', N'Đỗ Quốc Quân', CONVERT(date, '1991-08-08', 23), N'NAM', N'0912000035', N'bn035@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'HOANTAT'),
        (N'BN036', N'Ngô Quốc Đức', CONVERT(date, '1998-01-19', 23), N'NAM', N'0912000036', N'bn036@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'MOI'),
        (N'BN037', N'Nguyễn Đức Minh', CONVERT(date, '2005-06-03', 23), N'NAM', N'0912000037', N'bn037@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'DANGDIEUTRI'),
        (N'BN038', N'Trần Đức An', CONVERT(date, '1959-11-14', 23), N'NAM', N'0912000038', N'bn038@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'HOANTAT'),
        (N'BN039', N'Lê Đức Hiếu', CONVERT(date, '1966-04-25', 23), N'NAM', N'0912000039', N'bn039@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'MOI'),
        (N'BN040', N'Phạm Đức Nam', CONVERT(date, '1973-09-09', 23), N'NAM', N'0912000040', N'bn040@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'DANGDIEUTRI'),
        (N'BN041', N'Hoàng Đức Bảo', CONVERT(date, '1980-02-20', 23), N'NAM', N'0912000041', N'bn041@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'HOANTAT'),
        (N'BN042', N'Võ Đức Hoàng', CONVERT(date, '1987-07-04', 23), N'NAM', N'0912000042', N'bn042@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'MOI'),
        (N'BN043', N'Đặng Đức Nghĩa', CONVERT(date, '1994-12-15', 23), N'NAM', N'0912000043', N'bn043@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'DANGDIEUTRI'),
        (N'BN044', N'Bùi Đức Cường', CONVERT(date, '2001-05-26', 23), N'NAM', N'0912000044', N'bn044@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'HOANTAT'),
        (N'BN045', N'Đỗ Đức Hùng', CONVERT(date, '2008-10-10', 23), N'NAM', N'0912000045', N'bn045@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'MOI'),
        (N'BN046', N'Ngô Đức Phong', CONVERT(date, '1962-03-21', 23), N'NAM', N'0912000046', N'bn046@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'DANGDIEUTRI'),
        (N'BN047', N'Lê Thị Anh', CONVERT(date, '1969-08-05', 23), N'NU', N'0912000047', N'bn047@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'HOANTAT'),
        (N'BN048', N'Võ Thị Mai', CONVERT(date, '1976-01-16', 23), N'NU', N'0912000048', N'bn048@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'MOI'),
        (N'BN049', N'Đỗ Thị Thảo', CONVERT(date, '1983-06-27', 23), N'NU', N'0912000049', N'bn049@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'DANGDIEUTRI'),
        (N'BN050', N'Trần Thị Bích', CONVERT(date, '1990-11-11', 23), N'NU', N'0912000050', N'bn050@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'HOANTAT'),
        (N'BN051', N'Hoàng Thị My', CONVERT(date, '1997-04-22', 23), N'NU', N'0912000051', N'bn051@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'MOI'),
        (N'BN052', N'Bùi Thị Thu', CONVERT(date, '2004-09-06', 23), N'NU', N'0912000052', N'bn052@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'DANGDIEUTRI'),
        (N'BN053', N'Nguyễn Thị Châu', CONVERT(date, '1958-02-17', 23), N'NU', N'0912000053', N'bn053@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'HOANTAT'),
        (N'BN054', N'Phạm Thị Ngân', CONVERT(date, '1965-07-01', 23), N'NU', N'0912000054', N'bn054@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'MOI'),
        (N'BN055', N'Đặng Thị Trâm', CONVERT(date, '1972-12-12', 23), N'NU', N'0912000055', N'bn055@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'DANGDIEUTRI'),
        (N'BN056', N'Ngô Thị Diễm', CONVERT(date, '1979-05-23', 23), N'NU', N'0912000056', N'bn056@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'HOANTAT'),
        (N'BN057', N'Lê Ngọc Nhi', CONVERT(date, '1986-10-07', 23), N'NU', N'0912000057', N'bn057@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'MOI'),
        (N'BN058', N'Võ Ngọc Trúc', CONVERT(date, '1993-03-18', 23), N'NU', N'0912000058', N'bn058@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'DANGDIEUTRI'),
        (N'BN059', N'Đỗ Ngọc Hân', CONVERT(date, '2000-08-02', 23), N'NU', N'0912000059', N'bn059@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'HOANTAT'),
        (N'BN060', N'Trần Ngọc Phương', CONVERT(date, '2007-01-13', 23), N'NU', N'0912000060', N'bn060@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'MOI'),
        (N'BN061', N'Hoàng Ngọc Yến', CONVERT(date, '1961-06-24', 23), N'NU', N'0912000061', N'bn061@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'DANGDIEUTRI'),
        (N'BN062', N'Bùi Ngọc Linh', CONVERT(date, '1968-11-08', 23), N'NU', N'0912000062', N'bn062@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'HOANTAT'),
        (N'BN063', N'Nguyễn Ngọc Quỳnh', CONVERT(date, '1975-04-19', 23), N'NU', N'0912000063', N'bn063@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'MOI'),
        (N'BN064', N'Phạm Ngọc Anh', CONVERT(date, '1982-09-03', 23), N'NU', N'0912000064', N'bn064@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'DANGDIEUTRI'),
        (N'BN065', N'Đặng Ngọc Mai', CONVERT(date, '1989-02-14', 23), N'NU', N'0912000065', N'bn065@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'HOANTAT'),
        (N'BN066', N'Ngô Ngọc Thảo', CONVERT(date, '1996-07-25', 23), N'NU', N'0912000066', N'bn066@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'MOI'),
        (N'BN067', N'Lê Thu Châu', CONVERT(date, '2003-12-09', 23), N'NU', N'0912000067', N'bn067@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'DANGDIEUTRI'),
        (N'BN068', N'Võ Thu Ngân', CONVERT(date, '2010-05-20', 23), N'NU', N'0912000068', N'bn068@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'HOANTAT'),
        (N'BN069', N'Đỗ Thu Trâm', CONVERT(date, '1964-10-04', 23), N'NU', N'0912000069', N'bn069@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'MOI'),
        (N'BN070', N'Trần Thu Diễm', CONVERT(date, '1971-03-15', 23), N'NU', N'0912000070', N'bn070@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'DANGDIEUTRI'),
        (N'BN071', N'Hoàng Thu Ngọc', CONVERT(date, '1978-08-26', 23), N'NU', N'0912000071', N'bn071@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'HOANTAT'),
        (N'BN072', N'Bùi Thu Trang', CONVERT(date, '1985-01-10', 23), N'NU', N'0912000072', N'bn072@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'MOI'),
        (N'BN073', N'Nguyễn Thu Hà', CONVERT(date, '1992-06-21', 23), N'NU', N'0912000073', N'bn073@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'DANGDIEUTRI'),
        (N'BN074', N'Phạm Thu Nhi', CONVERT(date, '1999-11-05', 23), N'NU', N'0912000074', N'bn074@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'HOANTAT'),
        (N'BN075', N'Đặng Thu Trúc', CONVERT(date, '2006-04-16', 23), N'NU', N'0912000075', N'bn075@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'MOI'),
        (N'BN076', N'Ngô Thu Hân', CONVERT(date, '1960-09-27', 23), N'NU', N'0912000076', N'bn076@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'DANGDIEUTRI'),
        (N'BN077', N'Lê Mỹ Quỳnh', CONVERT(date, '1967-02-11', 23), N'NU', N'0912000077', N'bn077@medident.example.com', N'Quận 1, TP. Hồ Chí Minh', N'Chưa ghi nhận', N'HOANTAT'),
        (N'BN078', N'Võ Mỹ Anh', CONVERT(date, '1974-07-22', 23), N'NU', N'0912000078', N'bn078@medident.example.com', N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Tiền sử viêm lợi', N'MOI'),
        (N'BN079', N'Đỗ Mỹ Mai', CONVERT(date, '1981-12-06', 23), N'NU', N'0912000079', N'bn079@medident.example.com', N'Gò Vấp, TP. Hồ Chí Minh', N'Đã từng trám răng', N'DANGDIEUTRI'),
        (N'BN080', N'Trần Mỹ Thảo', CONVERT(date, '1988-05-17', 23), N'NU', N'0912000080', N'bn080@medident.example.com', N'Bình Thạnh, TP. Hồ Chí Minh', N'Răng nhạy cảm', N'HOANTAT'),
        (N'BN081', N'Hoàng Mỹ Bích', CONVERT(date, '1995-10-01', 23), N'NU', N'0912000081', N'bn081@medident.example.com', N'Quận 7, TP. Hồ Chí Minh', N'Dị ứng thuốc (cần xác nhận)', N'MOI'),
        (N'BN082', N'Bùi Mỹ My', CONVERT(date, '2002-03-12', 23), N'NU', N'0912000082', N'bn082@medident.example.com', N'Quận 10, TP. Hồ Chí Minh', N'Tiền sử cao huyết áp', N'DANGDIEUTRI'),
        (N'BN083', N'Nguyễn Mỹ Thu', CONVERT(date, '2009-08-23', 23), N'NU', N'0912000083', N'bn083@medident.example.com', N'Quận 3, TP. Hồ Chí Minh', N'Đang điều trị nha chu', N'HOANTAT'),
        (N'BN084', N'Phạm Mỹ Châu', CONVERT(date, '1963-01-07', 23), N'NU', N'0912000084', N'bn084@medident.example.com', N'Tân Bình, TP. Hồ Chí Minh', N'Đã từng nhổ răng khôn', N'MOI'),
        (N'BN085', N'Đặng Mỹ Ngân', CONVERT(date, '1970-06-18', 23), N'NU', N'0912000085', N'bn085@medident.example.com', N'Bình Tân, TP. Hồ Chí Minh', N'Có tiền sử sâu răng', N'DANGDIEUTRI'),
        (N'BN086', N'Ngô Mỹ Trâm', CONVERT(date, '1977-11-02', 23), N'NU', N'0912000086', N'bn086@medident.example.com', N'Phú Nhuận, TP. Hồ Chí Minh', N'Chưa ghi nhận bệnh lý đặc biệt', N'HOANTAT')
) AS s(maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
WHERE NOT EXISTS (SELECT 1 FROM dbo.BenhNhan t WHERE t.maBN = s.maBN);

/* ============================================================
   3B. BO SUNG DICHVU (DV007 - DV026)
   20 dich vu bo sung, gia tri phu hop DECIMAL(18,2).
   ============================================================ */
INSERT INTO dbo.DichVu (maDV, tenDV, mota, dongia, trangthai)
SELECT s.maDV, s.tenDV, s.mota, s.dongia, s.trangthai
FROM (VALUES
        (N'DV007', N'Tư vấn chỉnh nha', N'Tư vấn phương án niềng răng và kế hoạch điều trị', CAST(0.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV008', N'Chụp X-quang răng', N'Chụp phim X-quang phục vụ chẩn đoán', CAST(150000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV009', N'Chụp CT Cone Beam', N'Chụp CT Cone Beam đánh giá cấu trúc xương hàm', CAST(650000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV010', N'Điều trị tủy răng cửa', N'Điều trị nội nha cho răng cửa', CAST(900000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV011', N'Điều trị tủy răng hàm', N'Điều trị nội nha cho răng hàm', CAST(1800000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV012', N'Bọc răng sứ kim loại', N'Phục hình răng bằng mão sứ kim loại', CAST(1800000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV013', N'Bọc răng sứ toàn sứ', N'Phục hình răng bằng mão toàn sứ', CAST(4500000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV014', N'Dán sứ veneer', N'Dán mặt sứ thẩm mỹ cho răng', CAST(5500000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV015', N'Niềng răng mắc cài kim loại', N'Điều trị chỉnh nha bằng mắc cài kim loại', CAST(25000000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV016', N'Niềng răng mắc cài sứ', N'Điều trị chỉnh nha bằng mắc cài sứ', CAST(35000000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV017', N'Niềng răng trong suốt', N'Điều trị chỉnh nha bằng khay trong suốt', CAST(65000000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV018', N'Cạo vôi và đánh bóng', N'Lấy cao răng kết hợp đánh bóng bề mặt răng', CAST(350000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV019', N'Nhổ răng khôn thường', N'Nhổ răng khôn mọc thẳng theo chỉ định', CAST(1200000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV020', N'Nhổ răng khôn phẫu thuật', N'Tiểu phẫu răng khôn mọc lệch hoặc ngầm', CAST(2500000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV021', N'Tẩy trắng răng tại phòng khám', N'Tẩy trắng răng bằng công nghệ tại phòng khám', CAST(2500000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV022', N'Trám răng thẩm mỹ', N'Trám răng bằng vật liệu composite thẩm mỹ', CAST(450000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV023', N'Phục hình răng tháo lắp', N'Làm hàm giả tháo lắp theo chỉ định', CAST(3000000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV024', N'Cấy ghép Implant', N'Cấy ghép trụ Implant thay thế chân răng', CAST(18000000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV025', N'Tái khám sau điều trị', N'Kiểm tra kết quả sau điều trị nha khoa', CAST(100000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV026', N'Vệ sinh răng miệng trẻ em', N'Làm sạch và hướng dẫn chăm sóc răng trẻ em', CAST(200000.00 AS decimal(18,2)), N'NGUNGAPDUNG')
) AS s(maDV, tenDV, mota, dongia, trangthai)
WHERE NOT EXISTS (SELECT 1 FROM dbo.DichVu t WHERE t.maDV = s.maDV);

/* ============================================================
   4B. BO SUNG 180 LICH HEN
   - Phan bo trong qua khu va 60 ngay sap toi.
   - Chi dung maBN/maNV da co trong bang.
   - Khong chen trung theo benh nhan, nhan vien, ngay va gio.
   - Khong phu thuoc vao IDENTITY maLichHen.
   ============================================================ */
;WITH Numbers AS
(
    SELECT TOP (180)
           ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n
    FROM sys.all_objects a
    CROSS JOIN sys.all_objects b
), Doctors AS
(
    SELECT maNV,
           ROW_NUMBER() OVER (ORDER BY maNV) AS rn,
           COUNT(*) OVER () AS total
    FROM dbo.NhanVien
    WHERE chucvu = N'Bác sĩ'
      AND trangthai = N'DANGLAMVIEC'
), GeneratedAppointments AS
(
    SELECT
        CONCAT(N'BN', RIGHT(N'000' + CONVERT(nvarchar(3), ((n.n * 7 + 11) % 86) + 1), 3)) AS maBN,
        d.maNV,
        DATEADD(DAY,
            CASE WHEN n.n % 5 = 0 THEN -((n.n % 30) + 1)
                 ELSE (n.n % 60) + 1 END,
            CAST(GETDATE() AS date)) AS ngayHen,
        CONVERT(time(0), DATEADD(MINUTE, (n.n % 10) * 45,
            CAST('08:00:00' AS datetime2(0)))) AS gioHen,
        CASE n.n % 3 WHEN 0 THEN 30 WHEN 1 THEN 45 ELSE 60 END AS thoiLuongPhut,
        CASE n.n % 3 WHEN 0 THEN N'Phòng 01' WHEN 1 THEN N'Phòng 02' ELSE N'Phòng 03' END AS phongKham,
        CASE
            WHEN (CASE WHEN n.n % 5 = 0 THEN -((n.n % 30) + 1)
                       ELSE (n.n % 60) + 1 END) < 0
                THEN CASE WHEN n.n % 13 = 0 THEN N'Đã hủy' ELSE N'Đã khám' END
            ELSE CASE WHEN n.n % 3 = 0 THEN N'Đã xác nhận' ELSE N'Chờ xác nhận' END
        END AS trangthai
    FROM Numbers n
    CROSS JOIN (SELECT MAX(total) AS totalDoctors FROM Doctors) dc
    JOIN Doctors d
      ON d.rn = ((n.n - 1) % dc.totalDoctors) + 1
)
INSERT INTO dbo.LichHen
    (maBN, maNV, ngayHen, gioHen, thoiLuongPhut, phongKham, trangthai)
SELECT g.maBN, g.maNV, g.ngayHen, g.gioHen,
       g.thoiLuongPhut, g.phongKham, g.trangthai
FROM GeneratedAppointments g
WHERE EXISTS (SELECT 1 FROM dbo.BenhNhan b WHERE b.maBN = g.maBN)
  AND NOT EXISTS
  (
      SELECT 1
      FROM dbo.LichHen t
      WHERE t.maBN = g.maBN
        AND ISNULL(t.maNV, N'') = ISNULL(g.maNV, N'')
        AND t.ngayHen = g.ngayHen
        AND t.gioHen = g.gioHen
  );

/* ============================================================
   5B. BO SUNG 100 HOA DON
   - maTT va maPK la ma gia lap theo ghi chu schema hien tai.
   - Hoa don tao trong 90 ngay gan day.
   - Khong chen lai cap maTT/maPK da ton tai.
   ============================================================ */
;WITH Numbers AS
(
    SELECT TOP (100)
           ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n
    FROM sys.all_objects a
    CROSS JOIN sys.all_objects b
), GeneratedInvoices AS
(
    SELECT
        n + 5 AS maTT,
        n + 5 AS maPK,
        CAST(DATEADD(DAY, -(n % 90), SYSDATETIME()) AS datetime2(0)) AS ngayLap,
        CAST(100000 + ((n * 137500) % 3900000) AS decimal(18,2)) AS tongTien,
        CASE
            WHEN n % 10 = 0 THEN N'Đã hủy'
            WHEN n % 4 = 0 THEN N'Chờ thanh toán'
            ELSE N'Đã thanh toán'
        END AS trangthai
    FROM Numbers
)
INSERT INTO dbo.HoaDon (maTT, maPK, ngayLap, tongTien, trangthai)
SELECT g.maTT, g.maPK, g.ngayLap, g.tongTien, g.trangthai
FROM GeneratedInvoices g
WHERE NOT EXISTS
(
    SELECT 1 FROM dbo.HoaDon t
    WHERE t.maTT = g.maTT AND t.maPK = g.maPK
);


    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/* ------------------------------------------------------------
   6. KIEM TRA DU LIEU SAU KHI CHEN
   ------------------------------------------------------------ */
SELECT * FROM dbo.NhanVien ORDER BY maNV;
SELECT * FROM dbo.BenhNhan ORDER BY maBN;
SELECT * FROM dbo.DichVu   ORDER BY maDV;
SELECT * FROM dbo.LichHen  ORDER BY maLichHen;
SELECT * FROM dbo.HoaDon   ORDER BY maHD;


SELECT N'Tong so nhan vien' AS Bang, COUNT(*) AS SoLuong FROM dbo.NhanVien
UNION ALL SELECT N'Tong so benh nhan', COUNT(*) FROM dbo.BenhNhan
UNION ALL SELECT N'Tong so dich vu', COUNT(*) FROM dbo.DichVu
UNION ALL SELECT N'Tong so lich hen', COUNT(*) FROM dbo.LichHen
UNION ALL SELECT N'Tong so hoa don', COUNT(*) FROM dbo.HoaDon;
GO
