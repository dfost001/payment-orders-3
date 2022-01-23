package com.mycompany.hosted.checkoutFlow.mvc.controller.paypal;

import javax.servlet.http.HttpSession;


import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;


import com.mycompany.hosted.checkoutFlow.exceptions.CheckoutErrModel;
import com.mycompany.hosted.checkoutFlow.exceptions.CheckoutHttpException;
import com.mycompany.hosted.exception_handler.EhrLogger;
import com.paypal.http.exceptions.HttpException;

/*
 * To do: If CheckoutException is not in session, throw UserNavigation
 */

@Controller
public class PaymentExceptionController {	  
	
	  public static final String ERR_GET_DETAIL = "ERR_GET_DETAIL";		
		
		private final String errRecoverable = "The payment service is temporarily unavailable. ";
				
		private final String errFatal = "An error occurred. Please contact support to complete your order.";		
		
		@GetMapping(value="paymentException/initErrorModel")
		public String  initModel(HttpSession session, ModelMap model) {				
			
			
			CheckoutHttpException ex = (CheckoutHttpException)session.getAttribute("checkoutHttpException");
			
			if(ex == null) // change to NavigationException, cannot change to static view since link contains parameter
				EhrLogger.throwIllegalArg(this.getClass(),"initErrorModel", "CheckoutHttpException is null");			
			
			CheckoutErrModel errModel = initErrorModel(ex);	
			
			errModel.setRetUrl(this.evalRecoverableUrl(errModel));
			
			model.addAttribute("checkoutErrModel", errModel);
			
			if(errModel.isRecoverable())
			  session.removeAttribute("checkoutHttpException");
			
			return "jsp/checkoutErrSupport";
			
		}
		
		private CheckoutErrModel initErrorModel(CheckoutHttpException ex) {
			
	       if(ex.isTestException()) {				
				
				return initTestRecoverable(ex);
			}
			CheckoutErrModel err = new CheckoutErrModel();
			
			Throwable cause = EhrLogger.getRootCause(ex.getCause());
			
			System.out.println("CheckoutHttpHandle#initErrorModel: cause=" + cause); //Exception created with cause
			
			Integer status = null;		
			
	        if(cause != null && cause.getClass() == HttpException.class) {
				
				status = ((HttpException)cause).statusCode();
				
				err.setResponseCode(status);
		    }    
	       
	        //To do: eval status codes: 422
	        if(status != null && status == 503) {
	        	
	        	err.setRecoverable(true);
	        	
	        	err.setFriendly(errRecoverable);
	        } else {
	        	
	        	err.setRecoverable(false);
	        	
	        	err.setFriendly(errFatal);
	        }
	        
	        err.setMessage(ex.getMessage());
	        
	        err.setErrMethod(ex.getMethod());  	        
	        
	        err.setMessageTrace(EhrLogger.getMessages(ex, "<br />"));
	        
	        if(err.getErrMethod().contains("refund"))
	        	err.setRecoverable(false); //Until able to test
	        
	        if(cause != null)	        
	           err.setCause(cause.getClass().getCanonicalName());
	        
	        return err;
	   }
		
	   private String evalRecoverableUrl(CheckoutErrModel errModel) {
		   
		   
			
			String url = "checkout-flow";
			
			if (errModel.isRecoverable() && errModel.getErrMethod().contains("getOrder"))
				
				url += "?" + ERR_GET_DETAIL + "=true" ;
			
			return url;
		}
		
		private CheckoutErrModel initTestRecoverable(CheckoutHttpException ex) {
			
	        CheckoutErrModel err = new CheckoutErrModel();		
	        
	        err.setRecoverable(true);
	                	
	        err.setFriendly(errRecoverable); 
	        
	        err.setResponseCode(503);
	        
	        err.setMessage(ex.getMessage());
	        
	        err.setMessageTrace(EhrLogger.getMessages(ex, "<br />"));
	        
	        err.setErrMethod(ex.getMethod());
	        
	        return err;
			
		}	


}
