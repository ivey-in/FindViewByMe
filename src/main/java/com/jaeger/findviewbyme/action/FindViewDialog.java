package com.jaeger.findviewbyme.action;

import com.jaeger.findviewbyme.model.ViewPart;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

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

public class FindViewDialog extends JDialog {
    private JPanel contentPane;

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

    private JButton btnCopyCode;
    private JButton btnClose;

    private FindViewController controller = new FindViewController();

    private final static String[] HEADERS = {"selected", "type", "id", "name"};

    public FindViewDialog() {
        setContentPane(contentPane);
        setModal(true);

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

        contentPane.registerKeyboardAction(e -> FindViewDialog.this.onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        contentPane.registerKeyboardAction(e -> {
                    FindViewDialog.this.onCopy();
                    FindViewDialog.this.onCancel();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
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

    private void onCopy() {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(textCode.getText());
        clip.setContents(tText, null);
    }

    private void onCancel() {
        dispose();
    }

    public void setViewParts(List<ViewPart> viewParts) {
        controller.setViewParts(viewParts);
        updateTable();
        updateTextCode();
    }
}
