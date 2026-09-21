package com.ayeshamart.service;

import com.ayeshamart.dao.CartDAO;
import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.exception.ValidationException;
import com.ayeshamart.model.CartItem;
import com.ayeshamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartDAO, productDAO);
    }

    private Product stocked(long id, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setSellerId(2L);
        product.setStockQty(stock);
        product.setPrice(new BigDecimal("10.00"));
        return product;
    }

    private CartItem cartLine(long productId, int quantity) {
        CartItem item = new CartItem(1L, productId, quantity);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setProductName("Product " + productId);
        return item;
    }

    @Test
    void addInsertsNewRowWhenProductNotInCart() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 8));
        when(cartDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());

        cartService.add(1L, 10L, 3);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartDAO).insert(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(10L, captor.getValue().getProductId());
        assertEquals(3, captor.getValue().getQuantity());
    }

    @Test
    void addIncrementsExistingRowInsteadOfDuplicating() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 8));
        when(cartDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.of(cartLine(10L, 3)));

        cartService.add(1L, 10L, 2);

        verify(cartDAO, never()).insert(any(CartItem.class));
        verify(cartDAO).updateQuantity(1L, 10L, 5);
    }

    @Test
    void addRejectsUnknownProduct() throws Exception {
        when(productDAO.findById(99L)).thenReturn(null);

        assertThrows(ValidationException.class, () -> cartService.add(1L, 99L, 1));
        verifyNoInteractions(cartDAO);
    }

    @Test
    void addRejectsNonPositiveQuantity() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 8));

        assertThrows(ValidationException.class, () -> cartService.add(1L, 10L, 0));
        assertThrows(ValidationException.class, () -> cartService.add(1L, 10L, -2));
        verifyNoInteractions(cartDAO);
    }

    @Test
    void addRejectsQuantityAboveStockIncludingExisting() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 5));
        when(cartDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.of(cartLine(10L, 4)));

        assertThrows(ValidationException.class, () -> cartService.add(1L, 10L, 2));
        verify(cartDAO, never()).insert(any(CartItem.class));
        verify(cartDAO, never()).updateQuantity(anyLong(), anyLong(), any(Integer.class));
    }

    @Test
    void addRejectsProductOutOfStock() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 0));
        when(cartDAO.findByUserAndProduct(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> cartService.add(1L, 10L, 1));
        verify(cartDAO, never()).insert(any(CartItem.class));
    }

    @Test
    void updateQuantitySetsAbsoluteValueAndValidatesStock() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 50));
        when(cartDAO.updateQuantity(1L, 10L, 7)).thenReturn(true);

        cartService.updateQuantity(1L, 10L, 7);

        verify(cartDAO).updateQuantity(1L, 10L, 7);
    }

    @Test
    void updateQuantityRejectsOutOfRangeValues() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 5));

        assertThrows(ValidationException.class, () -> cartService.updateQuantity(1L, 10L, 0));
        assertThrows(ValidationException.class, () -> cartService.updateQuantity(1L, 10L, -1));
        assertThrows(ValidationException.class, () -> cartService.updateQuantity(1L, 10L, 6));
        verify(cartDAO, never()).updateQuantity(anyLong(), anyLong(), any(Integer.class));
    }

    @Test
    void updateQuantityRejectsRowThatIsNotInCart() throws Exception {
        when(productDAO.findById(10L)).thenReturn(stocked(10L, 5));
        when(cartDAO.updateQuantity(1L, 10L, 2)).thenReturn(false);

        assertThrows(ValidationException.class, () -> cartService.updateQuantity(1L, 10L, 2));
    }

    @Test
    void removeDeletesOwnRowOnly() throws Exception {
        when(cartDAO.delete(1L, 10L)).thenReturn(true);

        cartService.remove(1L, 10L);

        verify(cartDAO).delete(1L, 10L);
    }

    @Test
    void removeRejectsRowNotInCart() throws Exception {
        when(cartDAO.delete(1L, 10L)).thenReturn(false);

        assertThrows(ValidationException.class, () -> cartService.remove(1L, 10L));
    }

    @Test
    void cartTotalSumsSubtotals() throws Exception {
        CartItem a = cartLine(10L, 2);
        CartItem b = cartLine(11L, 3);
        when(cartDAO.findByUserId(1L)).thenReturn(List.of(a, b));

        BigDecimal total = cartService.cartTotal(1L);

        assertEquals(0, new BigDecimal("50.00").compareTo(total));
    }

    @Test
    void cartForUsesOnlyTheGivenBuyer() throws Exception {
        when(cartDAO.findByUserId(7L)).thenReturn(List.of(cartLine(10L, 1)));

        List<CartItem> items = cartService.cartFor(7L);

        assertEquals(1, items.size());
        verify(cartDAO).findByUserId(7L);
    }
}