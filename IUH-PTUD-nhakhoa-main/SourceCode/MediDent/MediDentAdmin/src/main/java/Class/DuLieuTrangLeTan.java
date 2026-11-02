package Class;

import java.util.ArrayList;
import java.util.List;

/** Gói kết quả tải từ SQL để chuyển từ luồng nền sang giao diện. */
public final class DuLieuTrangLeTan {
    public boolean viewAllowed;
    public boolean receiveAllowed;
    public int totalPatients;
    public int waitingToday;
    public int receivedToday;
    public int examsToday;
    public final List<Object[]> examRows = new ArrayList<>();
}
