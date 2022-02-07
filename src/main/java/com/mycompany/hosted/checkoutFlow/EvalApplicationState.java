package com.mycompany.hosted.checkoutFlow;



import org.springframework.stereotype.Component;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import com.mycompany.hosted.checkoutFlow.exceptions.FlowNavigationException;
import com.mycompany.hosted.checkoutFlow.exceptions.WebflowCartEmptyException;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails;
import com.mycompany.hosted.exception_handler.EhrLogger;
import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.PostalAddress;


@Component
public class EvalApplicationState {
	
	private enum EntryKey {
		
		EntryCustomer, EntrySelectedAddress, EntryDetailsCompleted, EntryDetails
	}
	
	Customer currentCustomer;	
	PostalAddress currentSelectedAddress;
	Boolean currentHasDetails;
	PaymentDetails currentPaymentDetails;
	
	Customer entryCustomer;
	PostalAddress entrySelectedAddress;
	Boolean entryHasDetails;	
	PaymentDetails entryPaymentDetails;
	
	
	private void initCurrentSessionState(RequestContext ctx) {
		
	   SharedAttributeMap<Object> sessionMap = 	ctx.getExternalContext().getSessionMap();
		
	   currentCustomer = (Customer)sessionMap.get(WebFlowConstants.CUSTOMER_KEY);
	   
	   currentSelectedAddress = (PostalAddress)sessionMap.get(WebFlowConstants.SELECTED_POSTAL_ADDR);
	   
	   currentHasDetails = sessionMap.getBoolean(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED);  
	   
	   currentPaymentDetails = (PaymentDetails)sessionMap.get(WebFlowConstants.PAYMENT_DETAILS);
		
	}
	
	private void initEntrySessionState(RequestContext ctx) {
		
		MutableAttributeMap<Object> viewScope = ctx.getViewScope();
		
		entryCustomer = (Customer)viewScope.get(EntryKey.EntryCustomer.name());
		
		entrySelectedAddress = (PostalAddress)viewScope.get(EntryKey.EntrySelectedAddress.name());
		
		entryHasDetails = (Boolean) viewScope.getBoolean(EntryKey.EntryDetailsCompleted.name());
		
		entryPaymentDetails = (PaymentDetails)viewScope.get(EntryKey.EntryDetails.name());
		
		//debugViewScope(ctx.getCurrentState().toString(), entryCustomer, entrySelectedAddress, entryPaymentDetails);
		
		
	}
	
	public void setViewScopeComparisonAttrs(RequestContext ctx) {
		
		SharedAttributeMap<Object> sessionMap = ctx.getExternalContext().getSessionMap();
		
		MutableAttributeMap<Object> viewScope = ctx.getViewScope();
		
		Customer entryCustomer = (Customer)sessionMap.get(WebFlowConstants.CUSTOMER_KEY);
		   
		PostalAddress entrySelectedAddress = (PostalAddress)sessionMap.get(WebFlowConstants.SELECTED_POSTAL_ADDR);
		   
		Boolean entryHasPayment = sessionMap.getBoolean(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED);
		
		PaymentDetails paymentDetails = (PaymentDetails)sessionMap.get(WebFlowConstants.PAYMENT_DETAILS);
		
		viewScope.put(EntryKey.EntryCustomer.name(), entryCustomer);
		
		viewScope.put(EntryKey.EntrySelectedAddress.name(), entrySelectedAddress);
		
		viewScope.put(EntryKey.EntryDetailsCompleted.name(), entryHasPayment);
		
		viewScope.put(EntryKey.EntryDetails.name(), paymentDetails);		
		
	}
	
	public void evalLogin(RequestContext ctx) throws FlowNavigationException {		
		
		initCurrentSessionState(ctx); //From session into module level variables
		
		initEntrySessionState(ctx); //From view-scope into module-level
		
		//debugViewScope("Login", entryCustomer, entrySelectedAddress, entryPaymentDetails);
		
		boolean expectedOnEnter = entryCustomer == null
				&& entrySelectedAddress == null && entryHasDetails == null;		
		
		boolean expectedOnRender = currentCustomer == null && currentSelectedAddress == null 
				&& currentHasDetails == null; 
		
		evalExpectedState(expectedOnEnter, "on-enter", "evalLogin", "");
		
		evalExpectedState(expectedOnRender, "on-render", "evalLogin", "");
     
	}
	
	/*
	 * If selected address does not compare, end-user may be at payment-buttons view.
	 */
	public void evalSelectAddress(RequestContext ctx) throws FlowNavigationException {
        
		
		initCurrentSessionState(ctx);
		
		initEntrySessionState(ctx);
		
		//debugViewScope("evalSelectAddress", entryCustomer, entrySelectedAddress, entryPaymentDetails);
		
		boolean expectedOnEnter = entryCustomer != null &&  entryHasDetails == null;		
		
		boolean expectedOnRender = currentCustomer != null && currentSelectedAddress == entrySelectedAddress 
				&& currentHasDetails == null; 
		
        String detail = currentSelectedAddress != entrySelectedAddress ?
    	    		"Selected address on-entry and in current session are not equal" : "";
			
        evalExpectedState(expectedOnEnter, "on-enter", "evalSelectAddress", "");
		
		evalExpectedState(expectedOnRender, "on-render", "evalSelectAddress", detail);
		
	}
	
	public void evalPaymentButtonView(RequestContext ctx) throws FlowNavigationException {
		   
			
			initCurrentSessionState(ctx);
			
			initEntrySessionState(ctx);
			
		//	debugViewScope("paymentButtonView", entryCustomer, entrySelectedAddress, entryPaymentDetails);
			
			boolean expectedOnEnter = entryCustomer != null &&  entrySelectedAddress != null
					&& entryHasDetails == null;			
			
			boolean expectedOnRender = currentCustomer != null && currentSelectedAddress != null
					&& currentHasDetails == null; 
			
			evalExpectedState(expectedOnEnter, "on-enter", "evalPaymentButtonView", "");
				
			evalExpectedState(expectedOnRender, "on-render","evalPaymentButtonView", "");
			
	      
	}
	
	public void evalPaymentDetailView(RequestContext ctx) throws FlowNavigationException {		    
			
			initCurrentSessionState(ctx);
			
			initEntrySessionState(ctx);
			
			//debugViewScope("paymentDetailView", entryCustomer, entrySelectedAddress, entryPaymentDetails);
			
			String issue = "";
			
			boolean expectedOnEnter = entryCustomer != null &&  entrySelectedAddress != null
					&& entryHasDetails != null && entryPaymentDetails.getTransactionId() == null ;	
			
			if(entryHasDetails != null && entryPaymentDetails.getTransactionId() != null)
				 issue = "Details not reset on 2nd transaction. Transaction Id should be null";
			
			evalExpectedState(expectedOnEnter, "on-enter", "evalPaymentDetailView", issue);
			
			boolean expectedOnRender = currentCustomer != null && currentSelectedAddress != null
					&& currentHasDetails != null && currentPaymentDetails.getTransactionId() == null;					
			
			if(currentHasDetails != null && currentPaymentDetails.getTransactionId() != null)
				EhrLogger.throwIllegalArg(this.getClass(), "evalPaymentDetailView", 
						"DetailsCompleted not removed from session when payment captured.");
			
			issue = currentPaymentDetails.getTransactionId() != null ?
					"Entering details/authorize view after payment completed: Transaction Id obtained." : "";
			
			evalExpectedState(expectedOnRender, "on-render","evalPaymentDetailView", issue);			
			
	}
	
	public void evalPostalEditView(RequestContext ctx) 
			 throws FlowNavigationException {		
		
		
		this.initCurrentSessionState(ctx);
		
		this.initEntrySessionState(ctx);
		
		debugViewScope("postalEditView", entryCustomer, entrySelectedAddress, entryHasDetails);
		
		boolean expectedOnEnter = entryHasDetails == null; //Customer may be null if creating		
		 
		boolean expectedOnRender = entryCustomer == currentCustomer && currentHasDetails == null;				
			
        evalExpectedState(expectedOnEnter, "on-enter", "evalPostalEditView", "");
        
        String detail = entryCustomer != currentCustomer ?
	    		"Customer on-entry is not equal to current session. " : ""; //Render phase may execute multiple
		
		evalExpectedState(expectedOnRender, "on-render", "evalPostalEditView", detail);		
		
	}
	
	public void evalNavigationErrorView(WebflowCartEmptyException entryException,
			RequestContext request) throws FlowNavigationException {		
		
		if(entryException == null) {
			
			String error = "on-entry: CartEmptyException on-entry is null." ;
			
			EhrLogger.throwIllegalArg(this.getClass(), "evalNavigationErrorView", error);
		}
		
		WebflowCartEmptyException ex =
				(WebflowCartEmptyException) request.getFlashScope().get(WebflowDebug.EXCEPTION_KEY);
		
		if(ex==null) {
			
			String error = "on-render: CartEmptyException on-render is not in FlashScope";
			
			this.throwFlowNavigation(error, "evalNavigationErrorView");
		}
		
	}
	
	private void evalExpectedState(Boolean expected, String phase, String method, String detail) 
	
	                 throws FlowNavigationException {
		
		String error="";
		
		if(!expected)
			if(phase.contentEquals("on-entry")) {
				
				error = genErrText(phase, entryCustomer, entrySelectedAddress, 
						entryHasDetails, detail);
				
				EhrLogger.throwIllegalArg(this.getClass(), method, error);
			}
			else if(phase.contentEquals("on-render")) {
				
                error = genErrText("on-render", currentCustomer, currentSelectedAddress, 
                		currentHasDetails, detail);
				
				throwFlowNavigation(error, method);
			}				
	}
	
	private String genErrText(String phase, Customer customer, PostalAddress selected, 
			   Boolean currentDetails, String info) {
		
		String err = phase + ": ";
		
		 err += customer == null ? "Customer is null" : "Customer is non-null";
		 
		 err += ". ";
		 
		 err += selected == null ? "Selected Address is null" : "Selected Address is non-null";
		 
		 err += ". ";
		 
		 err += currentDetails == null ? "paymentDetails is null" : "paymentDetails is non-null";
		 
		 err += ". " + info;
		 
		 return err;
	}
	
	
	
	private void throwFlowNavigation(String message, String method) throws FlowNavigationException {
		
		String error = method + ": " + message;
		
		throw new FlowNavigationException(error);
	}
	
	public void print(RequestContext request, String detail) {
		
		String view = request.getCurrentView() == null ?
				"null" : request.getCurrentView().toString();
		
		String action = request.getCurrentState() == null ? "null" :
			request.getCurrentState().getId();
		
		String transition = request.getCurrentTransition() == null ?
				"null" : request.getCurrentTransition().getId();
		
		String event = request.getCurrentEvent() == null ? "null"
				: request.getCurrentEvent().getId();
		
		String line = detail + ": " + "view=" + view
				+ " currentState=" + action
				+ " transition=" + transition
				+ " event=" + event;
		
		System.out.println(line);
		
	}
	
	private void debugViewScope(String view, Customer customer, PostalAddress selected, Boolean details) {
		
		System.out.println("In viewScope map assigned on-entry at:" + view);
		System.out.println("Customer=" + customer + " selectedAddress=" + selected
				+ " details=" + details);
		
	}

}
