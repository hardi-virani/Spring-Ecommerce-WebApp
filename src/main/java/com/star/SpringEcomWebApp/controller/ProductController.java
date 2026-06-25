package com.star.SpringEcomWebApp.controller;

import com.star.SpringEcomWebApp.model.Product;
import com.star.SpringEcomWebApp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() { //The reason we have use List<Product> is because we needed the whole list of products
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) { // Here only product is because, we just needed one product amoung that list of products.

        Product product = productService.getProductById(id);

       if(product != null) {
           return new ResponseEntity<>(product, HttpStatus.OK );
       }
       else {
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }

    }
}
