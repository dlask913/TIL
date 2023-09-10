package com.limnj.til.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemServiceImpl;

    @GetMapping("/item/{itemId}/increment")
    public void incrementItem(@PathVariable("itemId") Long itemId){
        itemServiceImpl.incrementStock(itemId);
    }

    @GetMapping("/item/{itemId}/decrement")
    public void decrementItem(@PathVariable("itemId") Long itemId){
        itemServiceImpl.decrementStock(itemId);
    }
}
