package com.limnj.til.item;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table @Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;
    private int stock;

    public void increment(){
        this.stock ++;
    }
    public void decrement(){
        this.stock --;
    }

    @Builder
    public Item(Long id, int stock) {
        this.id = id;
        this.stock = stock;
    }
}
