/*
 * SharedListeners: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class SharedListeners {
    /** Ngăn tạo lớp chỉ cung cấp bộ lắng nghe dữ liệu dùng chung. */
    private SharedListeners() { }

    static DocumentListener document(Runnable onChange) {
        return new DocumentListener() {
            @Override
            /** Chạy callback khi nội dung ô nhập vừa được thêm ký tự. */
            public void insertUpdate(DocumentEvent event) {
                onChange.run();
            }

            @Override
            /** Chạy callback khi nội dung ô nhập vừa bị xóa. */
            public void removeUpdate(DocumentEvent event) {
                onChange.run();
            }

            @Override
            /** Chạy callback khi thuộc tính của Document thay đổi. */
            public void changedUpdate(DocumentEvent event) {
                onChange.run();
            }
        };
    }
}
