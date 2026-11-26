package com.lunch.order.service;

import com.lunch.auth.model.User;
import com.lunch.auth.repository.UserRepository;
import com.lunch.menu.model.Allergen;
import com.lunch.menu.model.Dish;
import com.lunch.menu.repository.AllergenRepository;
import com.lunch.menu.repository.DishRepository;
import com.lunch.order.model.*;
import com.lunch.order.repository.GroupOrderRepository;
import com.lunch.order.repository.OrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupOrderService {

    public static final String ADDRESS_PLACEHOLDER = "—";

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    private final GroupOrderRepository groupOrderRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final AllergenRepository allergenRepository;
    private final PriceCalculationService priceCalculationService;

    public GroupOrderService(GroupOrderRepository groupOrderRepository, OrderRepository orderRepository,
                             UserRepository userRepository, DishRepository dishRepository,
                             AllergenRepository allergenRepository,
                             PriceCalculationService priceCalculationService) {
        this.groupOrderRepository = groupOrderRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
        this.allergenRepository = allergenRepository;
        this.priceCalculationService = priceCalculationService;
    }

    // ---------- менеджер збору ----------

    @Transactional
    public GroupOrder createRoom(String title, String collectorEmail) {
        User user = userRepository.findByEmail(collectorEmail).orElseThrow();

        GroupOrder group = new GroupOrder();
        group.setCollectorId(user.getId());
        group.setTitle(title.trim());
        group.setRoomCode(generateCode());
        group.setStatus(GroupOrderStatus.OPEN);
        group = groupOrderRepository.save(group);

        // Замовлення створюється одразу: позиції учасників додаються в нього до закриття збору
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGroupOrderId(group.getId());
        order.setStatus(OrderStatus.COLLECTING);
        order.setDeliveryAddress(user.getAddress() != null && !user.getAddress().isBlank()
                ? user.getAddress() : ADDRESS_PLACEHOLDER);
        orderRepository.save(order);

        return group;
    }

    @Transactional(readOnly = true)
    public List<GroupOrder> getRooms(String collectorEmail) {
        User user = userRepository.findByEmail(collectorEmail).orElseThrow();
        return groupOrderRepository.findByCollectorIdOrderByIdDesc(user.getId());
    }

    /** Кімната, до якої має доступ лише її власник. */
    @Transactional(readOnly = true)
    public GroupOrder getRoomForCollector(Long id, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        GroupOrder group = groupOrderRepository.findById(id).orElseThrow();
        if (!group.getCollectorId().equals(user.getId())) {
            throw new AccessDeniedException("Немає доступу до цієї кімнати");
        }
        return group;
    }

    @Transactional
    public Order closeRoom(Long id, String email, CloseGroupForm form) {
        GroupOrder group = getRoomForCollector(id, email);
        if (!group.isOpen()) {
            throw new IllegalStateException("Збір уже закрито");
        }
        Order order = getOrder(group);
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("У кімнаті немає жодної позиції");
        }

        order.setDeliveryAddress(form.getDeliveryAddress().trim());
        order.setComment(form.getComment() == null || form.getComment().isBlank()
                ? null : form.getComment().trim());
        recalculate(order);
        order.setStatus(OrderStatus.NEW);
        group.setStatus(GroupOrderStatus.CLOSED);

        orderRepository.save(order);
        groupOrderRepository.save(group);
        return order;
    }

    // ---------- учасники ----------

    @Transactional(readOnly = true)
    public GroupOrder getRoomByCode(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase();
        return groupOrderRepository.findByRoomCode(normalized).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Order getOrder(GroupOrder group) {
        return orderRepository.findByGroupOrderId(group.getId()).orElseThrow();
    }

    /** Додає позицію. Повертає список алергенів учасника, які містить страва (може бути порожнім). */
    @Transactional
    public List<String> addItem(String code, ParticipantItemForm form) {
        GroupOrder group = getRoomByCode(code);
        if (!group.isOpen()) {
            throw new RoomClosedException();
        }
        Order order = getOrder(group);

        Dish dish = dishRepository.findById(form.getDishId()).orElseThrow();
        if (dish.isArchived() || !dish.isAvailable()) {
            throw new DishUnavailableException("Страва «" + dish.getName() + "» зараз недоступна");
        }

        List<Allergen> allergens = form.getAllergenIds() == null || form.getAllergenIds().isEmpty()
                ? List.of() : allergenRepository.findAllById(form.getAllergenIds());
        String allergenText = allergens.stream()
                .map(Allergen::getName).sorted().collect(Collectors.joining(", "));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setDish(dish);
        item.setQuantity(form.getQuantity());
        item.setUnitPrice(dish.getPrice()); // ціна фіксується на момент додавання
        item.setParticipantName(form.getParticipantName().trim());
        item.setParticipantAllergens(allergenText.isEmpty() ? null : allergenText);
        item.setParticipantWishes(form.getWishes() == null || form.getWishes().isBlank()
                ? null : form.getWishes().trim());

        order.getItems().add(item);
        recalculate(order);
        orderRepository.save(order);

        return item.getAllergenConflicts();
    }

    /** Учасник може видалити лише свої позиції, поки збір відкритий. */
    @Transactional
    public void removeItem(String code, Long itemId, String participantName) {
        GroupOrder group = getRoomByCode(code);
        if (!group.isOpen()) {
            throw new RoomClosedException();
        }
        Order order = getOrder(group);
        boolean removed = order.getItems().removeIf(i -> i.getId().equals(itemId)
                && i.getParticipantName() != null
                && i.getParticipantName().equalsIgnoreCase(participantName));
        if (removed) {
            recalculate(order);
            orderRepository.save(order);
        }
    }

    // ---------- допоміжні ----------

    private void recalculate(Order order) {
        double subtotal = priceCalculationService.round2(
                order.getItems().stream().mapToDouble(OrderItem::getLineTotal).sum());
        int totalQuantity = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();

        PriceBreakdown price = priceCalculationService.calculate(subtotal, totalQuantity, true, 0, 0);
        order.setSubtotal(price.getSubtotal());
        order.setDiscount(price.getDiscount());
        order.setBonusUsed(0);
        order.setTotal(price.getTotal());
    }

    public int discountPercent(Order order) {
        int totalQuantity = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        return priceCalculationService.discountPercent(totalQuantity, true);
    }

    private String generateCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (groupOrderRepository.existsByRoomCode(code));
        return code;
    }
}