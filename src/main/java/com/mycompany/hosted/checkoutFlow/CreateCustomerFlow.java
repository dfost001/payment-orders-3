package com.mycompany.hosted.checkoutFlow;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;


import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.PostalAddress;
import com.mycompany.hosted.exception_handler.EhrLogger;
import com.mycompany.hosted.checkoutFlow.jpa.CustomerJpa;
import com.mycompany.hosted.checkoutFlow.jpa.CustomerNotFoundException;


@Component
public class CreateCustomerFlow {		
	
	private  final String NUMBER_FMT_BUNDLE =	"loginNumberFormat";
	
	private  final String ID_ERR_BUNDLE = "invalidCustomerId";
			
	@Autowired
	private CustomerJpa customerJpa;
	
	@Autowired
	private ValidationUtil vUtil;
	
	
	public boolean customerInSession(RequestContext ctx){	
		
		
		SharedAttributeMap<Object> sharedSession = ctx.getExternalContext().getSessionMap();
		
		Customer customer = (Customer)sharedSession.get(WebFlowConstants.CUSTOMER_KEY);	
		
		
		if(customer == null)
			return false; 
		
		debugPrintCustomer(customer);
		
		MessageContext utilCtx = vUtil.validate(customer);
		
		if(utilCtx.hasErrorMessages())
			this.throwIllegalArg("",  "customerInSession", utilCtx);
			
		return true;		
			
	}
	
	
	/*
	 * To do: Are we retrieving or inserting.
	 * If inserting, do not validate.
	 *
	 */
	public void customerIntoSession(Customer customer,
			RequestContext ctx) {
		
		if(customer == null) 
			this.throwIllegalArg("Customer has not been added to the session.",					
					 "customerIntoSession",
					 null) ;
			
		
		if(customer.getId() == null || customer.getId().equals(0)) {
			
			this.throwIllegalArg("Customer added to session has an empty Id at "
					 + ctx.getCurrentState().getId(),
					 "customerIntoSession",
					 null) ;
		}		
		
       MessageContext mctx = vUtil.validate((PostalAddress)customer);
		
		if(mctx.hasErrorMessages())
			 this.throwIllegalArg("", "customerIntoSession", mctx); 
		
		SharedAttributeMap<Object> sharedSession = ctx.getExternalContext()
				.getSessionMap(); 
		
		sharedSession.put(WebFlowConstants.CUSTOMER_KEY, customer);
		
	}
	/*
	 * Exit on a cancelled customer insertion
	 */
	public void customerIntoSessionOnCancel(Customer customer, RequestContext ctx,
			MyFlowAttributes myFlowAttrs) {
		
		if(myFlowAttrs.isCustomerInsertion()) {
			myFlowAttrs.setCustomerInsertion(false);
			return;
		}
		
		customerIntoSession(customer,ctx);
		
	}
	
	public Customer processLogin(String id, RequestContext ctx) 
			throws NumberFormatException, CustomerNotFoundException {
		
        Customer customer = null;
        
        MessageContext messageContext = ctx.getMessageContext();
        
        Integer customerId = null;
        
        try {
        	customerId = new Integer(id);
        }
        catch(NumberFormatException ex) {
        	
        	initMessageContext(messageContext,id, this.NUMBER_FMT_BUNDLE);        	
        	
			throw new CustomerNotFoundException("processLogin: " + ex.getMessage());
        }
		
		try {
			
			 customer = customerJpa.findCustomer(customerId);
			
		}
		catch(CustomerNotFoundException ex){
			
			initMessageContext(messageContext, id, this.ID_ERR_BUNDLE);
			
			throw ex;
			
		}
		return customer;
	}
	
	
	
	
	public Customer newCustomer() {
		
		Customer customer = new Customer();
		
		customer.setDtCreated(new Date());
		
		return customer;
	}
	
	/*public Customer cloneCustomer(Customer original){		
		
		Customer cloned = (Customer)CloneUtil.cloneCustomer(original);
		
		return cloned;
	}*/
	
/*	public void createUserCookie(RequestContext ctx, Customer customer) {		
		
		 HttpServletResponse response = (HttpServletResponse)ctx.getExternalContext().getNativeResponse();
		
		 String name = customer.getFirstName() + " " + customer.getLastName();
	        
	        Cookie cookie = new Cookie(MyFlowHandler.CHECKOUT_NAME_COOKIE, name );   	        
	        
	        String path =  ctx.getExternalContext().getContextPath();
	        
	        cookie.setPath(path);
	        
	        cookie.setMaxAge(-1);
	        
	        response.addCookie(cookie);  
	        
	        System.out.println("CreateCustomerFlow#createUserCookie: " + MyFlowHandler.CHECKOUT_NAME_COOKIE);
	     
	}	*/
	
	private void throwIllegalArg(String message, String method, MessageContext ctx) {
		
	     String err = "";
	     
	     if(ctx == null)
	    	 err = message;
	     else {
	    	 
	        for(Message m : ctx.getAllMessages())
	    	    err += m + "; " ;
	     }   
		
		 throw new IllegalArgumentException(EhrLogger.doMessage(this.getClass(), method, err));
				 	
		 
	}	
	
	private void initMessageContext(MessageContext messageCtx, String id, String code){
		messageCtx.addMessage(new MessageBuilder()
		.code(code)
		.error()
		.args("Customer Id", id)
		.source("Customer Id")
		.build());
	}
	
	private void debugPrintCustomer(Customer customer) {
		
		System.out.println("CreateCustomerFlow#debugPrint: for Customer in session");
		
		String out = customer.getFirstName() + " " + customer.getLastName() 
				+ " " + customer.getAddress() + " " + customer.getCity()
				+ " " + customer.getPostalCode();
		
		System.out.println(out);
	}

}
