import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main implements Serializable {

    private static final long serialVersionUID = 1L;

    static abstract class Product implements Serializable {
        private static final long serialVersionUID = 1L;
        protected String id;
        protected String title;
        protected double price;

        public Product(String id, String title, double price) {
            this.id = id;
            this.title = title;
            this.price = price;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public double getPrice() { return price; }

        public String getType() { return "Product"; }

        @Override
        public String toString() {
            return String.format("%s (id=%s) - %s: $%.2f", getType(), id, title, price);
        }
    }

    static class Book extends Product {
        private static final long serialVersionUID = 1L;
        private String author;
        private int pages;

        public Book(String id, String title, double price, String author, int pages) {
            super(id, title, price);
            this.author = author;
            this.pages = pages;
        }

        public String getAuthor() { return author; }
        public int getPages() { return pages; }

        @Override
        public String getType() { return "Book"; }

        @Override
        public String toString() {
            return String.format("%s - %s by %s, %d pages ($%.2f)", getType(), title, author, pages, price);
        }
    }

    static class Magazine extends Product {
        private static final long serialVersionUID = 1L;
        private int issueNumber;

        public Magazine(String id, String title, double price, int issueNumber) {
            super(id, title, price);
            this.issueNumber = issueNumber;
        }

        @Override
        public String getType() { return "Magazine"; }

        @Override
        public String toString() {
            return String.format("%s - %s (Issue %d) - $%.2f", getType(), title, issueNumber, price);
        }
    }

    static class CD extends Product {
        private static final long serialVersionUID = 1L;
        private String artist;
        private int tracks;

        public CD(String id, String title, double price, String artist, int tracks) {
            super(id, title, price);
            this.artist = artist;
            this.tracks = tracks;
        }

        @Override
        public String getType() { return "CD"; }

        @Override
        public String toString() {
            return String.format("%s - %s by %s, %d tracks ($%.2f)", getType(), title, artist, tracks, price);
        }
    }

    static class Customer implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String contact;

        public Customer(String id, String name, String contact) {
            this.id = id;
            this.name = name;
            this.contact = contact;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getContact() { return contact; }

        @Override
        public String toString() {
            return String.format("Customer %s (id=%s)", name, id);
        }
    }

    static class OrderItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Product product;
        private int quantity;

        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public double lineTotal() { return product.getPrice() * quantity; }

        @Override
        public String toString() {
            return String.format("%s x%d - $%.2f", product.getTitle(), quantity, lineTotal());
        }
    }

    static class Order implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private Customer customer;
        private List<OrderItem> items = new ArrayList<>();
        private boolean delivered = false;

        public Order(String id, Customer customer) {
            this.id = id;
            this.customer = customer;
        }

        public void addItem(Product p, int qty) { items.add(new OrderItem(p, qty)); }

        public double total() {
            return items.stream().mapToDouble(OrderItem::lineTotal).sum();
        }

        public void markDelivered() { delivered = true; }

        public void printInvoice() {
            System.out.println("---------------------------");
            System.out.println("Invoice for Order: " + id);
            System.out.println("Customer: " + customer.getName() + " (" + customer.getContact() + ")");
            System.out.println("Items:");
            for (OrderItem oi : items) {
                System.out.println("  - " + oi.toString());
            }
            System.out.printf("Total: $%.2f\n", total());
            System.out.println("Delivered: " + (delivered ? "Yes" : "No"));
            System.out.println("---------------------------");
        }
    }

    static class Bookshop implements Serializable {
        private static final long serialVersionUID = 1L;
        private Map<String, Product> products = new LinkedHashMap<>();
        private Map<String, Customer> customers = new LinkedHashMap<>();
        private Map<String, Order> orders = new LinkedHashMap<>();

        public void addProduct(Product p) { products.put(p.getId(), p); }
        public void addCustomer(Customer c) { customers.put(c.getId(), c); }

        public Product findProduct(String id) { return products.get(id); }
        public Customer findCustomer(String id) { return customers.get(id); }
        public Order findOrder(String id) { return orders.get(id); }

        public Order createOrder(String orderId, String customerId) {
            Customer c = findCustomer(customerId);
            if (c == null) throw new IllegalArgumentException("Customer not found: " + customerId);
            Order o = new Order(orderId, c);
            orders.put(orderId, o);
            return o;
        }

        public void saveToFile(String path) throws IOException {
            File f = new File(path);
            f.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(this);
            }
        }

        public static Bookshop loadFromFile(String path) throws IOException, ClassNotFoundException {
            File f = new File(path);
            if (!f.exists()) return null;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                return (Bookshop) ois.readObject();
            }
        }
    }

    private static Bookshop shop;

    private static void seedSampleData(Bookshop s) {
        if (s.findProduct("B001") == null) s.addProduct(new Book("B001", "The Java Handbook", 29.95, "A. Author", 450));
        if (s.findProduct("B002") == null) s.addProduct(new Book("B002", "Design Patterns", 39.50, "G. Gamma", 395));
        if (s.findProduct("M001") == null) s.addProduct(new Magazine("M001", "Time", 5.99, 202));
        if (s.findProduct("C001") == null) s.addProduct(new CD("C001", "Greatest Hits", 12.99, "Famous Band", 14));

        if (s.findCustomer("CUST1") == null) s.addCustomer(new Customer("CUST1", "Alice", "+1-555-0100"));
        if (s.findCustomer("CUST2") == null) s.addCustomer(new Customer("CUST2", "Bob", "+1-555-0200"));
    }

    private static void interactiveCLI(Scanner in) {
        String dataPath = "data/bookshop.dat";
        shop = new Bookshop();
        seedSampleData(shop);

        while (true) {
            System.out.println("\nBookshop Menu:\n1) List products\n2) List customers\n3) Add customer\n4) Create order\n5) Add item to order\n6) View order\n7) Mark order delivered\n8) Save state\n9) Load state\n0) Exit");
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        shop.products.values().forEach(p -> System.out.println(" - " + p.toString()));
                        break;
                    case "2":
                        shop.customers.values().forEach(c -> System.out.println(" - " + c.toString()));
                        break;
                    case "3":
                        System.out.print("Customer id: ");
                        String cid = in.nextLine().trim();
                        System.out.print("Name: ");
                        String name = in.nextLine().trim();
                        System.out.print("Contact: ");
                        String contact = in.nextLine().trim();
                        shop.addCustomer(new Customer(cid, name, contact));
                        System.out.println("Customer added.");
                        break;
                    case "4":
                        System.out.print("Order id: ");
                        String oid = in.nextLine().trim();
                        System.out.print("Customer id: ");
                        String ocid = in.nextLine().trim();
                        if (shop.findCustomer(ocid) == null) {
                            System.out.println("Customer not found.");
                            break;
                        }
                        shop.createOrder(oid, ocid);
                        System.out.println("Order created.");
                        break;
                    case "5":
                        System.out.print("Order id: ");
                        String oadd = in.nextLine().trim();
                        Order o = shop.findOrder(oadd);
                        if (o == null) { System.out.println("Order not found."); break; }
                        System.out.print("Product id: ");
                        String pid = in.nextLine().trim();
                        Product p = shop.findProduct(pid);
                        if (p == null) { System.out.println("Product not found."); break; }
                        System.out.print("Quantity: ");
                        int q = Integer.parseInt(in.nextLine().trim());
                        o.addItem(p, q);
                        System.out.println("Item added.");
                        break;
                    case "6":
                        System.out.print("Order id: ");
                        String ov = in.nextLine().trim();
                        Order ovw = shop.findOrder(ov);
                        if (ovw == null) { System.out.println("Order not found."); break; }
                        ovw.printInvoice();
                        break;
                    case "7":
                        System.out.print("Order id: ");
                        String od = in.nextLine().trim();
                        Order odr = shop.findOrder(od);
                        if (odr == null) { System.out.println("Order not found."); break; }
                        odr.markDelivered();
                        System.out.println("Order marked delivered.");
                        break;
                    case "8":
                        try { shop.saveToFile(dataPath); System.out.println("Saved to " + dataPath); } catch (Exception ex) { System.out.println("Save failed: " + ex.getMessage()); }
                        break;
                    case "9":
                        try {
                            Bookshop loaded = Bookshop.loadFromFile(dataPath);
                            if (loaded != null) { shop = loaded; System.out.println("Loaded from " + dataPath); } else System.out.println("No saved data found.");
                        } catch (Exception ex) { System.out.println("Load failed: " + ex.getMessage()); }
                        break;
                    case "0":
                        System.out.println("Goodbye.");
                        return;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void demoRun() {
        shop = new Bookshop();
        seedSampleData(shop);

        Order order1 = shop.createOrder("ORD1001", "CUST1");
        order1.addItem(shop.findProduct("B001"), 1);
        order1.addItem(shop.findProduct("C001"), 2);

        Order order2 = shop.createOrder("ORD1002", "CUST2");
        order2.addItem(shop.findProduct("B002"), 1);
        order2.addItem(shop.findProduct("M001"), 3);
        order2.markDelivered();

        System.out.println("Sample Bookshop Demo Output\n");
        order1.printInvoice();
        order2.printInvoice();

        System.out.println("Available products:");
        shop.products.values().forEach(p -> System.out.println(" - " + p.toString()));
    }

    public static void main(String[] args) {
        if (args.length > 0 && "demo".equalsIgnoreCase(args[0])) {
            demoRun();
            return;
        }

        Scanner in = new Scanner(System.in);
        interactiveCLI(in);
        in.close();
    }
}
