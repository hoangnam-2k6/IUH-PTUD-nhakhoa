package UI.QuanLy;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * Quản lý việc chuyển đổi giữa panel danh sách và các panel thêm/sửa.
 */
public class QuanLyPanel {

    private final JPanel container;
    private final JPanel listPanel;
    private JPanel currentPanel;
    private final Runnable reload;

    public QuanLyPanel(JPanel container, JPanel listPanel, Runnable reload) {
        this.container = container;
        this.listPanel = listPanel;
        this.reload = reload;

        this.container.setLayout(new BorderLayout());
        this.currentPanel = listPanel;

        this.container.add(listPanel, BorderLayout.CENTER);
        this.container.revalidate();
        this.container.repaint();
    }

    // Hiển thị form thêm hoặc sửa
    public void show(JPanel panelMoi) {
        if (panelMoi == null) {
            return;
        }

        container.removeAll();
        currentPanel = panelMoi;
        container.add(panelMoi, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();
    }

    // Quay lại danh sách và tải lại dữ liệu
    public void showList() {
        if (reload != null) {
            reload.run();
        }

        container.removeAll();
        currentPanel = listPanel;
        container.add(listPanel, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();
    }

    // Đưa chức năng về trạng thái danh sách khi chuyển sang chức năng khác
    public void reset() {
        showList();
    }

    public JPanel getCurrentPanel() {
        return currentPanel;
    }
}
