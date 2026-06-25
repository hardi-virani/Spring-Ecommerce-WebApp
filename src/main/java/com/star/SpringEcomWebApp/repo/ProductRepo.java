package com.star.SpringEcomWebApp.repo;

import com.star.SpringEcomWebApp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo  extends JpaRepository<Product, Integer> {
}
