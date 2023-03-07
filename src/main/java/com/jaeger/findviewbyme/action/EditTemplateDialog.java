package com.jaeger.findviewbyme.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.*;

public class EditTemplateDialog extends DialogWrapper {
    private JPanel contentPane;

    private JLabel tips;
    private JEditorPane editorPane;
    private JButton save;
    private JButton cancel;

    private OnSaveListener onSaveListener;

    public EditTemplateDialog() {
        super(null);
        init();

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

    @Override
    protected @NotNull Action[] createActions() {
        return new Action[]{};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    public interface OnSaveListener {
        void onSave(EditTemplateDialog dialog, String text);
    }
}
