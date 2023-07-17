package com.amangarg4804.springbootpractice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Builder
@Getter
@Setter
public class Order {
    private String quantity;
    private String itemName;
}
