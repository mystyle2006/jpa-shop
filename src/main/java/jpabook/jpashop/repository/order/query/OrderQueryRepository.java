package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(int offset, int limit) {
        List<OrderQueryDto> result = findOrders(offset, limit);
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                "from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class
        ).setParameter("orderId", orderId).getResultList();
    }


    public List<OrderQueryDto> findOrders(int offset, int limit) {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimization(int offset, int limit) {
        List<OrderQueryDto> orders = findOrders(offset, limit);
        List<Long> orderIds = orders.stream().map(o -> o.getOrderId()).collect(Collectors.toList());
        List<OrderItemQueryDto> orderItems = em.createQuery("select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                "from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds",
                        OrderItemQueryDto.class
                )
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        orders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return orders;
    }
}
