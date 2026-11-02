package UI.LeTan;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Gọi lại một hành động mỗi khi nội dung ô nhập thay đổi. */
public final class BoLangNgheTimKiem implements DocumentListener {
    private final Runnable action;

    public BoLangNgheTimKiem(Runnable action) {
        this.action = action;
    }

    @Override public void insertUpdate(DocumentEvent e) { action.run(); }
    @Override public void removeUpdate(DocumentEvent e) { action.run(); }
    @Override public void changedUpdate(DocumentEvent e) { action.run(); }
}
