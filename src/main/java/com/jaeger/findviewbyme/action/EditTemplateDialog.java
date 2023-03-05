package com.jaeger.findviewbyme.action;

import javax.swing.*;
import java.awt.event.*;

public class EditTemplateDialog extends JDialog {
    private JPanel contentPane;

    private JLabel tips;
    private JEditorPane editorPane;
    private JButton save;
    private JButton cancel;

    private OnSaveListener onSaveListener;

    public EditTemplateDialog() {
        setContentPane(contentPane);
        setModal(true);

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditTemplateDialog.this.onSave();
                EditTemplateDialog.this.onCancel();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditTemplateDialog.this.onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    public void setOnSaveListener(OnSaveListener onSaveListener) {
        this.onSaveListener = onSaveListener;
    }

    public void setTemplate(String text) {
        editorPane.setText(text != null ? text : "");
    }

    public void onSave() {
        if (onSaveListener != null) {
            onSaveListener.onSave(this, editorPane.getText());
        }
    }

    private void onCancel() {
        dispose();
    }

    public interface OnSaveListener {
        void onSave(EditTemplateDialog dialog, String text);
    }
}
