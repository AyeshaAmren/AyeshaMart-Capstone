package com.ayeshamart.service;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dto.ProductForm;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productDAO);
    }

    private ProductForm validForm() {
        return new ProductForm("Wireless Keyboard", "Nice keyboard", "750.00", "12", "Electronics", null);
    }

    private Product ownedProduct(long sellerId) {
        Product product = new Product();
        product.setId(10);
        product.setSellerId(sellerId);
        product.setName("Existing Product");
        product.setPrice(new BigDecimal("100.00"));
        return product;
    }

    @Test
    void sellerCreatesProduct() throws Exception {
        when(productDAO.create(any())).thenAnswer(inv -> inv.getArgument(0));

        Product created = productService.create(1L, validForm());

        assertEquals(1L, created.getSellerId());
        assertEquals("Wireless Keyboard", created.getName());
        assertEquals(0, new BigDecimal("750.00").compareTo(created.getPrice()));
        assertEquals(12, created.getStockQty());
    }

    @Test
    void rejectsEmptyName() throws Exception {
        ProductForm form = new ProductForm("", "d", "10.00", "1", "Books", null);
        assertThrows(ValidationException.class, () -> productService.create(1L, form));
        verify(productDAO, never()).create(any());
    }

    @Test
    void rejectsInvalidPrice() throws Exception {
        assertThrows(ValidationException.class, () -> productService.create(1L,
                new ProductForm("X", "d", "abc", "1", "Books", null)));
        assertThrows(ValidationException.class, () -> productService.create(1L,
                new ProductForm("X", "d", "-5", "1", "Books", null)));
        verify(productDAO, never()).create(any());
    }

    @Test
    void rejectsInvalidStock() {
        assertThrows(ValidationException.class, () -> productService.create(1L,
                new ProductForm("X", "d", "10.00", "seven", "Books", null)));
        assertThrows(ValidationException.class, () -> productService.create(1L,
                new ProductForm("X", "d", "10.00", "-2", "Books", null)));
    }

    @Test
    void rejectsBadImageUrl() {
        assertThrows(ValidationException.class, () -> productService.create(1L,
                new ProductForm("X", "d", "10.00", "1", "Books", "not a url")));
    }

    @Test
    void sellerUpdatesOwnProduct() throws Exception {
        when(productDAO.findById(10L)).thenReturn(ownedProduct(1L));
        when(productDAO.update(anyLong(), anyLong(), any())).thenReturn(true);

        productService.update(1L, 10L, validForm());

        verify(productDAO).update(eq(10L), eq(1L), any(Product.class));
    }

    @Test
    void sellerCannotUpdateAnotherSellersProduct() throws Exception {
        when(productDAO.findById(10L)).thenReturn(ownedProduct(2L));

        assertThrows(ValidationException.class, () -> productService.update(1L, 10L, validForm()));
        verify(productDAO, never()).update(anyLong(), anyLong(), any());
    }

    @Test
    void sellerDeletesOwnProduct() throws Exception {
        when(productDAO.findById(10L)).thenReturn(ownedProduct(1L));
        when(productDAO.delete(10L, 1L)).thenReturn(true);

        productService.delete(1L, 10L);

        verify(productDAO).delete(10L, 1L);
    }

    @Test
    void sellerCannotDeleteAnotherSellersProduct() throws Exception {
        when(productDAO.findById(10L)).thenReturn(ownedProduct(2L));

        assertThrows(ValidationException.class, () -> productService.delete(1L, 10L));
        verify(productDAO, never()).delete(anyLong(), anyLong());
    }

    @Test
    void findOwnedBySellerRejectsMissingProduct() throws Exception {
        when(productDAO.findById(99L)).thenReturn(null);

        assertThrows(ValidationException.class, () -> productService.findOwnedBySeller(1L, 99L));
    }

    @Test
    void findBySellerDelegates() throws Exception {
        when(productDAO.findBySellerId(1L)).thenReturn(List.of(ownedProduct(1L)));

        assertTrue(productService.findBySeller(1L).size() == 1);
        verify(productDAO).findBySellerId(1L);
    }
}