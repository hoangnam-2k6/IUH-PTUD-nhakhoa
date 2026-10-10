
package gui;

public class NhanVienGUI extends QuanLyBangGUI {

    public NhanVienGUI() {
        super(
            "Nhân viên",
            "Quản lý nhân viên",
            "Quản lý hồ sơ, vị trí và trạng thái làm việc của nhân viên.",

            new String[] {
                "MÃ NV",
                "HỌ TÊN",
                "EMAIL",
                "VỊ TRÍ",
                "SỐ ĐIỆN THOẠI",
                "TRẠNG THÁI"
            },

            new String[][] {
                {
                    "NV-001", "Nguyễn Hoàng Việt",
                    "viet.nguyen@medident.vn",
                    "Bác sĩ nha khoa", "090 812 4567",
                    "Đang làm việc"
                },
                {
                    "NV-002", "Trần Minh Trang",
                    "trang.tran@medident.vn",
                    "Bác sĩ chỉnh nha", "091 536 2804",
                    "Đang làm việc"
                },
                {
                    "NV-003", "Phạm Tuấn Anh",
                    "anh.pham@medident.vn",
                    "Bác sĩ điều trị", "098 421 7830",
                    "Đang làm việc"
                },
                {
                    "NV-004", "Lê Thu Hương",
                    "huong.le@medident.vn",
                    "Lễ tân", "093 728 1540",
                    "Đang nghỉ phép"
                },
                {
                    "NV-005", "Võ Hải Yến",
                    "yen.vo@medident.vn",
                    "Điều dưỡng", "097 315 8624",
                    "Đang làm việc"
                },
                {
                    "NV-006", "Đỗ Đức Long",
                    "long.do@medident.vn",
                    "Kế toán", "096 842 3107",
                    "Tạm ngưng"
                }
            },

            3,
            5,

            new String[] {
                "Tất cả vị trí",
                "Bác sĩ nha khoa",
                "Bác sĩ chỉnh nha",
                "Bác sĩ điều trị",
                "Lễ tân",
                "Điều dưỡng",
                "Kế toán"
            }
        );
    }
}
