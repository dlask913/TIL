package com.limnj.til.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemService itemServiceImpl;

    @Test
    public void stockIncrementTest(){
        itemServiceImpl.incrementStock(1L);
        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> assertEquals(101L, i.getStock())
        );
    }

    @Test
    public void stockDecrementTest(){
        itemServiceImpl.decrementStock(1L);
        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> assertEquals(99L, i.getStock())
        );
    }

    @Test
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
    }

    @Test
    @DisplayName("동시성 이슈 성공 테스트")
    public void concurrencyIssue_success_Test() throws InterruptedException {
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
    }

    @AfterEach
    public void printStock(){
        Optional<Item> findItem = itemRepository.findById(1L);
        findItem.ifPresent(
                i -> System.out.println("남은 수량: "+i.getStock())
        );
    }
}