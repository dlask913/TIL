package com.limnj.til.item;

import org.springframework.stereotype.Service;

@Service
public interface ItemService {

    void incrementStock(Long itemId);
    void decrementStock(Long itemId);
}
