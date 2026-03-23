package com.sjmt.SJMT.Service;

import com.sjmt.SJMT.Exception.ResourceNotFoundException;
import com.sjmt.SJMT.Exception.DuplicateResourceException;
import com.sjmt.SJMT.DTO.RequestDTO.CustomerRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.CustomerResponse;
import com.sjmt.SJMT.DTO.ResponseDTO.PaginatedResponse;
import com.sjmt.SJMT.Entity.CustomerEntity;
import com.sjmt.SJMT.Entity.CustomerStatusEnum;
import com.sjmt.SJMT.Entity.BillStatus;
import com.sjmt.SJMT.Entity.CustomerBillEntity;
import com.sjmt.SJMT.Entity.QuotationEntity;
import com.sjmt.SJMT.Entity.QuotationStatus;
import com.sjmt.SJMT.Repository.CustomerBillRepository;
import com.sjmt.SJMT.Repository.CustomerRepository;
import com.sjmt.SJMT.Repository.QuotationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private QuotationRepository quotationRepository;
    @Autowired
    private CustomerBillRepository customerBillRepository;

    public List<CustomerResponse> getAllCustomers() {
        // MEDIUM-11: Exclude soft-deleted customers from standard list views
        return customerRepository.findByStatusNot(CustomerStatusEnum.DELETED).stream()
                .map(this::convertToResponse).collect(Collectors.toList());
    }

    public PaginatedResponse<CustomerResponse> getAllCustomers(int page, int size) {
        // MEDIUM-11: Exclude soft-deleted customers from standard list views
        Page<CustomerEntity> customerPage = customerRepository.findByStatusNot(
                CustomerStatusEnum.DELETED, PageRequest.of(page, size));
        List<CustomerResponse> content = customerPage.getContent().stream()
                .map(this::convertToResponse).collect(Collectors.toList());
        return PaginatedResponse.from(customerPage, content);
    }

    public CustomerResponse getCustomerById(Integer id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getStatus() == CustomerStatusEnum.DELETED) {
            throw new ResourceNotFoundException("Customer not found");
        }
        return convertToResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByCustomerEmail(request.getCustomerEmail())) {
            throw new DuplicateResourceException("Customer email already exists");
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
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                
        // Check if email is being changed to one that already exists
        if (!customer.getCustomerEmail().equals(request.getCustomerEmail()) &&
            customerRepository.existsByCustomerEmail(request.getCustomerEmail())) {
            throw new DuplicateResourceException("Email already exists for another customer");
        }
        
        updateEntityFromRequest(customer, request);
        return convertToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void softDeleteCustomer(Integer id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setStatus(CustomerStatusEnum.DELETED); // Requirement: Delete is soft delete
        customerRepository.save(customer);
        logger.info("Customer with ID {} has been soft-deleted", id);

        // MEDIUM-12: Auto-cancel DRAFT bills for deleted customer
        cancelDraftBillsForCustomer(id, "customer deleted");
    }

    /**
     * Toggle Customer Status between WHITELISTED and BLACKLISTED
     */
    @Transactional
    public CustomerResponse toggleCustomerStatus(Integer id) {
        logger.info("Toggling status for customer ID: {}", id);
        
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        if (customer.getStatus() == CustomerStatusEnum.DELETED) {
            throw new RuntimeException("Cannot toggle status of a deleted customer");
        }

        // Toggle logic
        if (customer.getStatus() == CustomerStatusEnum.WHITELISTED) {
            customer.setStatus(CustomerStatusEnum.BLACKLISTED);
        } else {
            customer.setStatus(CustomerStatusEnum.WHITELISTED);
        }

        CustomerEntity updatedCustomer = customerRepository.save(customer);
        logger.info("Customer ID: {} status changed to: {}", id, updatedCustomer.getStatus());

        // MEDIUM-1: Auto-cancel DRAFT and SENT quotations when a customer is blacklisted
        if (updatedCustomer.getStatus() == CustomerStatusEnum.BLACKLISTED) {
            List<QuotationEntity> openQuotations = quotationRepository
                    .findByCustomer_CustomerIdAndStatusIn(id,
                            List.of(QuotationStatus.DRAFT, QuotationStatus.SENT));
            for (QuotationEntity q : openQuotations) {
                q.setStatus(QuotationStatus.CANCELLED);
                quotationRepository.save(q);
                logger.info("Quotation {} auto-cancelled due to customer {} blacklisting",
                        q.getQuotationNumber(), id);
            }
            if (!openQuotations.isEmpty()) {
                logger.info("{} open quotation(s) auto-cancelled for blacklisted customer ID {}",
                        openQuotations.size(), id);
            }

            // MEDIUM-12: Also cancel DRAFT bills when customer is blacklisted
            cancelDraftBillsForCustomer(id, "customer blacklisted");
        }

        return convertToResponse(updatedCustomer);
    }

    /** MEDIUM-12: Cancel all DRAFT bills for a customer (e.g. on blacklist/delete) */
    private void cancelDraftBillsForCustomer(Integer customerId, String reason) {
        List<CustomerBillEntity> draftBills = customerBillRepository
                .findByCustomer_CustomerIdAndStatus(customerId, BillStatus.DRAFT);
        for (CustomerBillEntity bill : draftBills) {
            bill.setStatus(BillStatus.CANCELLED);
            customerBillRepository.save(bill);
            logger.info("DRAFT bill {} auto-cancelled — customer {}: {}", bill.getBillNumber(), customerId, reason);
        }
        if (!draftBills.isEmpty()) {
            logger.info("{} DRAFT bill(s) auto-cancelled for customer ID {}", draftBills.size(), customerId);
        }
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
