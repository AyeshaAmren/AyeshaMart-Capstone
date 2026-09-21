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
        product.setCategory("Electronics");
        return product;
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
        assertEquals("Electronics", found.getCategory());
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