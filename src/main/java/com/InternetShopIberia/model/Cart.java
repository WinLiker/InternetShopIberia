package com.InternetShopIberia.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "USER_CART")
@Setter
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    @OneToMany
    @JoinColumn(name = "product_id", unique = false)
    private List<Product> products;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void addProduct(Product product){
        this.products.add(product);
    }
}
