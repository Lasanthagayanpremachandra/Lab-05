import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static abstract class Product {
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

    static class Customer {
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

    static class OrderItem {
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

    static class Order {
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

    static class Bookshop {
        private Map<String, Product> products = new LinkedHashMap<>();
        private Map<String, Customer> customers = new LinkedHashMap<>();
        private Map<String, Order> orders = new LinkedHashMap<>();

        public void addProduct(Product p) { products.put(p.getId(), p); }
        public void addCustomer(Customer c) { customers.put(c.getId(), c); }

        public Product findProduct(String id) { return products.get(id); }
        public Customer findCustomer(String id) { return customers.get(id); }

        public Order createOrder(String orderId, String customerId) {
            Customer c = findCustomer(customerId);
            if (c == null) throw new IllegalArgumentException("Customer not found: " + customerId);
            Order o = new Order(orderId, c);
            orders.put(orderId, o);
            return o;
        }
    }

    public static void main(String[] args) {
        Bookshop shop = new Bookshop();

        // Add sample products
        shop.addProduct(new Book("B001", "The Java Handbook", 29.95, "A. Author", 450));
        shop.addProduct(new Book("B002", "Design Patterns", 39.50, "G. Gamma", 395));
        shop.addProduct(new Magazine("M001", "Time", 5.99, 202));
        shop.addProduct(new CD("C001", "Greatest Hits", 12.99, "Famous Band", 14));

        // Add customers
        shop.addCustomer(new Customer("CUST1", "Alice", "+1-555-0100"));
        shop.addCustomer(new Customer("CUST2", "Bob", "+1-555-0200"));

        // Demo: create an order for Alice
        Order order1 = shop.createOrder("ORD1001", "CUST1");
        order1.addItem(shop.findProduct("B001"), 1);
        order1.addItem(shop.findProduct("C001"), 2);

        // Demo: another order
        Order order2 = shop.createOrder("ORD1002", "CUST2");
        order2.addItem(shop.findProduct("B002"), 1);
        order2.addItem(shop.findProduct("M001"), 3);
        order2.markDelivered();

        // Print invoices
        System.out.println("Sample Bookshop Demo Output\n");
        order1.printInvoice();
        order2.printInvoice();

        System.out.println("Available products:");
        shop.products.values().forEach(p -> System.out.println(" - " + p.toString()));
    }
}
