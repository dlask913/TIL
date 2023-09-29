package com.limnj.til.item;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table @Entity @Getter
@NoArgsConstructor
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

    // 테스트 초기 설정 용도
    public void setStock(){
        this.stock = 100;
    }

}
