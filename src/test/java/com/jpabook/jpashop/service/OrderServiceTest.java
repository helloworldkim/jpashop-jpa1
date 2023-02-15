package com.jpabook.jpashop.service;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.domain.exception.NotEnoughStockException;
import com.jpabook.jpashop.domain.item.Book;
import com.jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;


    @Test
    @DisplayName("주문")
    void order() {

        //given
        Member member = createMember();

        em.persist(member);
        Book book = createBook("JPA", 10000, 10);
        em.persist(book);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then

        Order getOrder = orderRepository.findOne(orderId);


        assertEquals(OrderStatus.ORDER, getOrder.getStatus(),  "상품 주문시 상태는 ORDER");
        assertEquals(1, getOrder.getOrderItems().size(),  "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(10000 * orderCount, getOrder.getTotalPrice() ,  "주문 가격은 가격 * 수량 이다");
        assertEquals(8, book.getStockQuantity() ,  "주문수량만큼 재고가 줄어야한다.");
        //

    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrder() {

        //given
        Member member = createMember();
        em.persist(member);
        Book book = createBook("JPA", 10000, 10);
        em.persist(book);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(),  "주문취소시 상태는 CANCEL");
        assertEquals(10, book.getStockQuantity(),"주문이 취소된 상품은 재고가 증가해야한다.");

    }

    @Test
    @DisplayName("주문시 재고수량초과")
    void orderOvertheQuntity() throws Exception {

        //given
        Member member = createMember();
        em.persist(member);
        Book book = createBook("JPA", 10000, 10);
        em.persist(book);

        int orderCount = 11;

        //when

        //then
        assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));

    }

    private static Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        return book;
    }

    private static Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","강가", "123-123"));
        return member;
    }
}