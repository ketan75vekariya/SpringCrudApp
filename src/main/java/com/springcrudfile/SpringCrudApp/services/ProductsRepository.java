package com.springcrudfile.SpringCrudApp.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springcrudfile.SpringCrudApp.model.Products;

public interface ProductsRepository extends JpaRepository<Products, Integer>{

}
