-- Nhập bộ dữ liệu mẫu sử dụng cho thử nghiệm hệ thống.
                                                         

USE MediDent;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @MatKhauHash VARCHAR(100) = '$2a$10$z9XT7sr/xPRfQfa8HdmQBuPw1QSlfUalVsXYNtfXB7FRVxxy5bPHW';
    DECLARE @TaiKhoanMau TABLE (
        tenDangNhap VARCHAR(60), hoTen NVARCHAR(120), email VARCHAR(150),
        vaiTro NVARCHAR(100), trangThai NVARCHAR(20), thangTao INT
    );
    INSERT INTO @TaiKhoanMau VALUES
      ('demo_admin', N'Nguyễn Tấn Dự', 'admin@gmail.com', N'Quản trị viên', N'Hoạt động', 1),
      ('demo_ql1', N'Trần Minh Trang', 'trang@gmail.com', N'Quản lý phòng khám', N'Hoạt động', 2),
      ('demo_ql2', N'Phạm Hoàng Khang', 'khang@gmail.com', N'Quản lý phòng khám', N'Hoạt động', 3),
      ('demo_lt1', N'Lê Thu Hương', 'huong@gmail.com', N'Lễ tân', N'Hoạt động', 7),
      ('demo_lt2', N'Phan Thảo Vy', 'thaovy@gmail.com', N'Lễ tân', N'Hoạt động', 8);

    INSERT INTO dbo.TaiKhoan(tenDangNhap, matKhau, trangThai, ngayTao, maVaiTro)
    SELECT m.tenDangNhap, @MatKhauHash, m.trangThai,
           DATEADD(DAY, 5, DATEADD(MONTH, m.thangTao - 1,
                    DATEFROMPARTS(YEAR(GETDATE()), 1, 1))), v.maVaiTro
    FROM @TaiKhoanMau m
    JOIN dbo.VaiTro v ON v.tenVaiTro = m.vaiTro
    WHERE NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan t WHERE t.tenDangNhap = m.tenDangNhap);

    INSERT INTO dbo.NhanVien(hoTen, email, chucVu, trangThai, maTK)
    SELECT m.hoTen, m.email, m.vaiTro,
           CASE WHEN m.trangThai = N'Hoạt động' THEN N'Đang làm việc' ELSE N'Nghỉ việc' END,
           t.maTK
    FROM @TaiKhoanMau m
    JOIN dbo.TaiKhoan t ON t.tenDangNhap = m.tenDangNhap
    WHERE NOT EXISTS (SELECT 1 FROM dbo.NhanVien n WHERE n.maTK = t.maTK);

    INSERT INTO dbo.QuanTriVien(maNV, capDoQuyen)
    SELECT n.maNV, N'Toàn quyền' FROM dbo.NhanVien n
    JOIN dbo.TaiKhoan t ON t.maTK=n.maTK WHERE t.tenDangNhap='demo_admin'
      AND NOT EXISTS(SELECT 1 FROM dbo.QuanTriVien q WHERE q.maNV=n.maNV);
    INSERT INTO dbo.QuanLy(maNV, phongBan)
    SELECT n.maNV, N'Vận hành' FROM dbo.NhanVien n
    JOIN dbo.TaiKhoan t ON t.maTK=n.maTK WHERE t.tenDangNhap LIKE 'demo_ql%'
      AND NOT EXISTS(SELECT 1 FROM dbo.QuanLy q WHERE q.maNV=n.maNV);
    INSERT INTO dbo.LeTan(maNV, caLamViec)
    SELECT n.maNV, N'Ca sáng' FROM dbo.NhanVien n
    JOIN dbo.TaiKhoan t ON t.maTK=n.maTK WHERE t.tenDangNhap LIKE 'demo_lt%'
      AND NOT EXISTS(SELECT 1 FROM dbo.LeTan q WHERE q.maNV=n.maNV);

    DECLARE @BenhNhanMau TABLE (hoTen NVARCHAR(120), ngaySinh DATE,
                                gioiTinh NVARCHAR(15), sdt VARCHAR(20), tienSu NVARCHAR(200));
    INSERT INTO @BenhNhanMau VALUES
      (N'Nguyễn Minh Khoa','2001-03-12',N'Nam','0001000001',N'Không ghi nhận'),
      (N'Trần Ngọc Hân','1999-11-23',N'Nữ','0001000002',N'Dị ứng thuốc tê (giả lập)'),
      (N'Lê Hoàng Nam','1996-07-14',N'Nam','0001000003',N'Không ghi nhận'),
      (N'Phạm Thu Hà','1988-09-05',N'Nữ','0001000004',N'Viêm lợi'),
      (N'Võ Thanh Long','2004-02-20',N'Nam','0001000005',N'Không ghi nhận'),
      (N'Đặng Bảo Anh','2012-08-06',N'Nữ','0001000006',N'Không ghi nhận'),
      (N'Bùi Gia Huy','2014-12-11',N'Nam','0001000007',N'Không ghi nhận'),
      (N'Ngô Minh Châu','1994-06-16',N'Nữ','0001000008',N'Ê buốt răng'),
      (N'Đỗ Thuỳ Dương','2000-10-09',N'Nữ','0001000009',N'Không ghi nhận'),
      (N'Hoàng Quốc Duy','1985-01-17',N'Nam','0001000010',N'Cao huyết áp (giả lập)'),
      (N'Phan Nhật Minh','2003-04-27',N'Nam','0001000011',N'Không ghi nhận'),
      (N'Mai Ngọc Linh','1992-05-02',N'Nữ','0001000012',N'Không ghi nhận');

    INSERT INTO dbo.BenhNhan(hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, tienSuBenh, trangThai)
    SELECT m.hoTen, m.ngaySinh, m.gioiTinh, m.sdt,
           'benhnhan'+m.sdt+'@gmail.com', N'TP. Hồ Chí Minh', m.tienSu, N'Đang điều trị'
    FROM @BenhNhanMau m
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BenhNhan b WHERE b.sdt=m.sdt);

    INSERT INTO dbo.NguoiGiamHo(maBN, hoTen, sdt, quanHe)
    SELECT b.maBN, N'Người giám hộ (mẫu)', '0002000001', N'Cha/mẹ'
    FROM dbo.BenhNhan b WHERE b.sdt IN('0001000006','0001000007')
    AND NOT EXISTS(SELECT 1 FROM dbo.NguoiGiamHo g WHERE g.maBN=b.maBN);

    DECLARE @DichVuMau TABLE(ten NVARCHAR(150), moTa NVARCHAR(200), gia DECIMAL(18,2));
    INSERT INTO @DichVuMau VALUES
      (N'Khám tổng quát', N'Khám và tư vấn', 150000),
      (N'Cạo vôi răng', N'Làm sạch cao răng', 300000),
      (N'Trám răng', N'Trám răng sâu', 450000),
      (N'Nhổ răng', N'Nhổ răng thông thường', 600000),
      (N'Tẩy trắng răng', N'Tẩy trắng tại phòng khám', 1500000),
      (N'Điều trị tủy', N'Điều trị nội nha', 1200000),
      (N'Chụp X-quang', N'Chụp phim răng', 200000),
      (N'Tư vấn chỉnh nha', N'Tư vấn niềng răng', 250000);
    INSERT INTO dbo.DichVu(tenDV, moTa, donGia, trangThai)
    SELECT d.ten, d.moTa, d.gia, N'Đang áp dụng' FROM @DichVuMau d
    WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVu t WHERE t.tenDV=d.ten);
    INSERT INTO dbo.BangGiaDichVu(maDV,ngayApDung,donGia,trangThai)
    SELECT d.maDV, DATEFROMPARTS(YEAR(GETDATE()),1,1), d.donGia, N'Đang áp dụng'
    FROM dbo.DichVu d JOIN @DichVuMau m ON m.ten=d.tenDV
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BangGiaDichVu b WHERE b.maDV=d.maDV
                    AND b.ngayApDung=DATEFROMPARTS(YEAR(GETDATE()),1,1));

    INSERT INTO dbo.DichVuDieuTri(tenDDT,donGia,trangThai)
    SELECT d.ten,d.gia,N'Đang áp dụng' FROM @DichVuMau d
    WHERE d.ten IN(N'Cạo vôi răng',N'Trám răng',N'Nhổ răng',N'Điều trị tủy')
      AND NOT EXISTS(SELECT 1 FROM dbo.DichVuDieuTri t WHERE t.tenDDT=d.ten);

    DECLARE @NhanSuKham TABLE(hoTen NVARCHAR(120), email VARCHAR(150));
    INSERT INTO @NhanSuKham VALUES
        (N'Nguyễn Hoàng Việt','nvytviet@gmail.com'),
        (N'Đặng Minh Anh','nvytminhanh@gmail.com'),
        (N'Lê Quốc Bảo','nvytbao@gmail.com');
    INSERT INTO dbo.NhanVien(hoTen,email,chucVu,trangThai,maTK)
    SELECT a.hoTen,a.email,N'Bác sĩ',N'Đang làm việc',NULL
    FROM @NhanSuKham a
    WHERE NOT EXISTS (SELECT 1 FROM dbo.NhanVien n
                      WHERE n.hoTen=a.hoTen AND n.chucVu=N'Bác sĩ');

    INSERT INTO dbo.LichLamViec(maNV,thu,ngayLamViec,caLamViec,gioBatDau,gioKetThuc,trangThai)
    SELECT n.maNV, N'Theo lịch', DATEADD(DAY,x.ngay,CAST(GETDATE() AS DATE)),
           CASE WHEN x.ngay%2=0 THEN N'Ca sáng' ELSE N'Ca chiều' END,
           CASE WHEN x.ngay%2=0 THEN CAST('08:00' AS TIME) ELSE CAST('13:30' AS TIME) END,
           CASE WHEN x.ngay%2=0 THEN CAST('12:00' AS TIME) ELSE CAST('17:00' AS TIME) END,
           N'Đã xếp lịch'
    FROM dbo.NhanVien n JOIN @NhanSuKham a ON a.hoTen=n.hoTen
    CROSS JOIN (VALUES(0),(1),(2),(3)) x(ngay)
    WHERE n.chucVu=N'Bác sĩ'
      AND NOT EXISTS(SELECT 1 FROM dbo.LichLamViec l WHERE l.maNV=n.maNV
         AND l.ngayLamViec=DATEADD(DAY,x.ngay,CAST(GETDATE() AS DATE)));

    DECLARE @DSBN TABLE(stt INT IDENTITY(1,1), maBN INT);
    DECLARE @DSBS TABLE(stt INT IDENTITY(1,1), maNV INT);
    INSERT INTO @DSBN(maBN) SELECT b.maBN FROM dbo.BenhNhan b JOIN @BenhNhanMau m ON m.sdt=b.sdt ORDER BY b.sdt;
    INSERT INTO @DSBS(maNV) SELECT n.maNV FROM dbo.NhanVien n
      JOIN @NhanSuKham a ON a.hoTen=n.hoTen
      WHERE n.chucVu=N'Bác sĩ' ORDER BY n.maNV;
    DECLARE @SoBN INT=(SELECT COUNT(*) FROM @DSBN), @SoBS INT=(SELECT COUNT(*) FROM @DSBS);
    IF @SoBN < 12 OR @SoBS < 3 RAISERROR(N'Thieu benh nhan hoac nhan su kham mau.',16,1);

    DECLARE @i INT=1, @bn INT, @bs INT, @hen DATE, @gio TIME, @ngayKham DATETIME2;
    WHILE @i<=24
    BEGIN
        SELECT @bn=maBN FROM @DSBN WHERE stt=((@i-1)%@SoBN)+1;
        SELECT @bs=maNV FROM @DSBS WHERE stt=((@i-1)%@SoBS)+1;
        SET @hen=DATEADD(DAY,(@i*2)%23,DATEFROMPARTS(YEAR(GETDATE()),((@i-1)%10)+1,1));
        SET @gio=CASE WHEN @i%2=0 THEN CAST('09:00' AS TIME) ELSE CAST('14:30' AS TIME) END;
        IF NOT EXISTS(SELECT 1 FROM dbo.LichHen WHERE maBN=@bn AND maNV=@bs
               AND ngayHen=@hen AND gioHen=@gio)
            INSERT INTO dbo.LichHen(maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai,ghiChu)
            VALUES(@bn,@bs,N'Phòng 0'+CAST((@i%3)+1 AS NVARCHAR(2)),@hen,@gio,
                   N'Khám nha khoa định kỳ (dữ liệu mẫu)',
                   CASE WHEN @i%5=0 THEN N'Chờ xác nhận' ELSE N'Đã khám' END,
                   N'MEDIDENT DEMO - lịch hẹn');
        SET @i=@i+1;
    END;

    SET @i=1;
    WHILE @i<=30
    BEGIN
        SELECT @bn=maBN FROM @DSBN WHERE stt=((@i-1)%@SoBN)+1;
        SELECT @bs=maNV FROM @DSBS WHERE stt=((@i-1)%@SoBS)+1;
        SET @ngayKham=DATEADD(HOUR,8+(@i%8),CAST(DATEADD(DAY,(@i*3)%24,
            DATEFROMPARTS(YEAR(GETDATE()),((@i-1)%10)+1,1)) AS DATETIME2));
        IF NOT EXISTS(SELECT 1 FROM dbo.PhieuKham
                      WHERE ghiChu=N'MEDIDENT-DEMO-PK-'+CAST(@i AS NVARCHAR(10)))
            INSERT INTO dbo.PhieuKham(maBN,maNV,ngayKham,trieuChung,chanDoan,
                                      phuongPhapDieuTri,ketQua,ghiChu,trangThai)
            VALUES(@bn,@bs,@ngayKham,N'Đau nhức hoặc ê buốt răng',
                   CASE WHEN @i%3=0 THEN N'Sâu răng' ELSE N'Viêm lợi nhẹ' END,
                   N'Khám và điều trị theo chỉ định',N'Tình trạng ổn định',
                   N'MEDIDENT-DEMO-PK-'+CAST(@i AS NVARCHAR(10)),N'Hoàn tất');
        SET @i=@i+1;
    END;

    INSERT INTO dbo.ChiTietDieuTri(maPK,maDDT,noiDung,soLuong,donGia)
    SELECT p.maPK,d.maDDT,N'Điều trị theo phiếu khám mẫu',1,d.donGia
    FROM dbo.PhieuKham p
    JOIN dbo.DichVuDieuTri d ON d.tenDDT =
       CASE WHEN p.maPK%4=0 THEN N'Điều trị tủy'
            WHEN p.maPK%4=1 THEN N'Cạo vôi răng'
            WHEN p.maPK%4=2 THEN N'Trám răng' ELSE N'Nhổ răng' END
    WHERE p.ghiChu LIKE N'MEDIDENT-DEMO-PK-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietDieuTri c
                     WHERE c.maPK=p.maPK AND c.maDDT=d.maDDT);

    INSERT INTO dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai,maGiaoDich)
    SELECT p.maPK,DATEADD(HOUR,1,p.ngayKham),
           (SELECT SUM(c.thanhTien) FROM dbo.ChiTietDieuTri c WHERE c.maPK=p.maPK),
           CASE WHEN p.maPK%2=0 THEN N'Tiền mặt' ELSE N'Chuyển khoản' END,
           CASE WHEN p.maPK%5=0 THEN N'Chờ xử lý' ELSE N'Thành công' END,
           'MEDIDENT-DEMO-TT-'+CAST(p.maPK AS VARCHAR(15))
    FROM dbo.PhieuKham p
    WHERE p.ghiChu LIKE N'MEDIDENT-DEMO-PK-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.ThanhToan t WHERE t.maGiaoDich=
                     'MEDIDENT-DEMO-TT-'+CAST(p.maPK AS VARCHAR(15)));

    INSERT INTO dbo.HoaDon(maTT,maPK,ngayLap,tongTien,trangThai)
    SELECT t.maTT,t.maPK,t.ngayThanhToan,t.soTien,
           CASE WHEN t.trangThai=N'Thành công' THEN N'Đã thanh toán' ELSE N'Chờ thanh toán' END
    FROM dbo.ThanhToan t WHERE t.maGiaoDich LIKE 'MEDIDENT-DEMO-TT-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maTT=t.maTT);

    INSERT INTO dbo.YeuCauHuyPhieuThu(maTT,ngayYeuCau,lyDo,trangThai)
    SELECT TOP 1 t.maTT,GETDATE(),N'Thanh toán nhập sai (mẫu)',N'Đã từ chối'
    FROM dbo.ThanhToan t WHERE t.maGiaoDich LIKE 'MEDIDENT-DEMO-TT-%'
      AND t.trangThai=N'Chờ xử lý'
      AND NOT EXISTS(SELECT 1 FROM dbo.YeuCauHuyPhieuThu y WHERE y.maTT=t.maTT)
    ORDER BY t.maTT;
    INSERT INTO dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet)
    SELECT y.maYC,GETDATE(),N'Chưa đủ cơ sở hủy (mẫu)'
    FROM dbo.YeuCauHuyPhieuThu y
    WHERE y.lyDo=N'Thanh toán nhập sai (mẫu)'
      AND NOT EXISTS(SELECT 1 FROM dbo.PhieuDuyetHuyPhieuThu p WHERE p.maYC=y.maYC);

    ;WITH DoanhThuThang AS (
        SELECT MIN(h.maHD) AS maHD,
               DATEFROMPARTS(YEAR(t.ngayThanhToan),MONTH(t.ngayThanhToan),1) AS tuNgay,
               EOMONTH(t.ngayThanhToan) AS denNgay,SUM(t.soTien) AS tongDoanhThu
        FROM dbo.ThanhToan t JOIN dbo.HoaDon h ON h.maTT=t.maTT
        WHERE t.maGiaoDich LIKE 'MEDIDENT-DEMO-TT-%' AND t.trangThai=N'Thành công'
        GROUP BY YEAR(t.ngayThanhToan),MONTH(t.ngayThanhToan),EOMONTH(t.ngayThanhToan)
    )
    INSERT INTO dbo.BaoCaoDoanhThu(maHD,tuNgay,denNgay,tongDoanhThu)
    SELECT a.maHD,a.tuNgay,a.denNgay,a.tongDoanhThu
    FROM DoanhThuThang a
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BaoCaoDoanhThu b WHERE b.tuNgay=a.tuNgay);

    ;WITH LuotKhamThang AS (
        SELECT MIN(p.maPK) AS maPK,
               DATEFROMPARTS(YEAR(p.ngayKham),MONTH(p.ngayKham),1) AS tuNgay,
               EOMONTH(p.ngayKham) AS denNgay,COUNT(*) AS soLuotKham
        FROM dbo.PhieuKham p WHERE p.ghiChu LIKE N'MEDIDENT-DEMO-PK-%'
        GROUP BY YEAR(p.ngayKham),MONTH(p.ngayKham),EOMONTH(p.ngayKham)
    )
    INSERT INTO dbo.BaoCaoLuotKham(maPK,tuNgay,denNgay,soLuotKham)
    SELECT a.maPK,a.tuNgay,a.denNgay,a.soLuotKham
    FROM LuotKhamThang a
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BaoCaoLuotKham b WHERE b.tuNgay=a.tuNgay);

    DECLARE @NhatKyMau TABLE(hanhDong NVARCHAR(200),doiTuong NVARCHAR(150),moTa NVARCHAR(1000));
    INSERT INTO @NhatKyMau VALUES
      (N'Cập nhật quyền truy cập', N'VaiTro', N'[DEMO] Phân quyền Quản lý phòng khám'),
      (N'Sao lưu dữ liệu', N'CauHinh', N'[DEMO] Sao lưu hoàn tất'),
      (N'Thêm tài khoản', N'TaiKhoan', N'[DEMO] Tạo nhân viên mẫu'),
      (N'Đăng nhập thất bại', N'TaiKhoan', N'[DEMO] Cảnh báo đăng nhập #1'),
      (N'Đăng nhập thất bại', N'TaiKhoan', N'[DEMO] Cảnh báo đăng nhập #2'),
      (N'Đăng nhập thất bại', N'TaiKhoan', N'[DEMO] Cảnh báo đăng nhập #3');
    INSERT INTO dbo.NhatKyHeThong(thoiGian,hanhDong,doiTuong,moTa)
    SELECT DATEADD(MINUTE, -ROW_NUMBER() OVER (ORDER BY m.moTa)*15, SYSDATETIME()),
           m.hanhDong,m.doiTuong,m.moTa
    FROM @NhatKyMau m WHERE NOT EXISTS
      (SELECT 1 FROM dbo.NhatKyHeThong n WHERE n.moTa=m.moTa);

    COMMIT TRANSACTION;
    PRINT N'HOAN TAT: Da them du lieu mau MediDent (chi cac ban ghi chua ton tai).';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    DECLARE @Loi NVARCHAR(4000)=ERROR_MESSAGE();
    RAISERROR(N'Loi nap du lieu mau: %s',16,1,@Loi);
END CATCH;
GO

SELECT 'VaiTro' AS TenBang, COUNT(*) AS SoDong FROM dbo.VaiTro
UNION ALL SELECT 'TaiKhoan',COUNT(*) FROM dbo.TaiKhoan
UNION ALL SELECT 'NhanVien',COUNT(*) FROM dbo.NhanVien
UNION ALL SELECT 'BenhNhan',COUNT(*) FROM dbo.BenhNhan
UNION ALL SELECT 'LichHen',COUNT(*) FROM dbo.LichHen
UNION ALL SELECT 'PhieuKham',COUNT(*) FROM dbo.PhieuKham
UNION ALL SELECT 'ThanhToan',COUNT(*) FROM dbo.ThanhToan
UNION ALL SELECT 'HoaDon',COUNT(*) FROM dbo.HoaDon
UNION ALL SELECT 'NhatKyHeThong',COUNT(*) FROM dbo.NhatKyHeThong;
GO
