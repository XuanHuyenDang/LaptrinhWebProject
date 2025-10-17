package vn.flower.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.flower.entities.Order;
import vn.flower.repositories.AdminOrderRepository;

@Service
public class AdminOrderService {
    @Autowired
    private AdminOrderRepository orderRepository;

    @Transactional
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
    public long countByStatus(String status) {
        return orderRepository.findByStatus(status).size();
    }
    public List<Order> getOrdersByCustomerId(Integer customerId) {
        return orderRepository.findByAccountId(customerId);
    }
    public List<Order> getOrdersByCustomerName(String name) {
        return orderRepository.findByCustomerName(name);
    }
}
