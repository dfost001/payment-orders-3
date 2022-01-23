package com.mycompany.hosted.checkoutFlow;

import org.springframework.stereotype.Component;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import com.mycompany.hosted.cart.Cart;
import com.mycompany.hosted.model.order.OrderPayment;

@Component
public class PaymentStateAttrs {
	
	public enum PaymentState {
		LOGGED_IN,
		SHIP_SELECTED,
		DETAILS_COMPLETED,
		ERR_GET_DETAIL,
		NONE
	}


 public String evalPaymentState(RequestContext request, boolean errOnGetDetail)	{
	 
	 System.out.println("PaymentStateAttrs#evalPaymentState: executing");
	 
	 SharedAttributeMap<Object> map =  request
			 .getExternalContext()
			 .getSessionMap();
	
	if(errOnGetDetail) 
	    return PaymentState.ERR_GET_DETAIL.name(); //HttpException on GetDetails, return showDetails
	
	else if(map.get(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED) != null) //Capture did not complete
		return PaymentState.DETAILS_COMPLETED.name(); //HttpException on Capture or exit before capture, return showDetails
	
	else if(map.get(WebFlowConstants.SELECTED_POSTAL_ADDR) != null) //Card entry not completed, return paymentButton
		return PaymentState.SHIP_SELECTED.name();
	
	else if(map.get(WebFlowConstants.CUSTOMER_KEY) != null)//Customer retrieved, ShipAddress not selected
		return PaymentState.LOGGED_IN.name();
	
	return PaymentState.NONE.name(); //return customerLogin
 }
	
 public void removeSessionAttrsOnCapture(RequestContext request) {
		 
		 SharedAttributeMap<Object> map =  request
				 .getExternalContext()
				 .getSessionMap();
		 
		//PaymentDetails details = (PaymentDetails)map.get(WebFlowConstants.PAYMENT_DETAILS);
		 
		/* if(details == null)
			 EhrLogger.throwIllegalArg(this.getClass(), "removePaymentDetails",
					 "PaymentDetails is null in the Http Session");*/
		
		map.remove(WebFlowConstants.PAYPAL_SCRIPT_ID);		
		
		map.remove(WebFlowConstants.PAYPAL_SERVER_ID);
		
		map.remove(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED);
		 
	 }
 
 /*
  * Note: Cannot remove PostalAddress until Order initialized and persisted
  */
 public void removeAttrsOnOrderPersisted(RequestContext request) {
	 
	 SharedAttributeMap<Object> map =  request
			 .getExternalContext()
			 .getSessionMap();
	 
	 map.remove(WebFlowConstants.SELECTED_POSTAL_ADDR);
	 
	 Cart cart = (Cart)map.get(WebFlowConstants.CART);
	 
	 cart.clearCart();
	 
 }
 
 public void idValSubmittedIntoSession(RequestContext request, String paramId) {
	 
	 SharedAttributeMap<Object> map =  request
			 .getExternalContext()
			 .getSessionMap();
	 
	 map.put(WebFlowConstants.PAYPAL_SCRIPT_ID,paramId);
 }
 
    public void setOrderIntoSession(RequestContext request, OrderPayment order) {
    	
    	 SharedAttributeMap<Object> map =  request
    			 .getExternalContext()
    			 .getSessionMap();
    	 
    	 map.put(WebFlowConstants.ORDER_ENTITY_VALUE, order);
	
     }
}//end class
