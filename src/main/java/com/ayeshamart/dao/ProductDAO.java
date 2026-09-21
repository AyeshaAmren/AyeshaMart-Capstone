package com.ayeshamart.dao;

import com.ayeshamart.model.Product;
import com.ayeshamart.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data access for the products table. All SQL uses PreparedStatement.
 * UPDATE and DELETE bind the seller_id as well so a seller can only
 * touch their own rows at the database level too.
 * Every read joins the seller name so buyer-facing pages can show who
 * sells each product.
 */
public class ProductDAO {

    private static final String INSERT =
            "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String COLUMNS =
            "SELECT p.id, p.seller_id, p.name, p.description, p.price, p.stock_qty, p.category, p.image_url, "
                    + "p.created_at, u.name AS seller_name "
                    + "FROM products p JOIN users u ON u.id = p.seller_id ";
    private static final String SELECT_BY_ID =
            COLUMNS + "WHERE p.id = ?";
    private static final String SELECT_BY_SELLER =
            COLUMNS + "WHERE p.seller_id = ? ORDER BY p.created_at DESC, p.id DESC";
    private static final String UPDATE =
            "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, image_url = ? "
                    + "WHERE id = ? AND seller_id = ?";
    private static final String DELETE =
            "DELETE FROM products WHERE id = ? AND seller_id = ?";

    public Product create(Product product) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, product.getSellerId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStockQty());
            statement.setString(6, product.getCategory());
            statement.setString(7, product.getImageUrl());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }
            return product;
        }
    }

    public Product findById(long id) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            return findById(connection, id);
        }
    }

    /** Reads one product on a caller-managed (transactional) connection. */
    public Product findById(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    /**
     * Atomically removes quantity from a product's stock. The WHERE clause
     * re-checks stock_qty >= quantity so a checkout can never oversell, even
     * if the cart was read moments earlier. Returns false when there is not
     * enough stock (no row updated), leaving the row untouched.
     */
    public boolean decreaseStock(Connection connection, long productId, int quantity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE products SET stock_qty = stock_qty - ? "
                        + "WHERE id = ? AND stock_qty >= ?")) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Product> findBySellerId(long sellerId) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_SELLER)) {
            statement.setLong(1, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
            }
        }
        return products;
    }

    /**
     * Buyer catalog: searches by name and/or description and optionally
     * narrows by exact category. Only in-stock products are shown.
     *
     * <p>The WHERE clause is assembled from fixed SQL fragments, never from
     * user input; every user-supplied value (search terms, category) is
     * bound through PreparedStatement parameters, so it is SQL-injection
     * safe.
     */
    public List<Product> search(String query, String category) throws SQLException {
        String q = query == null ? null : query.trim();
        String cat = category == null ? null : category.trim();
        boolean hasQuery = q != null && !q.isEmpty();
        boolean hasCategory = cat != null && !cat.isEmpty();

        StringBuilder sql = new StringBuilder(COLUMNS)
                .append("WHERE p.stock_qty > 0");
        List<String> params = new ArrayList<>();

        if (hasQuery) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(COALESCE(p.description, '')) LIKE ?)");
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            params.add(like);
            params.add(like);
        }
        if (hasCategory) {
            sql.append(" AND p.category = ?");
            params.add(cat);
        }
        sql.append(" ORDER BY p.created_at DESC, p.id DESC");

        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
            }
        }
        return products;
    }

    /** Distinct non-null categories present in the products table. */
    public List<String> findCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DISTINCT p.category FROM products p "
                             + "WHERE p.category IS NOT NULL ORDER BY p.category")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(resultSet.getString(1));
                }
            }
        }
        return categories;
    }

    public boolean update(long productId, long sellerId, Product product) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQty());
            statement.setString(5, product.getCategory());
            statement.setString(6, product.getImageUrl());
            statement.setLong(7, productId);
            statement.setLong(8, sellerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(long productId, long sellerId) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, productId);
            statement.setLong(2, sellerId);
            return statement.executeUpdate() > 0;
        }
    }

    private Product mapRow(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getLong("id"));
        product.setSellerId(resultSet.getLong("seller_id"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setStockQty(resultSet.getInt("stock_qty"));
        product.setCategory(resultSet.getString("category"));
        product.setImageUrl(resultSet.getString("image_url"));
        product.setSellerName(resultSet.getString("seller_name"));
        product.setCreatedAt(resultSet.getObject("created_at", LocalDateTime.class));
        return product;
    }
}