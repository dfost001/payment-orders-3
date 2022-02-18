package com.mycompany.hosted.checkoutFlow.paypal.orders;

import java.io.IOException;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.PaymentCollection;
import com.paypal.orders.PurchaseUnit;
//import com.paypal.orders.LinkDescription;
import com.paypal.orders.Capture;
import com.mycompany.hosted.checkoutFlow.WebFlowConstants;
import com.mycompany.hosted.checkoutFlow.exceptions.CheckoutHttpException;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails.CaptureStatusEnum;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails.FailedReasonEnum;
import com.mycompany.hosted.exception_handler.EhrLogger;
//import com.paypal.orders.Payer;
import com.paypal.http.HttpResponse;


/*
 * Note: Not able to test FailedReasonEnum
 */

@Component
public class CaptureOrder {
	
	@Autowired
	private PayPalClient payClient;
	
	boolean testException = false;
	
	public String capture(RequestContext ctx) throws CheckoutHttpException  {		
		 
		 if(testException) {		 
		 
			    testException = false; 
			    
			    CheckoutHttpException ex = new CheckoutHttpException(new Exception("Testing Exception"),
			    		"capture");
			    
			    ex.setTestException(true);
			    
			    ctx.getExternalContext()
		    	   .getSessionMap()
		    	   .put("checkoutHttpException", ex);
			    
				throw ex;
				
		 }		
		
		 PaymentDetails details = (PaymentDetails)ctx.getExternalContext()
			       .getSessionMap()
			       .get(WebFlowConstants.PAYMENT_DETAILS);
		 
		 String statusResult = "";
		 
		 try {
		 
		evalDetails(details);	//throws exception for null object or empty resource id	
		
	    OrdersCaptureRequest request = new OrdersCaptureRequest(details.getPayPalResourceId());
	    
	    request.prefer("return=representation");	   
	    
	    request.requestBody(buildRequestBody());
	   
	    HttpResponse<Order> response = payClient.client().execute(request); //throws IOException	    
	       
	    System.out.println("CaptureOrder#Status Code: " + response.statusCode());		   
	   
	    Order order = response.result();
	    
	    if(order == null)
	    	this.throwIllegalArg("Order returned in HttpResponse is null.");	
	    
	    debugPrintOrder(order); //throws Runtime for uninitialized fields to catch block
	    
	    String json = GetOrderDetails.debugPrintJson(response);	    
	    
	    statusResult = initCaptureId(order, details,json);
	    
		} catch(IOException | RuntimeException e) {
			
			 CheckoutHttpException httpEx = new CheckoutHttpException(e, "capture");
		    	
		    	ctx.getExternalContext()
		    	   .getSessionMap()
		    	   .put("checkoutHttpException", httpEx);
		    	
		    	throw httpEx;
		}
	    
	    return statusResult;	   
	   
	  }

	  /**
	   *Creating empty body for capture request.
	   *You can set the payment source if required.
	   *
	   *@return OrderRequest request with empty body
	   */
	  private OrderRequest buildRequestBody() {
	    return new OrderRequest();
	  }
	  
	  private void evalDetails(PaymentDetails details) {
		
			 
		 if(details == null)
				 EhrLogger.throwIllegalArg(this.getClass(), "capture", 
						 "PaymentDetails is null in the Http session");
		 
		 else if(details.getPayPalResourceId() == null || details.getPayPalResourceId().isEmpty())
			 
			 EhrLogger.throwIllegalArg(this.getClass(), "capture", 
					 "PaymentDetails has an uninitialized OrderId");
	  }
	  
	  private void throwIllegalArg(String message) {
		  
		  throw new IllegalArgumentException(this.getClass().getCanonicalName()
				  + "#captureOrder: " + message);
	  }
	  
	  private void debugPrintOrder(Order order) {
		  
		  String err = "Order contains uninitialized fields: ";
		  
		   System.out.println("Printing captured order: ");
		  
		   System.out.println("Status: " + order.status()); //May not be initialized
		      
		   System.out.println("Order ID: " + order.id());
		   
		   if(order.paymentSource() == null)
	        	System.out.println("paymentSource is null"); 		      
		 
		   
		   List<PurchaseUnit> units = order.purchaseUnits();
		   
		   if(units == null) {
			   err += " Order#List<PurchaseUnit> ";
			   System.out.println(err);
			   throwIllegalArg(err);
		   }   
		      
		   PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
		   
		   if(purchaseUnit == null) {
			   err += " Order#PurchaseUnit ";
			   System.out.println(err);
			   throwIllegalArg(err);
		   }
		      
		   PaymentCollection payments = purchaseUnit.payments();
		   
		   if(payments == null) {
			   err += " Order#PaymentCollection ";
			   System.out.println(err);
			   throwIllegalArg(err);
		   }
		      
		   List<Capture> captures = payments.captures();
		      
		   if(captures == null || captures.isEmpty()) {
               err += " List<Capture> "	;	
               System.out.println(err);
			   throwIllegalArg(err);
		   }
		   
		   Capture capture = captures.get(0);
		   
		   if(capture == null || isNullOrEmpty(capture.id())) {
			   err += " Capture#id ";
			   System.out.println(err);
			   throwIllegalArg(err);
		   }  	  
		        
		   for (Capture cap : captures) {
		          System.out.println("\t" + "id=" + cap.id());
		          System.out.println("\t" + "status=" + cap.status());
		        
		    }
		     
	  }
	  
	  private String initCaptureId(Order order, PaymentDetails details, String json) {		  
		  
		  details.setJson(json);
		  
		  PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
	      
	      PaymentCollection payments = purchaseUnit.payments();
		  
	      List<Capture> captures = payments.captures();	      
	      
	      Capture capture = captures.get(0);	      
	      
	      //If statusDetails is not null, then status is not equal to COMPLETED/SAVED
	      
	      if(capture.captureStatusDetails() != null) {
	    	  
	    	  System.out.println("CaptureOrder#initCaptureId: capture.status()=" + capture.status());
	    	  
	    	  String reason = capture.captureStatusDetails().reason();
	    	 
	    	  details.setStatusReason(FailedReasonEnum.valueOf(reason));
	    	  
	    	  return "failed";
	    	  
	      } else {    
	    	  
	    	  System.out.println("CaptureOrder#initCaptureId: capture.status()=" + capture.status());
		      
		      details.setCaptureStatus(CaptureStatusEnum.valueOf(capture.status()));
	    	  
	    	  details.setTransactionId(capture.id());
		      
		      details.setCaptureTime(capture.createTime());	  
		      
		      return "success";
	      }
		  
	  }
	  
	 private boolean isNullOrEmpty(String value) {
		 
		 if(value == null || value.isEmpty())
			 return true;
		 return false;
	 }
}
