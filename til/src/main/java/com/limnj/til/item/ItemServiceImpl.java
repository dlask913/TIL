package com.limnj.til.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        item.ifPresent(Item::increment);
    }
    @Override
    public void decrementStock(Long itemId) {
        /* 동시성 이슈를 고려한 synchronized 사용 */
        synchronized (this){
            Optional<Item> item = itemRepository.findById(itemId);
            item.ifPresent(
                    i ->{
                        i.decrement();
                        itemRepository.saveAndFlush(i);
                    }
            );
        }
    }
}
