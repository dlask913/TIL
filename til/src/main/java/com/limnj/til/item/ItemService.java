package com.limnj.til.item;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ItemService {

    void incrementStock(Long itemId);
    void decrementStock(Long itemId);
    void setStock(Long itemId);
}
