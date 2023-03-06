package com.jaeger.findviewbyme.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.jaeger.findviewbyme.model.FindViewPropes;
import com.jaeger.findviewbyme.model.ViewPart;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.List;

public class FindViewDialog extends DialogWrapper {
    private JPanel contentPane;

    private JLabel tips;

    private JComboBox<String> codeTemplate;
    private JButton editTemplate;
    private JCheckBox chbAddM;
    private JTextField varPrefix;

    private JTextField editSearch;
    private JButton btnSearch;

    private JTable tableViews;
    private JButton btnSelectAll;
    private JButton btnSelectNone;
    private JButton btnNegativeSelect;

    private JTextArea textCode;

    private JButton btnInjectCode;
    private JButton btnCopyCode;
    private JButton btnClose;

    private FindViewController controller = new FindViewController();

    private final static String[] HEADERS = {"selected", "type", "id", "name"};

    public FindViewDialog(@Nullable Project project) {
        super(project);
        init();
        tips.setText(FindViewPropes.TIPS);
        updateCodeTemp();
        editTemplate.addActionListener(e -> onEditTemplateClick());
        chbAddM.setSelected(controller.propes.isVarPrefixEnabled());
        chbAddM.addChangeListener(e -> {
            if (controller.setVarPrefixEnabled(chbAddM.isSelected())) {
                updateTable();
                updateTextCode();
            }
        });
        String prefix = controller.propes.getVarPrefix();
        varPrefix.setText(prefix);
        varPrefix.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateVarPrefix();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateVarPrefix();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateVarPrefix();
            }

            private void updateVarPrefix() {
                if (controller.setVarPrefix(varPrefix.getText())) {
                    updateTable();
                    updateTextCode();
                }
            }
        });

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = controller.search(getSearch());
                setTableSelect(Math.max(sel, 0));
            }
        });


        btnSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.selectAll();
                updateTable();
                updateTextCode();
            }
        });
        btnSelectNone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.selectNone();
                updateTable();
                updateTextCode();
            }
        });
        btnNegativeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.negativeSelect();
                updateTable();
                updateTextCode();
            }
        });

        btnInjectCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindViewDialog.this.onInjectCode();
                FindViewDialog.this.onCancel();
            }
        });
        btnCopyCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindViewDialog.this.onCopy();
                FindViewDialog.this.onCancel();
            }
        });
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindViewDialog.this.onCancel();
            }
        });
    }

    public void updateCodeTemp() {
        List<String> temps = controller.propes.getAllTempList();
        String curTemp = controller.propes.getCurTemp();

        ListComboBoxModel<String> model = new ListComboBoxModel<String>(temps);
        if (curTemp != null && temps.contains(curTemp)) {
            model.setSelectedItem(curTemp);
        }
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {

            }

            @Override
            public void intervalRemoved(ListDataEvent e) {

            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                controller.propes.setCurTemp(model.getSelectedItem());
                updateTextCode();
            }
        });
        codeTemplate.setModel(model);
    }

    private void onEditTemplateClick() {
        EditTemplateDialog dialog = new EditTemplateDialog(controller.getProject());
        dialog.setTemplate(controller.propes.getAllTemp());
        dialog.setOnSaveListener(new EditTemplateDialog.OnSaveListener() {
            @Override
            public void onSave(EditTemplateDialog dialog, String text) {
                if (controller.propes.setAllTemp(text)) {
                    updateCodeTemp();
                    updateTable();
                    updateTextCode();
                }
            }
        });
        dialog.show();
    }

    public String getSearch() {
        String text = editSearch.getText();
        return text != null ? text.trim() : "";
    }

    public void updateTable() {
        tableViews.setModel(getTableModel());
        tableViews.getColumnModel().getColumn(0).setPreferredWidth(20);
    }

    private DefaultTableModel getTableModel() {
        List<ViewPart> viewParts = controller.getViewParts();
        int size = viewParts.size();
        Object[][] cellData = new Object[size][4];
        for (int i = 0; i < size; i++) {
            ViewPart viewPart = viewParts.get(i);
            for (int j = 0; j < 4; j++) {
                switch (j) {
                    case 0:
                        cellData[i][j] = viewPart.isSelected();
                        break;
                    case 1:
                        cellData[i][j] = viewPart.getType();
                        break;
                    case 2:
                        cellData[i][j] = viewPart.getId();
                        break;
                    case 3:
                        cellData[i][j] = viewPart.getName();
                        break;
                }
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(cellData, HEADERS) {
            final Class[] typeArray = {Boolean.class, Object.class, Object.class, Object.class};

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @SuppressWarnings("rawtypes")
            public Class getColumnClass(int column) {
                return typeArray[column];
            }
        };
        tableModel.addTableModelListener(event -> {
            int row = event.getFirstRow();
            int column = event.getColumn();
            if (column == 0) {
                Boolean isSelected = (Boolean) tableModel.getValueAt(row, column);
                controller.select(row, isSelected);
                updateTextCode();
            }
        });
        return tableModel;
    }

    public void setTableSelect(int position) {
        tableViews.grabFocus();
        tableViews.changeSelection(position, 1, false, false);
    }

    public void updateTextCode() {
        textCode.setText(controller.generateCode());
    }

    private void onInjectCode() {
        controller.injectCode();
    }

    private void onCopy() {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(textCode.getText());
        clip.setContents(tText, null);
    }

    private void onCancel() {
        dispose();
    }

    public void setViewParts(Project project, Editor editor, PsiFile file, PsiClass psiClass, List<ViewPart> viewParts) {
        controller.setViewParts(project, editor, file, psiClass, viewParts);
        btnInjectCode.setVisible(controller.isInjectCodeSupported());
        updateTable();
        updateTextCode();
    }

    @Override
    protected @NotNull Action[] createActions() {
        return new Action[]{};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
