package ui;

import database.PersonnelDAO;
import model.Personnel;
import model.Role;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class PersonnelPanel extends JPanel {

    private final PersonnelDAO personnelDAO;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public PersonnelPanel() {
        personnelDAO = new PersonnelDAO();

        // Налаштування головної панелі
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ініціалізація моделі таблиці
        String[] columns = {"ID", "ПІБ", "Посада", "Статус"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Налаштування візуалу таблиці
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(MAIN_FONT);
        table.setFocusable(false);
        table.setGridColor(Color.BLACK);

        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        // Кастомний рендер для заголовків таблиці
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);

                boolean isLast = (column == table.getColumnCount() - 1);
                javax.swing.border.Border matte = BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK);
                javax.swing.border.Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 0);

                setBorder(BorderFactory.createCompoundBorder(matte, padding));
                return this;
            }
        });

        // Кастомний рендер для звичайних комірок
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createCompoundBorder(getBorder(), BorderFactory.createEmptyBorder(0, 5, 0, 0)));
                return this;
            }
        });

        // Додавання таблиці у ScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок (низ)
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Ліва панель кнопок
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton rolesDictButton = new JButton("Довідник посад");
        rolesDictButton.setFont(MAIN_FONT);
        rolesDictButton.setFocusable(false);
        leftButtonPanel.add(rolesDictButton);

        // Права панель кнопок
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
        rolesDictButton.addActionListener(e -> showRolesManagerDialog());

        addButton.addActionListener(e -> showPersonnelDialog(null));

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть співробітника для редагування.", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            String roleName = (String) tableModel.getValueAt(selectedRow, 2);
            String status = (String) tableModel.getValueAt(selectedRow, 3);

            showPersonnelDialog(new Object[]{id, name, roleName, status});
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть співробітника для видалення!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Ви дійсно бажаєте видалити співробітника: " + name + "?",
                    "Підтвердження", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (personnelDAO.deletePersonnel(id)) {
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при видаленні з БД!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Завантаження даних при старті
        refreshTable();
    }

    // Допоміжні методи
    // Оновлення таблиці
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Personnel p : personnelDAO.getAllPersonnel()) {
            String statusDb = p.getStatus();
            String statusUkr = statusDb;

            if ("Active".equals(statusDb)) {
                statusUkr = "Активний";
            } else if ("On Leave".equals(statusDb)) {
                statusUkr = "У відпустці";
            } else if ("Fired".equals(statusDb)) {
                statusUkr = "Звільнений";
            }

            tableModel.addRow(new Object[]{ p.getId(), p.getFullName(), p.getRole().getName(), statusUkr });
        }
    }

    // Відкриття діалогу "Довідник посад"
    private void showRolesManagerDialog() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Довідник посад", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel roleTableModel = new DefaultTableModel(new String[]{"ID", "Назва посади"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable roleTable = new JTable(roleTableModel);
        roleTable.setRowHeight(25);
        roleTable.setFont(MAIN_FONT);
        roleTable.setFocusable(false);
        roleTable.setGridColor(Color.BLACK);
        roleTable.getColumnModel().getColumn(0).setMaxWidth(45);

        // Дизайн заголовків таблиці довідника
        roleTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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

        // Дизайн звичайних комірок таблиці довідника
        roleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        JScrollPane roleScrollPane = new JScrollPane(roleTable);
        roleScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(roleScrollPane, BorderLayout.CENTER);

        Runnable refreshRoleTable = () -> {
            roleTableModel.setRowCount(0);
            for (Role role : personnelDAO.getAllRoles()) {
                roleTableModel.addRow(new Object[]{role.getId(), role.getName()});
            }
        };
        refreshRoleTable.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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

        // Логіка кнопок довідника
        addBtn.addActionListener(e -> {
            JTextField nameField = new JTextField();
            nameField.setFont(MAIN_FONT);
            Object[] message = { "Назва посади:", nameField };

            if (JOptionPane.showConfirmDialog(dialog, message, "Додати", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    if (personnelDAO.addRoleAndGetId(newName) != -1) refreshRoleTable.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Заповніть назву посади!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editBtn.addActionListener(e -> {
            int row = roleTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Оберіть посаду для редагування!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) roleTableModel.getValueAt(row, 0);
            JTextField nameField = new JTextField((String) roleTableModel.getValueAt(row, 1));
            nameField.setFont(MAIN_FONT);

            if (JOptionPane.showConfirmDialog(dialog, new Object[]{"Назва посади:", nameField}, "Редагувати", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    if (personnelDAO.updateRole(id, newName)) {
                        refreshRoleTable.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Заповніть назву посади!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = roleTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, "Оберіть посаду для видалення!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) roleTableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(dialog, "Видалити посаду?", "Підтвердження", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (personnelDAO.deleteRole(id)) {
                    refreshRoleTable.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Ця посада закріплена за співробітниками!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                refreshTable();
            }
        });

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Відкриття діалогу "Додавання/Редагування співробітника"
    private void showPersonnelDialog(Object[] data) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        boolean isEditMode = (data != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEditMode ? "Редагувати співробітника" : "Додати співробітника", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Role> roles = personnelDAO.getAllRoles();

        // Збереження початкових значень для перевірки змін
        String initialName = isEditMode ? (String) data[1] : "";
        String initialStatus = isEditMode ? (String) data[3] : "Активний";
        final String initialRoleName = isEditMode ? (String) data[2] : (roles.isEmpty() ? "" : roles.get(0).getName());

        // Поле "ПІБ"
        JLabel nameLabel = new JLabel("ПІБ:");
        nameLabel.setFont(MAIN_FONT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField nameField = new JTextField(initialName);
        nameField.setFont(MAIN_FONT);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Поле "Посада"
        JLabel roleLabel = new JLabel("Посада:");
        roleLabel.setFont(MAIN_FONT);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<Role> roleCombo = new JComboBox<>();
        roleCombo.setFont(MAIN_FONT);
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        roleCombo.setFocusable(false);

        for (Role r : roles) {
            roleCombo.addItem(r);
            if (isEditMode && r.getName().equals(initialRoleName)) {
                roleCombo.setSelectedItem(r);
            }
        }

        Role otherRole = new Role(-1, "--- Додати іншу посаду ---") {
            @Override
            public String toString() { return getName(); }
        };
        roleCombo.addItem(otherRole);

        JPanel newRolePanel = new JPanel();
        newRolePanel.setLayout(new BoxLayout(newRolePanel, BoxLayout.Y_AXIS));
        newRolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        newRolePanel.setVisible(false);

        JLabel newRoleNameLabel = new JLabel("Назва нової посади:");
        newRoleNameLabel.setFont(MAIN_FONT);
        newRoleNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField newRoleNameField = new JTextField();
        newRoleNameField.setFont(MAIN_FONT);
        newRoleNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newRoleNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        newRolePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        newRolePanel.add(newRoleNameLabel);
        newRolePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        newRolePanel.add(newRoleNameField);
        newRolePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        roleCombo.addActionListener(e -> {
            Role selected = (Role) roleCombo.getSelectedItem();
            boolean isOther = (selected != null && selected.getId() == -1);
            newRolePanel.setVisible(isOther);
            dialog.setSize(380, isOther ? 360 : 280);
        });

        // Поле "Статус"
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setFont(MAIN_FONT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Активний", "У відпустці", "Звільнений"});
        statusCombo.setFont(MAIN_FONT);
        statusCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusCombo.setSelectedItem(initialStatus);
        statusCombo.setFocusable(false);

        formPanel.add(nameLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(nameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(roleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(roleCombo);
        formPanel.add(newRolePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(statusLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(statusCombo);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Панель кнопок (Скасувати / Зберегти)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        JButton cancelButton = new JButton("Скасувати");
        cancelButton.setFont(MAIN_FONT);
        cancelButton.setFocusable(false);
        JButton saveButton = new JButton("Зберегти");
        saveButton.setFont(MAIN_FONT);
        saveButton.setFocusable(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Перевірка на незбережені зміни перед закриттям
        Runnable checkAndClose = () -> {
            String currentName = nameField.getText().trim();
            Role currentRole = (Role) roleCombo.getSelectedItem();
            String currentRoleName = currentRole != null ? currentRole.getName() : "";
            String currentStatus = (String) statusCombo.getSelectedItem();
            boolean isNewRoleFilled = !newRoleNameField.getText().trim().isEmpty();

            boolean isChanged = !currentName.equals(initialName) ||
                    !currentRoleName.equals(initialRoleName) ||
                    !Objects.equals(currentStatus, initialStatus) ||
                    isNewRoleFilled;

            if (isChanged) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "У вас є незбережені зміни. Закрити вікно?",
                        "Увага", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    dialog.dispose();
                }
            } else {
                dialog.dispose();
            }
        };

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                checkAndClose.run();
            }
        });

        cancelButton.addActionListener(e -> checkAndClose.run());

        // Логіка збереження
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введіть ПІБ співробітника!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedStatusUkr = (String) statusCombo.getSelectedItem();
            String statusDb = "Active";
            if ("У відпустці".equals(selectedStatusUkr)) statusDb = "On Leave";
            if ("Звільнений".equals(selectedStatusUkr)) statusDb = "Fired";

            Role selectedRole = (Role) roleCombo.getSelectedItem();
            int roleId = selectedRole.getId();

            if (roleId == -1) {
                String newRoleName = newRoleNameField.getText().trim();
                if (newRoleName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Заповніть назву нової посади!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                roleId = personnelDAO.addRoleAndGetId(newRoleName);
                if (roleId == -1) {
                    JOptionPane.showMessageDialog(dialog, "Помилка збереження нової посади в БД!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            boolean success = isEditMode
                    ? personnelDAO.updatePersonnel((int) data[0], name, statusDb, roleId)
                    : personnelDAO.addPersonnel(name, statusDb, roleId);

            if (success) {
                dialog.dispose();
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Помилка бази даних при збереженні!", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}