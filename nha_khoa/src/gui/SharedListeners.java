package gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class SharedListeners {
    private SharedListeners() { }

    static DocumentListener document(Runnable onChange) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                onChange.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                onChange.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                onChange.run();
            }
        };
    }
}
