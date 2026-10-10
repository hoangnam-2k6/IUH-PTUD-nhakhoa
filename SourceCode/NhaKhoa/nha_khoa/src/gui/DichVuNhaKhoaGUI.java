
package gui;

public class DichVuNhaKhoaGUI extends QuanLyBangGUI {

    public DichVuNhaKhoaGUI() {
        super(
            "Dịch vụ nha khoa",
            "Dịch vụ nha khoa",
            "Quản lý danh mục, mô tả và bảng giá dịch vụ nha khoa.",

            new String[] {
                "MÃ DV",
                "TÊN DỊCH VỤ",
                "MÔ TẢ",
                "DANH MỤC",
                "ĐƠN GIÁ",
                "TRẠNG THÁI"
            },

            new String[][] {
                {
                    "DV-001", "Khám tổng quát",
                    "Khám sức khỏe răng miệng và tư vấn kế hoạch điều trị",
                    "Khám & tư vấn", "500.000 đ",
                    "Đang áp dụng"
                },
                {
                    "DV-002", "Lấy cao răng",
                    "Làm sạch cao răng và đánh bóng hai hàm",
                    "Điều trị nha chu", "450.000 đ",
                    "Đang áp dụng"
                },
                {
                    "DV-003", "Điều trị tủy",
                    "Điều trị nội nha và phục hồi mô răng tổn thương",
                    "Điều trị", "2.800.000 đ",
                    "Đang áp dụng "
                },
                {
                    "DV-004", "Tẩy trắng răng",
                    "Tẩy trắng răng tại phòng khám bằng công nghệ ánh sáng",
                    "Nha khoa thẩm mỹ", "1.200.000 đ",
                    "Đang áp dụng"
                },
                {
                    "DV-005", "Niềng răng mắc cài",
                    "Chỉnh nha toàn diện bằng hệ thống mắc cài kim loại",
                    "Chỉnh nha", "35.000.000 đ",
                    "Tạm ngưng"
                }
            },

            3,
            5,

            new String[] {
                "Tất cả danh mục",
                "Khám & tư vấn",
                "Điều trị nha chu",
                "Điều trị",
                "Nha khoa thẩm mỹ",
                "Chỉnh nha "
            }
        );
    }
}
