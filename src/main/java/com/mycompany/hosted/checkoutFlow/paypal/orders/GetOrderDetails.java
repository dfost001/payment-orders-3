package com.mycompany.hosted.checkoutFlow.paypal.orders;

import java.io.IOException;
import java.util.List;

import com.mycompany.hosted.checkoutFlow.WebFlowConstants;
import com.mycompany.hosted.checkoutFlow.exceptions.CheckoutHttpException;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails.GetDetailsStatus;
import com.mycompany.hosted.checkoutFlow.paypal.rest.OrderId;
import com.mycompany.hosted.exception_handler.EhrLogger;
import com.mycompany.hosted.model.Customer;
//import com.mycompany.hosted.model.order.PaymentStatusEnum;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.SerializeException;
import com.paypal.http.serializer.Json;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.orders.PaymentSource;
import com.paypal.orders.PurchaseUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Component;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

@Component
public class GetOrderDetails  {
	
	 private boolean testException;	
	
	@Autowired
	private PayPalClient payClient;
	
	public GetOrderDetails() {
		
		testException = true;
	}
	
	 public String getOrder(RequestContext ctx) throws CheckoutHttpException {			 
		  
		   
		   if(testException) {
			   
			    testException = false;			    
			    
			    CheckoutHttpException ex = new CheckoutHttpException(new Exception("Testing Exception"),
			    		"getOrder");
			    
			    ex.setTestException(true);
			    
			    ctx.getExternalContext()
		    	   .getSessionMap()
		    	   .put("checkoutHttpException", ex);
			    
				throw ex;
		   }
		 
		   SharedAttributeMap<Object> sharedSession = ctx.getExternalContext().getSessionMap();   		   
		 
		   OrderId orderId = (OrderId)sharedSession.get(WebFlowConstants.PAYPAL_SERVER_ID);
		   
		   String paramId = (String)sharedSession.get(WebFlowConstants.PAYPAL_SCRIPT_ID);
		   
		   PaymentDetails paymentDetails = null;
		    
		    try {
		    	
		    evalOrderId(orderId, paramId); //throws IllegalArgument
		 
		    OrdersGetRequest request = new OrdersGetRequest(orderId.getId());	
		    
		    HttpResponse<Order> response = payClient.client().execute(request);	//throws IOException	       
		    
		    if (!debugPrintOrder(response))
		    	throw new IllegalStateException("GetOrderDetails: debugPrintOrder returned false");
		    
		    paymentDetails = initOrderDetails(response, ctx);
		    
		    sharedSession.put(WebFlowConstants.PAYMENT_DETAILS, paymentDetails);
		    
		    sharedSession.put(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED, true);
		    
		    } catch(IOException | IllegalArgumentException ex) {
		    	
		        CheckoutHttpException httpEx = new CheckoutHttpException(ex, "getOrder");
		    	
		    	ctx.getExternalContext()
		    	   .getSessionMap()
		    	   .put("checkoutHttpException", httpEx);
		    	
		    	throw httpEx;
		    } catch (Exception ex) {
		    	throw ex;
		    }
		    
		    GetDetailsStatus detailsStatus = paymentDetails.getCreatedStatus();
		    
		    if(detailsStatus == GetDetailsStatus.APPROVED)
		    	return "success";		    
		    
		    return "failed";		   
	  }    	
   
	 
	private void evalOrderId(OrderId orderId, String paramId) {
		
		String err = "";

		if (paramId == null || paramId.trim().isEmpty())
			err = "JavaScript orderId is empty. ";

		if (orderId == null )
			err += "Cannot find created orderId in the session.";
		
		if(err == "" && !orderId.getId().contentEquals(paramId))
			err += "JavaScript orderId is not equal to created orderId";
		
		if(!err.isEmpty())
			EhrLogger.throwIllegalArg(this.getClass(), "getOrder", err);
		
	}
	 
	 private PaymentDetails initOrderDetails(HttpResponse<Order> response, RequestContext request) {	
		 
		 Order order = response.result();
		 
		 System.out.println("GetOrderDetails#initOrderDetails: status=" + order.status());
		 
		 PaymentDetails details = new PaymentDetails(order.id());	
		 
		 String json = debugPrintJson(response);
		 
		 details.setJson(json);
		 
		 details.setPayerId(order.payer().payerId());		 
		 
		 String fullName = order.payer().name().givenName().toUpperCase() + " "
				 + order.payer().name().surname().toUpperCase();
		 
		 details.setBillingName(fullName);
		 
		
		 Customer customer = (Customer)request.getExternalContext().getSessionMap()
				 .get(WebFlowConstants.CUSTOMER_KEY);
		 
		 if(customer == null) {	 
			
			 
			 EhrLogger.throwIllegalArg(this.getClass(),
					 "initOrderDetails", "Customer is null in the session");
		 }
		 
		 if(!order.payer().email().toLowerCase().equals(customer.getEmail().toLowerCase())) {
			 
			
			 
			initMessageContext(request.getMessageContext(),
					 "Billing Email",
					    "Your billing email is not the same as your customer email '"
					    + customer.getEmail().toLowerCase() + "'. "
					    + "Please verify." ) ;
		 }
		 
		
		 details.setBillingEmail(order.payer().email().toLowerCase());		
		 
		 details.setCreatedStatus(GetDetailsStatus.valueOf(order.status()));		 
		 
		 return details;
	 }
	 
	 private void initMessageContext(MessageContext messageCtx, 
			 String source, String text){
			messageCtx.addMessage(new MessageBuilder()			
			.error()
			.source(source)
			.defaultText(text)
			.build());
		}
	 
	public static String debugPrintJson(HttpResponse<?> response)  {		
		
		System.out.println("GetOrderDetails#json response body:");
		
		Json json = new Json();
		
		String ser = "";
		
		try {
			
		  ser = json.serialize(response.result());
		  
		} catch(SerializeException e) {
			
			EhrLogger.throwIllegalArg(GetOrderDetails.class, "debugPrintJson",
					"Error showing PayPal Json response", e);
		}
		
		JSONObject jsonObject = new JSONObject(ser);				
		
		System.out.println(jsonObject.toString(4));
		
		String pretty = jsonObject.toString(4);
		
		System.out.println("GetOrderDetails#debugPrintJson: doing pretty");
		
		pretty = pretty.replaceAll("\\n", "<br/>");
		
		pretty = pretty.replaceAll("\\s", "&nbsp;");		
		
		System.out.println("GetOrderDetails#debugPrintJson: completed and returning pretty");
		
		return pretty;
		
	}
	
	private boolean debugPrintOrder(HttpResponse<Order> response) {
		
		System.out.println("GetOrderDetails: responseCode=" + response.statusCode());	
		
		Order order = response.result();
		
		System.out.println("GetOrderDetails#debugPrintOrder:");
		
		if(order == null) {
			System.out.println("Details: Order is null");
			return false;
		}
		String id = order.id();
		
		if(id == null || id.isEmpty()) {
			
			System.out.println("Details: Order ID is null or empty");
			return false;
		}
		
		List<PurchaseUnit> units = order.purchaseUnits();
		
		if(units == null || units.isEmpty()) {
			System.out.println("List<PurchaseUnit> is empty");
			return false;
		}
		
		PurchaseUnit pu = units.get(0);
		
		String detail = pu.description();
		
		System.out.println("GetOrderDetails#debugPrintOrder: "
				+ id + ": " + detail);
		
		PaymentSource source = order.paymentSource();
		
		if(source == null)
			System.out.println("PaymentSource is null");
		else System.out.println("PaymentSource is not null");
		
		return true;
	}

}
