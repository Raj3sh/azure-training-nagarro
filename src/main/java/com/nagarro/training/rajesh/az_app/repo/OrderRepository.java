package com.nagarro.training.rajesh.az_app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nagarro.training.rajesh.az_app.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

}