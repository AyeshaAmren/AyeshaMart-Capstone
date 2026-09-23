package com.ayeshamart.dao;

import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.util.ConnectionManager;
import com.ayeshamart.util.TestDb;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDAOTest {

    private static ProductDAO productDAO;
    private static UserDAO userDAO;
    private static long seller1;
    private static long seller2;
    private long counter;

    @BeforeAll
    static void init() throws Exception {
        ConnectionManager.setDataSource(TestDb.create("productdaotest"));
        productDAO = new ProductDAO();
        userDAO = new UserDAO();
        seller1 = userDAO.create(new User("Seller One", "seller1@example.com", "hash", "SELLER")).getId();
        seller2 = userDAO.create(new User("Seller Two", "seller2@example.com", "hash", "SELLER")).getId();
    }

    @AfterAll
    static void cleanup() {
        ConnectionManager.close();
    }

    @BeforeEach
    void nextCounter() {
        counter = System.nanoTime();
    }

    private Product sampleProduct(long sellerId) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName("Product " + counter + "-" + sellerId);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("125.50"));
        product.setStockQty(7);
        product.setCategory("Fiction");
        return product;
    }

    private Product product(String name, String description, String category, int stock) {
        Product product = new Product();
        product.setSellerId(seller1);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQty(stock);
        product.setCategory(category);
        return product;
    }

    @Test
    void searchFindsByName() throws Exception {
        Product created = productDAO.create(product("SearchUniqueHeadphones", "great sound", "Fiction", 5));

        List<Product> results = productDAO.search("SearchUniqueHeadphones", null);

        assertTrue(results.stream().anyMatch(p -> p.getId() == created.getId()));
        assertEquals("Seller One", results.get(0).getSellerName());
    }

    @Test
    void searchFindsByDescription() throws Exception {
        Product created = productDAO.create(product("PlainName", "exploding cordless vibrato", "Books", 5));

        List<Product> results = productDAO.search("cordless", null);

        assertTrue(results.stream().anyMatch(p -> p.getId() == created.getId()));
    }

    @Test
    void searchIsCaseInsensitiveAndEmptyResultAllowed() throws Exception {
        productDAO.create(product("CaseSensitiveWidget", "desc", "Fiction", 5));

        assertTrue(productDAO.search("casesensitive", null).stream()
                .anyMatch(p -> p.getName().equals("CaseSensitiveWidget")));
        assertTrue(productDAO.search("zzz-no-such-term-zzz", null).isEmpty());
    }

    @Test
    void categoryFilterReturnsOnlyThatCategory() throws Exception {
        Product book = productDAO.create(product("CatBook", "desc", "Books", 5));
        Product gadget = productDAO.create(product("CatGadget", "desc", "Fiction", 5));

        List<Product> results = productDAO.search(null, "Fiction");

        assertTrue(results.stream().anyMatch(p -> p.getId() == gadget.getId()));
        assertTrue(results.stream().noneMatch(p -> p.getId() == book.getId()));
    }

    @Test
    void searchAndCategoryWorkTogether() throws Exception {
        Product target = productDAO.create(product("RobotVacuumX", "home helper", "Thriller", 5));
        productDAO.create(product("RobotVacuumX", "home helper", "Books", 5));
        productDAO.create(product("OtherRobot", "home helper", "Thriller", 5));

        List<Product> results = productDAO.search("RobotVacuumX", "Thriller");

        assertEquals(1, results.size());
        assertEquals(target.getId(), results.get(0).getId());
    }

    @Test
    void outOfStockProductsAreHiddenFromCatalog() throws Exception {
        Product inStock = productDAO.create(product("AvailableItem", "desc", "Fiction", 3));
        productDAO.create(product("OutOfStockItem", "desc", "Fiction", 0));

        List<Product> results = productDAO.search(null, null);

        assertTrue(results.stream().anyMatch(p -> p.getId() == inStock.getId()));
        assertTrue(results.stream().noneMatch(p -> p.getName().equals("OutOfStockItem")));
    }

    @Test
    void findCategoriesReturnsDistinctValues() throws Exception {
        productDAO.create(product("CatA", "desc", "Biography", 5));
        productDAO.create(product("CatB", "desc", "Biography", 5));
        productDAO.create(product("CatC", "desc", "Fantasy", 5));

        List<String> categories = productDAO.findCategories();
        assertTrue(categories.contains("Biography"));
        assertTrue(categories.contains("Fantasy"));
        assertEquals(1, categories.stream().filter(c -> c.equals("Biography")).count());
        assertEquals(1, categories.stream().filter(c -> c.equals("Fantasy")).count());
    }

    @Test
    void createThenFindById() throws Exception {
        Product created = productDAO.create(sampleProduct(seller1));

        Product found = productDAO.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(seller1, found.getSellerId());
        assertEquals(0, new BigDecimal("125.50").compareTo(found.getPrice()));
        assertEquals(7, found.getStockQty());
        assertEquals("Fiction", found.getCategory());
    }

    @Test
    void findBySellerIdReturnsOnlyThatSellersProducts() throws Exception {
        Product mine = productDAO.create(sampleProduct(seller1));
        productDAO.create(sampleProduct(seller2));

        List<Product> products = productDAO.findBySellerId(seller1);
        assertTrue(products.stream().allMatch(p -> p.getSellerId() == seller1));
        assertTrue(products.stream().anyMatch(p -> p.getId() == mine.getId()));
    }

    @Test
    void updateChangesFields() throws Exception {
        Product created = productDAO.create(sampleProduct(seller1));

        Product updated = new Product();
        updated.setName("Renamed Product");
        updated.setDescription("updated");
        updated.setPrice(new BigDecimal("99.99"));
        updated.setStockQty(3);
        updated.setCategory("Books");
        updated.setImageUrl("/img/x.jpg");

        boolean ok = productDAO.update(created.getId(), seller1, updated);
        assertTrue(ok);

        Product found = productDAO.findById(created.getId());
        assertEquals("Renamed Product", found.getName());
        assertEquals(0, new BigDecimal("99.99").compareTo(found.getPrice()));
        assertEquals(3, found.getStockQty());
        assertEquals("Books", found.getCategory());
        assertEquals("/img/x.jpg", found.getImageUrl());
    }

    @Test
    void deleteRemovesRow() throws Exception {
        Product created = productDAO.create(sampleProduct(seller1));

        boolean ok = productDAO.delete(created.getId(), seller1);
        assertTrue(ok);
        assertNull(productDAO.findById(created.getId()));
    }

    @Test
    void deleteWithWrongSellerIsIgnored() throws Exception {
        Product created = productDAO.create(sampleProduct(seller2));

        boolean ok = productDAO.delete(created.getId(), seller1);
        assertFalse(ok);
        assertNotNull(productDAO.findById(created.getId()));
    }
}