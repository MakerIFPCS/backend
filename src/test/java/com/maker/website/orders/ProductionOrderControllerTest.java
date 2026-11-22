package com.maker.website.orders;

import com.maker.website.orders.dtos.ProductionOrderResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionOrderControllerTest {

    @Mock
    private ProductionOrderService productionOrderService;

    @InjectMocks
    private ProductionOrderController productionOrderController;

    @Test
    void requesterListingReturnsOnlyTheirOrders() {
        List<ProductionOrderResponseDTO> ownOrders = List.of();
        var authentication = new UsernamePasswordAuthenticationToken(
                12L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(productionOrderService.getMyOrders(12L)).thenReturn(ownOrders);

        var response = productionOrderController.getOrders(authentication);

        assertEquals(ownOrders, response.getBody());
        verify(productionOrderService).getMyOrders(12L);
        verifyNoMoreInteractions(productionOrderService);
    }

    @Test
    void administratorListingReturnsAllOrders() {
        List<ProductionOrderResponseDTO> allOrders = List.of();
        var authentication = new UsernamePasswordAuthenticationToken(
                99L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(productionOrderService.getAllOrders()).thenReturn(allOrders);

        var response = productionOrderController.getOrders(authentication);

        assertEquals(allOrders, response.getBody());
        verify(productionOrderService).getAllOrders();
        verifyNoMoreInteractions(productionOrderService);
    }
}
