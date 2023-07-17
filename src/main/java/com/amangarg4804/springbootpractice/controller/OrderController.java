package com.amangarg4804.springbootpractice.controller;

import com.amangarg4804.springbootpractice.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OrderController {
    
    @Value("${order.maxquantity}")
    private int maxQuantity; // if no value specified -> exception->  Could not resolve placeholder 'order.maxquantity

    @Value("${order.commonname}")
    private String commonName;// if no value specified, exception regardless of String -> Could not resolve placeholder 'order.commonname

    //1. works without any request mapping and without context-path
    // Doesn't work with when context-path is set. returns 405 status with method not allowed
    @PostMapping
    public Order createOrder1(@RequestBody Order orderRequest) {
        return Order.builder() // new keyword not to be used when using lombok builder
                .quantity(orderRequest.getQuantity())
                .itemName(orderRequest.getItemName())
                .build();
    }

    // After adding /order mapping. API works
    @PostMapping("/order")
    public Order createOrder2(@RequestBody Order orderRequest) {
        log.info("****max quantity**** {}", maxQuantity);
        log.info("****Common name**** {}", commonName);
        return Order.builder() // new keyword not to be used when using lombok builder
                .quantity(orderRequest.getQuantity())
                .itemName(orderRequest.getItemName())
                .build();
    }
}
