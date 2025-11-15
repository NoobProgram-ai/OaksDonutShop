package program;

import core.DB;
import entity.MenuItem;
import entity.MenuItemDAO;
import entity.Order;
import entity.OrderDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Main extends JFrame {

    // DAOs
    private final MenuItemDAO menuDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    // Menu UI components
    private JTable jTableMenu;
    private DefaultTableModel menuTableModel;
    private JTextField jTextMenuName;
    private JTextField jTextMenuPrice;
    private JTextField jTextMenuCategory;
    private JButton btnMenuInsert, btnMenuUpdate, btnMenuDelete;

    // Orders UI components
    private JTable jTableOrders;
    private DefaultTableModel ordersTableModel;

    // Left (menu selection) for order creation
    private JTable jTableMenuForOrder;
    private DefaultTableModel menuForOrderModel;
    private JSpinner spinnerQty;
    private JButton btnAddToCart, btnRemoveFromCart, btnPlaceOrder;

    // Cart table
    private JTable jTableCart;
    private DefaultTableModel cartTableModel;

    // In-memory cart
    private final List<CartItem> cart = new ArrayList<>();

    public Main() {
        super("Oak Donuts - Menu & Orders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        initComponents();
        refreshMenuTable();
        refreshMenuForOrderTable();
        refreshOrdersTable();
    }

    private void initComponents() {
        JTabbedPane tabs = new JTabbedPane();

        // ---------- Menu Panel ----------
        JPanel menuPanel = new JPanel(new BorderLayout(8, 8));
        menuTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Category"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        jTableMenu = new JTable(menuTableModel);
        jTableMenu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { menuRowSelected(); }
        });

        JScrollPane menuScroll = new JScrollPane(jTableMenu);
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // form for menu
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("Name:"), c);
        c.gridy++; form.add(new JLabel("Price:"), c);
        c.gridy++; form.add(new JLabel("Category:"), c);

        jTextMenuName = new JTextField(18);
        jTextMenuPrice = new JTextField(10);
        jTextMenuCategory = new JTextField(12);

        c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.LINE_START;
        form.add(jTextMenuName, c);
        c.gridy++; form.add(jTextMenuPrice, c);
        c.gridy++; form.add(jTextMenuCategory, c);

        // buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnMenuInsert = new JButton("Insert");
        btnMenuUpdate = new JButton("Update");
        btnMenuDelete = new JButton("Delete");
        btns.add(btnMenuInsert); btns.add(btnMenuUpdate); btns.add(btnMenuDelete);

        c.gridy++; form.add(btns, c);

        menuPanel.add(form, BorderLayout.EAST);

        // button actions
        btnMenuInsert.addActionListener(e -> onInsertMenuItem());
        btnMenuUpdate.addActionListener(e -> onUpdateMenuItem());
        btnMenuDelete.addActionListener(e -> onDeleteMenuItem());

        tabs.addTab("Menu", menuPanel);

        // ---------- Orders Panel ----------
        JPanel ordersPanel = new JPanel(new BorderLayout(8,8));

        // top area: split pane: left menu to select, middle cart, right orders
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplit.setResizeWeight(0.35);

        // Left: menu table for ordering (select item)
        menuForOrderModel = new DefaultTableModel(new String[]{"ID", "Name", "Price"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        jTableMenuForOrder = new JTable(menuForOrderModel);
        jTableMenuForOrder.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuForOrderScroll = new JScrollPane(jTableMenuForOrder);

        JPanel leftOrderPanel = new JPanel(new BorderLayout(6,6));
        leftOrderPanel.add(new JLabel("Menu (select + quantity)"), BorderLayout.NORTH);
        leftOrderPanel.add(menuForOrderScroll, BorderLayout.CENTER);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.add(new JLabel("Qty:"));
        spinnerQty = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        qtyPanel.add(spinnerQty);
        btnAddToCart = new JButton("Add to Cart");
        qtyPanel.add(btnAddToCart);
        leftOrderPanel.add(qtyPanel, BorderLayout.SOUTH);

        btnAddToCart.addActionListener(e -> onAddToCart());

        // Middle: cart panel
        cartTableModel = new DefaultTableModel(new String[]{"MenuID", "Name", "Qty", "UnitPrice", "LineTotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        jTableCart = new JTable(cartTableModel);
        JScrollPane cartScroll = new JScrollPane(jTableCart);

        JPanel cartPanel = new JPanel(new BorderLayout(6,6));
        cartPanel.add(new JLabel("Cart"), BorderLayout.NORTH);
        cartPanel.add(cartScroll, BorderLayout.CENTER);
        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRemoveFromCart = new JButton("Remove Selected");
        btnPlaceOrder = new JButton("Place Order");
        cartButtons.add(btnRemoveFromCart);
        cartButtons.add(btnPlaceOrder);
        cartPanel.add(cartButtons, BorderLayout.SOUTH);

        btnRemoveFromCart.addActionListener(a -> removeSelectedCartItem());
        btnPlaceOrder.addActionListener(a -> placeOrder());

        // Right: orders history
        ordersTableModel = new DefaultTableModel(new String[]{"OrderID", "Total", "DateTime", "Items"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        jTableOrders = new JTable(ordersTableModel);
        JScrollPane ordersScroll = new JScrollPane(jTableOrders);
        JPanel ordersHistoryPanel = new JPanel(new BorderLayout(6,6));
        ordersHistoryPanel.add(new JLabel("Orders"), BorderLayout.NORTH);
        ordersHistoryPanel.add(ordersScroll, BorderLayout.CENTER);

        topSplit.setLeftComponent(leftOrderPanel);
        JSplitPane midSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        midSplit.setLeftComponent(cartPanel);
        midSplit.setRightComponent(ordersHistoryPanel);
        midSplit.setResizeWeight(0.4);
        topSplit.setRightComponent(midSplit);

        ordersPanel.add(topSplit, BorderLayout.CENTER);

        tabs.addTab("Orders", ordersPanel);

        add(tabs, BorderLayout.CENTER);
    }

    // ---------- Menu actions ----------
    private void menuRowSelected() {
        int r = jTableMenu.getSelectedRow();
        if (r == -1) return;
        String id = menuTableModel.getValueAt(r, 0).toString();
        String name = menuTableModel.getValueAt(r, 1).toString();
        String price = menuTableModel.getValueAt(r, 2).toString();
        String category = menuTableModel.getValueAt(r, 3).toString();
        jTextMenuName.setText(name);
        jTextMenuPrice.setText(price);
        jTextMenuCategory.setText(category);
    }

    private void onInsertMenuItem() {
        try {
            String name = jTextMenuName.getText().trim();
            String priceStr = jTextMenuPrice.getText().trim();
            String cat = jTextMenuCategory.getText().trim();
            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and price required");
                return;
            }
            double price = Double.parseDouble(priceStr);
            MenuItem item = new MenuItem(0, name, price, cat); // id will be ignored by DAO insert and generated by DB
            menuDAO.insert(item);
            refreshMenuTable();
            refreshMenuForOrderTable();
            clearMenuFields();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Price must be a number");
        }
    }

    private void onUpdateMenuItem() {
        int r = jTableMenu.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a menu item to update");
            return;
        }
        int id = Integer.parseInt(menuTableModel.getValueAt(r, 0).toString());
        try {
            String name = jTextMenuName.getText().trim();
            double price = Double.parseDouble(jTextMenuPrice.getText().trim());
            String cat = jTextMenuCategory.getText().trim();
            MenuItem item = new MenuItem(id, name, price, cat);
            menuDAO.update(item);
            refreshMenuTable();
            refreshMenuForOrderTable();
            clearMenuFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a number");
        }
    }

    private void onDeleteMenuItem() {
        int r = jTableMenu.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to delete");
            return;
        }
        int id = Integer.parseInt(menuTableModel.getValueAt(r, 0).toString());
        String name = menuTableModel.getValueAt(r, 1).toString();
        MenuItem item = new MenuItem(id, name, 0.0, "");
        int ok = JOptionPane.showConfirmDialog(this, "Delete selected menu item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            menuDAO.delete(item);
            refreshMenuTable();
            refreshMenuForOrderTable();
            clearMenuFields();
        }
    }

    private void clearMenuFields() {
        jTextMenuName.setText("");
        jTextMenuPrice.setText("");
        jTextMenuCategory.setText("");
        jTableMenu.clearSelection();
    }

    // ---------- Orders actions ----------
    private void refreshMenuTable() {
        menuTableModel.setRowCount(0);
        List<MenuItem> items = menuDAO.getAll();
        if (items == null) return;
        for (MenuItem m : items) {
            menuTableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    String.format("%.2f", m.getPrice()),
                    m.getCategory()
            });
        }
    }

    private void refreshMenuForOrderTable() {
        menuForOrderModel.setRowCount(0);
        List<MenuItem> items = menuDAO.getAll();
        if (items == null) return;
        for (MenuItem m : items) {
            menuForOrderModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    String.format("%.2f", m.getPrice())
            });
        }
    }

    private void refreshOrdersTable() {
        ordersTableModel.setRowCount(0);
        List<Order> orders = orderDAO.getAll();
        if (orders == null) return;
        for (Order o : orders) {
            ordersTableModel.addRow(new Object[]{
                    o.getID(),
                    String.format("%.2f", o.getPrice()),
                    o.getDateTime(),
                    o.getItemName()
            });
        }
    }


    private void onAddToCart() {
        int r = jTableMenuForOrder.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a menu item first");
            return;
        }
        int id = Integer.parseInt(menuForOrderModel.getValueAt(r, 0).toString());
        String name = menuForOrderModel.getValueAt(r, 1).toString();
        double unitPrice = Double.parseDouble(menuForOrderModel.getValueAt(r, 2).toString());
        int qty = (Integer) spinnerQty.getValue();
        // check if exists in cart -> increase qty
        for (CartItem ci : cart) {
            if (ci.menuId == id) {
                ci.qty += qty;
                ci.lineTotal = ci.qty * ci.unitPrice;
                refreshCartTable();
                return;
            }
        }
        CartItem ci = new CartItem(id, name, qty, unitPrice, qty * unitPrice);
        cart.add(ci);
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (CartItem ci : cart) {
            cartTableModel.addRow(new Object[]{ci.menuId, ci.name, ci.qty, String.format("%.2f", ci.unitPrice), String.format("%.2f", ci.lineTotal)});
        }
    }

    private void removeSelectedCartItem() {
        int r = jTableCart.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a cart row to remove");
            return;
        }
        int menuId = Integer.parseInt(cartTableModel.getValueAt(r, 0).toString());
        cart.removeIf(ci -> ci.menuId == menuId);
        refreshCartTable();
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart empty");
            return;
        }

        StringBuilder items = new StringBuilder();
        double subtotal = 0.0;

        for (CartItem ci : cart) {
            if (items.length() > 0) items.append("; ");
            items.append(ci.name).append(" x").append(ci.qty);
            subtotal += ci.lineTotal;
        }

        double tax = subtotal * 0.06;
        double total = subtotal + tax;

        LocalDateTime now = LocalDateTime.now();
        String ts = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Order o = new Order(
                0,
                total,
                ts,
                items.toString()
        );

        orderDAO.insert(o);
        cart.clear();
        refreshCartTable();
        refreshOrdersTable();

        JOptionPane.showMessageDialog(
                this,
                "Order placed.\nSubtotal: " + String.format("%.2f", subtotal) +
                        "\nTax (6%): " + String.format("%.2f", tax) +
                        "\nTotal: " + String.format("%.2f", total)
        );
    }



    // ---------- helper classes ----------
    private static class CartItem {
        int menuId;
        String name;
        int qty;
        double unitPrice;
        double lineTotal;
        CartItem(int menuId, String name, int qty, double unitPrice, double lineTotal) {
            this.menuId = menuId;
            this.name = name;
            this.qty = qty;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }
    }

    // ---------- Main.form ----------
    public static void main(String[] args) {
        // Ensure DB singleton is initialized (runs migrations)
        try {
            DB.getInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}