package com.lunch.order.service;

import com.lunch.auth.repository.UserRepository;
import com.lunch.auth.service.BonusService;
import com.lunch.order.model.Order;
import com.lunch.order.model.OrderStatus;
import com.lunch.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BonusService bonusService;

    private OrderManagementService service;

    @BeforeEach
    void setUp() {
        service = new OrderManagementService(orderRepository, userRepository, bonusService);
    }

    private Order order(long id, OrderStatus status, Long userId, int bonusUsed) {
        Order o = new Order();
        ReflectionTestUtils.setField(o, "id", id);
        o.setStatus(status);
        o.setUserId(userId);
        o.setBonusUsed(bonusUsed);
        return o;
    }

    @Test
    void accept_movesNewOrderToAccepted() {
        Order o = order(1, OrderStatus.NEW, null, 0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        service.accept(1L);

        assertThat(o.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        verify(orderRepository).save(o);
    }

    @Test
    void accept_rejectsOrderThatIsNotNew() {
        Order o = order(1, OrderStatus.PREPARING, null, 0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> service.accept(1L)).isInstanceOf(IllegalStateException.class);

        assertThat(o.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void reject_refundsUsedBonusesToClient() {
        Order o = order(1, OrderStatus.NEW, 5L, 30);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        service.reject(1L);

        assertThat(o.getStatus()).isEqualTo(OrderStatus.REJECTED);
        verify(bonusService).changeBalance(eq(5L), eq(30), anyString(), eq(1L));
    }

    @Test
    void reject_guestOrderHasNoRefund() {
        Order o = order(1, OrderStatus.NEW, null, 0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        service.reject(1L);

        assertThat(o.getStatus()).isEqualTo(OrderStatus.REJECTED);
        verifyNoInteractions(bonusService);
    }

    @Test
    void reject_withoutUsedBonusesHasNoRefund() {
        Order o = order(1, OrderStatus.NEW, 5L, 0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        service.reject(1L);

        verifyNoInteractions(bonusService);
    }

    @Test
    void reject_cannotBeRepeated_soBonusesAreRefundedOnlyOnce() {
        Order o = order(1, OrderStatus.REJECTED, 5L, 30);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> service.reject(1L)).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(bonusService);
    }

    @Test
    void startPreparing_worksOnlyForAcceptedOrders() {
        Order accepted = order(1, OrderStatus.ACCEPTED, null, 0);
        Order fresh = order(2, OrderStatus.NEW, null, 0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(accepted));
        when(orderRepository.findById(2L)).thenReturn(Optional.of(fresh));

        service.startPreparing(1L);
        assertThat(accepted.getStatus()).isEqualTo(OrderStatus.PREPARING);

        assertThatThrownBy(() -> service.startPreparing(2L)).isInstanceOf(IllegalStateException.class);
        assertThat(fresh.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void findOrders_hidesCollectingAndShowsNewOnesFirst() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order(1, OrderStatus.ACCEPTED, null, 0),
                order(2, OrderStatus.NEW, null, 0),
                order(3, OrderStatus.COLLECTING, null, 0),
                order(4, OrderStatus.NEW, null, 0),
                order(5, OrderStatus.COMPLETED, null, 0)));

        List<Long> ids = service.findOrders().stream().map(Order::getId).toList();

        assertThat(ids).containsExactly(4L, 2L, 5L, 1L);
    }

    @Test
    void getOrder_hidesOrdersThatAreStillCollecting() {
        when(orderRepository.findById(3L))
                .thenReturn(Optional.of(order(3, OrderStatus.COLLECTING, null, 0)));

        assertThatThrownBy(() -> service.getOrder(3L)).isInstanceOf(NoSuchElementException.class);
    }
}