package com.mycompany.hosted.checkoutFlow;

import java.io.Serializable;


import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.PostalAddress;
import com.mycompany.hosted.model.ShipAddress;
import com.mycompany.hosted.cart.CartItem;

import java.util.ArrayList;
import java.util.List;



import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.Message;
import org.springframework.webflow.execution.RequestContext;


@SuppressWarnings("serial")
public class MyFlowAttributes implements Serializable {
	
	
	
	private final String SHIP_CAPTION = "Ship To Information";
	
	private final String CUSTOMER_CAPTION = "Customer Information";	
	
	private String formTitle ;
	
	private List<CartItem> flowCartItems;
	
	private final String ALL_MESSAGES = "allMessages" ;
	
	
	public List<CartItem> getFlowCartItems() {
		if(flowCartItems == null)
			return new ArrayList<CartItem>();
		return flowCartItems;
	}

	public void setFlowCartItems(List<CartItem> mvcCartItems) {
		this.flowCartItems = mvcCartItems;
	}

	public void evalFormTitle(PostalAddress addr) {
		
		if(Customer.class.isAssignableFrom(addr.getClass()))
			formTitle = CUSTOMER_CAPTION;
		else if(ShipAddress.class.isAssignableFrom(addr.getClass()))
			formTitle = SHIP_CAPTION;
	}
	
	public String getFormTitle() {
		
		return formTitle;
	}
	
	public void preserveMessagesIntoSession(RequestContext request, MessageContext msgCtx) {
		
		Message[] messages = null;
		
		if(msgCtx != null)
		    messages =  msgCtx.getAllMessages();
		else return;
		
		if(messages.length > 0) {
			request.getExternalContext()
			   .getSessionMap()
			   .put(ALL_MESSAGES, messages);
			return;
		}		
	}
	
	public void removeSessionMessages(RequestContext request) {
	
		request.getExternalContext()
		   .getSessionMap()
		   .remove(ALL_MESSAGES);
	
    }

} //end class
