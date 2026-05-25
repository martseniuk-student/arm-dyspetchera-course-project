package ui;

import database.TransportDAO;
import model.Transport;
import model.TransportType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransportPanel extends JPanel {

    private final TransportDAO transportDAO;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public TransportPanel() {
        transportDAO = new TransportDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Налаштування таблиці
        tableModel = new DefaultTableModel(new String[]{"ID", "Модель", "Бортовий номер", "Місткість", "Статус"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(MAIN_FONT);
        table.setFocusable(false);
        table.setGridColor(Color.BLACK);

        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        // Дизайн заголовків
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);
                boolean isLast = (c == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton modelsDictButton = new JButton("Довідник моделей");
        modelsDictButton.setFont(MAIN_FONT);
        modelsDictButton.setFocusable(false);
        leftButtonPanel.add(modelsDictButton);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton addButton = new JButton("Додати");
        addButton.setFont(MAIN_FONT);
        addButton.setFocusable(false);

        JButton editButton = new JButton("Редагувати");
        editButton.setFont(MAIN_FONT);
        editButton.setFocusable(false);

        JButton deleteButton = new JButton("Видалити");
        deleteButton.setFont(MAIN_FONT);
        deleteButton.setFocusable(false);

        rightButtonPanel.add(addButton);
        rightButtonPanel.add(editButton);
        rightButtonPanel.add(deleteButton);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        // Логіка кнопок
        modelsDictButton.addActionListener(e -> showModelsManagerDialog());

        addButton.addActionListener(e -> showTransportDialog(null));

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть транспорт для редагування.", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String boardNumber = (String) tableModel.getValueAt(selectedRow, 2);
            String typeName = (String) tableModel.getValueAt(selectedRow, 1);
            String status = (String) tableModel.getValueAt(selectedRow, 4);

            showTransportDialog(new Object[]{id, boardNumber, typeName, null, status});
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть транспорт для видалення!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String boardNumber = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Ви дійсно бажаєте видалити транспортний засіб з бортовим номером: " + boardNumber + "?",
                    "Підтвердження", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (transportDAO.deleteTransport(id)) {
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при видаленні!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Початкове завантаження
        refreshTable();
    }

    // Допоміжні методи
    // Оновлення таблиці
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Transport t : transportDAO.getAllTransport()) {
            String statusDb = t.getStatus();
            String statusUkr = statusDb;

            if ("Functional".equals(statusDb)) {
                statusUkr = "Активний";
            } else if ("Under Maintenance".equals(statusDb)) {
                statusUkr = "У ремонті";
            } else if ("Decommissioned".equals(statusDb)) {
                statusUkr = "Списаний";
            }

            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getType().getName(),
                    t.getBoardNumber(),
                    t.getType().getCapacity(),
                    statusUkr
            });
        }
    }

    // Відкриття діалогу "Довідник моделей"
    private void showModelsManagerDialog() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Довідник моделей транспорту", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel modelTableModel = new DefaultTableModel(new String[]{"ID", "Назва моделі", "Місткість"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable modelTable = new JTable(modelTableModel);
        modelTable.setRowHeight(25);
        modelTable.setFont(MAIN_FONT);
        modelTable.setFocusable(false);
        modelTable.setGridColor(Color.BLACK);
        modelTable.getColumnModel().getColumn(0).setMinWidth(45);
        modelTable.getColumnModel().getColumn(0).setMaxWidth(45);

        // Дизайн заголовків
        modelTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);
                boolean isLast = (c == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        modelTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        JScrollPane modelScrollPane = new JScrollPane(modelTable);
        modelScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(modelScrollPane, BorderLayout.CENTER);

        Runnable refreshModelTable = () -> {
            modelTableModel.setRowCount(0);
            for (TransportType type : transportDAO.getAllTransportTypes()) {
                modelTableModel.addRow(new Object[]{type.getId(), type.getName(), type.getCapacity()});
            }
        };
        refreshModelTable.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addBtn = new JButton("Додати");
        JButton editBtn = new JButton("Редагувати");
        JButton deleteBtn = new JButton("Видалити");


        addBtn.setFont(MAIN_FONT);
        editBtn.setFont(MAIN_FONT);
        deleteBtn.setFont(MAIN_FONT);
        addBtn.setFocusable(false);
        editBtn.setFocusable(false);
        deleteBtn.setFocusable(false);

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // Логіка кнопок довідника
        addBtn.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField capField = new JTextField();
            Object[] message = { "Назва моделі:", nameField, "Місткість:", capField };

            if (JOptionPane.showConfirmDialog(dialog, message, "Додати нову модель", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String newName = nameField.getText().trim();
                    String capText = capField.getText().trim();

                    if (newName.isEmpty() || capText.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Заповніть всі поля!", "Помилка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int newCap = Integer.parseInt(capText);
                    if (newCap <= 0) {
                        JOptionPane.showMessageDialog(dialog, "Місткість має бути більше нуля!", "Помилка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (transportDAO.addTransportTypeAndGetId(newName, newCap) != -1) {
                        refreshModelTable.run();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Помилка збереження нової моделі в БД!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Місткість має бути числом!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editBtn.addActionListener(e -> {
            int row = modelTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Оберіть модель для редагування!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) modelTableModel.getValueAt(row, 0);
            JTextField nameField = new JTextField((String) modelTableModel.getValueAt(row, 1));
            JTextField capField = new JTextField(String.valueOf(modelTableModel.getValueAt(row, 2)));
            Object[] message = { "Назва моделі:", nameField, "Місткість:", capField };

            if (JOptionPane.showConfirmDialog(dialog, message, "Редагувати модель", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String newName = nameField.getText().trim();
                    int newCap = Integer.parseInt(capField.getText().trim());

                    if (!newName.isEmpty() && newCap > 0) {
                        transportDAO.updateTransportType(id, newName, newCap);
                        refreshModelTable.run();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Некоректні дані!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Місткість має бути числом!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = modelTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Оберіть модель для видалення!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) modelTableModel.getValueAt(row, 0);
            String name = (String) modelTableModel.getValueAt(row, 1);

            if (JOptionPane.showConfirmDialog(dialog, "Видалити модель " + name + "?", "Підтвердження", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (transportDAO.deleteTransportType(id)) {
                    refreshModelTable.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Неможливо видалити модель!\nСпочатку видаліть або змініть модель для всього транспорту цього типу.", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent windowEvent) { refreshTable(); }
        });

        dialog.setVisible(true);
    }

    // Відкриття діалогу "Додавання/Редагування транспорту"
    private void showTransportDialog(Object[] data) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        boolean isEditMode = (data != null);
        String title = isEditMode ? "Редагувати транспорт" : "Додати транспорт";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<TransportType> types = transportDAO.getAllTransportTypes();

        String initialBoardNumber = isEditMode ? (String) data[1] : "";
        String initialStatus = isEditMode ? (String) data[4] : "Активний";
        final String initialTypeName = isEditMode ? (String) data[2] : (types.isEmpty() ? "" : types.get(0).getName());

        JLabel boardNumberLabel = new JLabel("Бортовий номер:");
        boardNumberLabel.setFont(MAIN_FONT);
        boardNumberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField boardNumberField = new JTextField(initialBoardNumber);
        boardNumberField.setFont(MAIN_FONT);
        boardNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);
        boardNumberField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel typeLabel = new JLabel("Модель транспорту:");
        typeLabel.setFont(MAIN_FONT);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<TransportType> typeCombo = new JComboBox<>();
        typeCombo.setFont(MAIN_FONT);
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        typeCombo.setFocusable(false);

        for (TransportType t : types) {
            typeCombo.addItem(t);
            if (isEditMode && t.getName().equals(initialTypeName)) typeCombo.setSelectedItem(t);
        }

        TransportType otherType = new TransportType(-1, "--- Додати іншу модель ---", 0) {
            @Override public String toString() { return getName(); }
        };
        typeCombo.addItem(otherType);

        JPanel newModelPanel = new JPanel();
        newModelPanel.setLayout(new BoxLayout(newModelPanel, BoxLayout.Y_AXIS));
        newModelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        newModelPanel.setVisible(false);

        JLabel newModelNameLabel = new JLabel("Назва нової моделі:");
        newModelNameLabel.setFont(MAIN_FONT);
        newModelNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField newModelNameField = new JTextField();
        newModelNameField.setFont(MAIN_FONT);
        newModelNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newModelNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel newModelCapacityLabel = new JLabel("Місткість (пасажирів):");
        newModelCapacityLabel.setFont(MAIN_FONT);
        newModelCapacityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField newModelCapacityField = new JTextField();
        newModelCapacityField.setFont(MAIN_FONT);
        newModelCapacityField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newModelCapacityField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        newModelPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        newModelPanel.add(newModelNameLabel);
        newModelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        newModelPanel.add(newModelNameField);
        newModelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        newModelPanel.add(newModelCapacityLabel);
        newModelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        newModelPanel.add(newModelCapacityField);
        newModelPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        typeCombo.addActionListener(e -> {
            TransportType selected = (TransportType) typeCombo.getSelectedItem();
            boolean isOther = (selected != null && selected.getId() == -1);
            newModelPanel.setVisible(isOther);
            dialog.setSize(380, isOther ? 460 : 320);
        });

        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(MAIN_FONT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Активний", "У ремонті", "Списаний"});
        statusCombo.setFont(MAIN_FONT);
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusCombo.setSelectedItem(initialStatus);
        statusCombo.setFocusable(false);

        formPanel.add(boardNumberLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(boardNumberField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(typeLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(typeCombo);
        formPanel.add(newModelPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(statusLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(statusCombo);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Скасувати");
        JButton saveButton = new JButton("Зберегти");
        cancelButton.setFont(MAIN_FONT);
        saveButton.setFont(MAIN_FONT);
        cancelButton.setFocusable(false);
        saveButton.setFocusable(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        Runnable checkAndClose = () -> {
            String currentBoardNumber = boardNumberField.getText().trim();
            TransportType currentType = (TransportType) typeCombo.getSelectedItem();
            String currentTypeName = currentType != null ? currentType.getName() : "";
            String currentStatus = (String) statusCombo.getSelectedItem();
            boolean isNewModelFilled = !newModelNameField.getText().trim().isEmpty();

            boolean isChanged = !currentBoardNumber.equals(initialBoardNumber) ||
                    !currentTypeName.equals(initialTypeName) ||
                    !currentStatus.equals(initialStatus) ||
                    isNewModelFilled;

            if (isChanged) {
                if (JOptionPane.showConfirmDialog(dialog, "У вас є незбережені зміни. Закрити вікно?", "Увага", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    dialog.dispose();
                }
            } else {
                dialog.dispose();
            }
        };

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent windowEvent) { checkAndClose.run(); }
        });

        cancelButton.addActionListener(e -> checkAndClose.run());

        saveButton.addActionListener(e -> {
            String boardNumber = boardNumberField.getText().trim();
            if (boardNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введіть бортовий номер!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TransportType selectedType = (TransportType) typeCombo.getSelectedItem();
            String selectedStatusUkr = (String) statusCombo.getSelectedItem();

            String statusDb = "Functional";
            if ("У ремонті".equals(selectedStatusUkr)) statusDb = "Under Maintenance";
            if ("Списаний".equals(selectedStatusUkr)) statusDb = "Decommissioned";

            int typeId = selectedType.getId();

            if (typeId == -1) {
                String newModelName = newModelNameField.getText().trim();
                String newCapStr = newModelCapacityField.getText().trim();

                if (newModelName.isEmpty() || newCapStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Заповніть назву та місткість нової моделі!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int capacity = Integer.parseInt(newCapStr);
                    typeId = transportDAO.addTransportTypeAndGetId(newModelName, capacity);
                    if (typeId == -1) {
                        JOptionPane.showMessageDialog(dialog, "Помилка збереження нової моделі в БД!", "Помилка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Місткість повинна бути числом!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            boolean success = isEditMode
                    ? transportDAO.updateTransport((int) data[0], boardNumber, statusDb, typeId)
                    : transportDAO.addTransport(boardNumber, statusDb, typeId);

            if (success) {
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Помилка бази даних (можливо, такий борт. номер вже існує)!", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}