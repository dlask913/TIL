package com.limnj.til.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService{

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void incrementStock(Long itemId) {
        /* 데이터 동기화 불가 */
        Optional<Item> item = itemRepository.findById(itemId);
        item.ifPresent(
                i ->{
                    i.increment();
                    itemRepository.saveAndFlush(item.get());
                }
        );
    }
    @Override
    public synchronized void decrementStock(Long itemId) {
        /* 동시성 이슈를 고려한 synchronized 사용 */
        Optional<Item> item = itemRepository.findById(itemId);
        item.ifPresent(
                i ->{
                    i.decrement();
                    itemRepository.saveAndFlush(i);
                }
        );
    }

    @Override
    public void setStock(Long id){ // 테스트 용도로, stock 값 초기화
        itemRepository.findById(id).ifPresent(
                i ->{
                    i.setStock();
                    itemRepository.saveAndFlush(i);
                }
        );
    }
}
