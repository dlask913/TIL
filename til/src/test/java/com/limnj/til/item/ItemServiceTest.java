package com.limnj.til.item;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 테스트 순서 지정
class ItemServiceTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemService itemServiceImpl;
    @BeforeEach // 각 테스트 시작 전에 stock 값 100으로 초기화
    public void setup(){
        itemServiceImpl.setStock(1L);
    }

    @Test
    @Order(1)
    @DisplayName("재고 증가 성공 테스트")
    public void stockIncrementTest(){
        itemServiceImpl.incrementStock(1L);
        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> assertEquals(101L, i.getStock())
        );
    }

    @Test
    @Order(2)
    @DisplayName("재고 감소 성공 테스트")
    public void stockDecrementTest(){
        itemServiceImpl.decrementStock(1L);
        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> assertEquals(99L, i.getStock())
        );
    }

    @Test
    @Order(3)
    @DisplayName("동시성 이슈 실패 테스트")
    public void concurrencyIssue_fail_Test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32 개의 스레드가 있는 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(threadCount); // latch 가 대기해야하는 스레드 수

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    itemServiceImpl.incrementStock(1L);
                } finally {
                    latch.countDown(); // count down by 1
                }
            });
        }
        latch.await(); // wait until latch counted down to 0

        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> assertNotEquals(200L,i.getStock())
        );
    }

    @Test
    @Order(4)
    @DisplayName("동시성 이슈 성공 테스트")
    public void concurrencyIssue_success_test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 32 개의 스레드가 있는 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(threadCount); // latch 가 대기해야하는 스레드 수

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    itemServiceImpl.decrementStock(1L);
                } finally {
                    latch.countDown(); // count down by 1
                }
            });
        }
        latch.await(); // wait until latch counted down to 0

        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> assertEquals(0L,i.getStock())
        );
    }

    @AfterEach // 각 테스트 끝날 때마다 남은 수량 출력
    public void printStock(){
        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> System.out.println("남은 수량: "+i.getStock())
        );
    }
}