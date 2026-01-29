package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.DTO.RequestDTO.CustomerRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.CustomerResponse;
import com.sjmt.SJMT.Entity.CustomerEntity;
import com.sjmt.SJMT.Entity.CustomerStatusEnum;
import com.sjmt.SJMT.Repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToResponse).collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Integer id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return convertToResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByCustomerEmail(request.getCustomerEmail())) {
            throw new RuntimeException("Customer email already exists");
        }
        CustomerEntity customer = new CustomerEntity();
        updateEntityFromRequest(customer, request);
        customer.setStatus(CustomerStatusEnum.WHITELISTED); // Requirement: Default is Whitelisted
        
        CustomerEntity saved = customerRepository.save(customer);
        logger.info("New customer created: {}", saved.getCustomerName());
        return convertToResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer id, CustomerRequest request) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        updateEntityFromRequest(customer, request);
        return convertToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void softDeleteCustomer(Integer id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        customer.setStatus(CustomerStatusEnum.DELETED); // Requirement: Delete is soft delete
        customerRepository.save(customer);
        logger.info("Customer with ID {} has been blacklisted", id);
    }

    /**
     * Toggle Customer Status between WHITELISTED and BLACKLISTED
     */
    @Transactional
    public CustomerResponse toggleCustomerStatus(Integer id) {
        logger.info("Toggling status for customer ID: {}", id);
        
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        // Toggle logic
        if (customer.getStatus() == CustomerStatusEnum.WHITELISTED) {
            customer.setStatus(CustomerStatusEnum.BLACKLISTED);
        } else {
            customer.setStatus(CustomerStatusEnum.WHITELISTED);
        }

        CustomerEntity updatedCustomer = customerRepository.save(customer);
        logger.info("Customer ID: {} status changed to: {}", id, updatedCustomer.getStatus());
        
        return convertToResponse(updatedCustomer);
    }

    private void updateEntityFromRequest(CustomerEntity entity, CustomerRequest request) {
        entity.setCustomerName(request.getCustomerName());
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setGstNumber(request.getGstNumber());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setPincode(request.getPincode());
    }

    private CustomerResponse convertToResponse(CustomerEntity entity) {
        CustomerResponse res = new CustomerResponse();
        res.setCustomerId(entity.getCustomerId());
        res.setCustomerName(entity.getCustomerName());
        res.setCustomerEmail(entity.getCustomerEmail());
        res.setPhoneNumber(entity.getPhoneNumber());
        res.setGstNumber(entity.getGstNumber());
        res.setAddress(entity.getAddress());
        res.setCity(entity.getCity());
        res.setState(entity.getState());
        res.setPincode(entity.getPincode());
        res.setCreatedAt(entity.getCreatedAt());
        res.setStatus(entity.getStatus());
        return res;
    }
}