package com.mycompany.hosted.checkoutFlow.jpa;


import javax.persistence.EntityManager;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.ShipAddress;
import com.mycompany.hosted.model.order.OrderPayment;
import com.mycompany.hosted.checkoutFlow.jpa.OrderNotFoundException;

@Service
@Repository
public class CustomerJpaImpl implements CustomerJpa {
	
	private boolean testOrderException = true;
	
	@PersistenceContext(unitName="springMvcSample")
	private EntityManager em;
	
	@Transactional(readOnly=true)
	public Customer findCustomer(Integer id) throws CustomerNotFoundException {		
		
		
		Customer customer = em.find(Customer.class, id);
		
		if(customer == null)
			throw new CustomerNotFoundException("Customer with id '" + id + "' cannot be found.");
		
				
		return customer;		
		
	}
	
	@Transactional
	public void insertCustomer(Customer customer) {
		
		em.persist(customer);
	}
	
	@Transactional
	public void editCustomer(Customer customer) {
		
		em.merge(customer);
	}

	@Override
	@Transactional
	public Customer updateCustomer(Customer customer) {
		
		Customer updated = null;
		
		if(customer.getId() == null) {
			
		   em.persist(customer);
		   
		   updated = customer;
		
		} else {
			
			updated = em.merge(customer);
			
		}		  
		
		return updated;
		
	}

	@Override
	@Transactional
	public Customer updateShipAddress(Customer customer, ShipAddress shipAddress) {
		
		if(shipAddress.getId() == null)
		
		   customer.addShipAddress(shipAddress);
		
		else {
			customer.editShipAddress(shipAddress);
		}
		
		Customer updated = em.merge(customer);
		
		System.out.println("CustomerJpa#updateShipAddress: customer="
				+ customer.toString() + " updated="
				+ updated.toString());
		
		return updated;	
		
	}
	
	@Transactional
	public OrderPayment saveOrder(OrderPayment order) throws Exception {
		
		System.out.println("CustomerJpa#saveOrder: executing");
		
		if(testOrderException) {
			
			testOrderException = false;
			
			throw new Exception("Testing saveOrder exception.");
		}
		
		try {
		   em.persist(order);
		} catch (PersistenceException e) {
			System.out.println("CustomerJpa#saveOrder: PersistenceException: "
					+ e.getMessage());
			throw e;
		} catch (Exception e) {
			System.out.println("CustomerJpa#saveOrder: Exception "
					+ e.getMessage());
			throw e;
		}
		System.out.println("CustomerJpa#saveOrder: orderId=" + order.getOrderId());
		
		return findOrderPayment(order.getOrderId());
		
	}
	
	@Transactional
	public OrderPayment findOrderPayment(Integer id) throws Exception {
		
		OrderPayment order = null;
		
		try {
			
		    order = em.find(OrderPayment.class, id);
		    
		} catch (Exception e) {
			System.out.println("CustomerJpa#findOrderPayment: Exception: "
					+ e.getMessage());
			throw e;
		}
		
		if(order == null)
			throw new OrderNotFoundException("OrderPayment not found: #" + id);
		
		//Fill in Object fields. Calling the accessor assigns the field.
		
		order.getLineItemPayments().size();
		
		order.getCustomerId().getId();
		
		order.getOrderShipTo().getId();
		
		return order;
	}
	
	@Transactional
	public OrderPayment updateOrderStatus(OrderPayment order) throws PersistenceException {	
		
		    System.out.println("CustomerJpaImpl#updateOrderStatus executing");
		
			em.merge(order);
			
			return order;
		
	}
	
	/*private Map createOrderGraph () {
		
		Map<String, Object> hints = new HashMap<>();
		
		EntityGraph graph = em.createEntityGraph(Order.class)
		
		return hints;
		
	}*/

}
