package com.mycompany.hosted.checkoutFlow;



import org.springframework.stereotype.Component;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import com.mycompany.hosted.checkoutFlow.exceptions.FlowNavigationException;
import com.mycompany.hosted.checkoutFlow.exceptions.WebflowCartEmptyException;
import com.mycompany.hosted.exception_handler.EhrLogger;
import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.PostalAddress;


@Component
public class EvalApplicationState {
	
	private enum EntryKey {
		
		EntryCustomer, EntrySelectedAddress, EntryPaymentDetails
	}
	
	Customer currentCustomer;
	Boolean currentPaymentDetails;
	PostalAddress currentSelectedAddress;
	
	Customer entryCustomer;
	PostalAddress entrySelectedAddress;
	Boolean entryPaymentDetails;
	
	
	private void initCurrentSessionState(RequestContext ctx) {
		
	   SharedAttributeMap<Object> sessionMap = 	ctx.getExternalContext().getSessionMap();
		
	   currentCustomer = (Customer)sessionMap.get(WebFlowConstants.CUSTOMER_KEY);
	   
	   currentSelectedAddress = (PostalAddress)sessionMap.get(WebFlowConstants.SELECTED_POSTAL_ADDR);
	   
	   currentPaymentDetails = sessionMap.getBoolean(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED);   
		
	}
	
	private void initEntrySessionState(RequestContext ctx) {
		
		MutableAttributeMap<Object> viewScope = ctx.getViewScope();
		
		entryCustomer = (Customer)viewScope.get(EntryKey.EntryCustomer.name());
		
		entrySelectedAddress = (PostalAddress)viewScope.get(EntryKey.EntrySelectedAddress.name());
		
		entryPaymentDetails = (Boolean) viewScope.getBoolean(EntryKey.EntryPaymentDetails.name());
		
		//debugViewScope(ctx.getCurrentState().toString(), entryCustomer, entrySelectedAddress, entryPaymentDetails);
		
		
	}
	
	public void setViewScopeComparisonAttrs(RequestContext ctx) {
		
		SharedAttributeMap<Object> sessionMap = ctx.getExternalContext().getSessionMap();
		
		MutableAttributeMap<Object> viewScope = ctx.getViewScope();
		
		Customer entryCustomer = (Customer)sessionMap.get(WebFlowConstants.CUSTOMER_KEY);
		   
		PostalAddress entrySelectedAddress = (PostalAddress)sessionMap.get(WebFlowConstants.SELECTED_POSTAL_ADDR);
		   
		Boolean entryPaymentDetails = sessionMap.getBoolean(WebFlowConstants.PAYPAL_GETDETAILS_COMPLETED);
		
		viewScope.put(EntryKey.EntryCustomer.name(), entryCustomer);
		
		viewScope.put(EntryKey.EntrySelectedAddress.name(), entrySelectedAddress);
		
		viewScope.put(EntryKey.EntryPaymentDetails.name(), entryPaymentDetails);
		
		
	}
	
	public void evalLogin(RequestContext ctx) throws FlowNavigationException {		
		
		initCurrentSessionState(ctx); //From session into module level variables
		
		initEntrySessionState(ctx); //From view-scope into module-level
		
		//debugViewScope("Login", entryCustomer, entrySelectedAddress, entryPaymentDetails);
		
		boolean expectedOnEnter = entryCustomer == null
				&& entrySelectedAddress == null && entryPaymentDetails == null;		
		
		boolean expectedOnRender = currentCustomer == null && currentSelectedAddress == null 
				&& currentPaymentDetails == null; 
		
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
		
		boolean expectedOnEnter = entryCustomer != null &&  entryPaymentDetails == null;		
		
		boolean expectedOnRender = currentCustomer != null && currentSelectedAddress == entrySelectedAddress 
				&& currentPaymentDetails == null; 
		
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
					&& entryPaymentDetails == null;			
			
			boolean expectedOnRender = currentCustomer != null && currentSelectedAddress != null
					&& currentPaymentDetails == null; 
			
			evalExpectedState(expectedOnEnter, "on-enter", "evalPaymentButtonView", "");
				
			evalExpectedState(expectedOnRender, "on-render","evalPaymentButtonView", "");
			
	      
	}
	
	public void evalPaymentDetailView(RequestContext ctx) throws FlowNavigationException {		    
			
			initCurrentSessionState(ctx);
			
			initEntrySessionState(ctx);
			
			//debugViewScope("paymentDetailView", entryCustomer, entrySelectedAddress, entryPaymentDetails);
			
			boolean expectedOnEnter = entryCustomer != null &&  entrySelectedAddress != null
					&& entryPaymentDetails != null ;				
			
			boolean expectedOnRender = currentCustomer != null && currentSelectedAddress != null
					&& currentPaymentDetails != null; 
			
			evalExpectedState(expectedOnEnter, "on-enter", "evalPaymentDetailView", "");
			
			evalExpectedState(expectedOnRender, "on-render","evalPaymentDetailView", "");			
			
	}
	
	public void evalPostalEditView(RequestContext ctx) 
			 throws FlowNavigationException {		
		
		
		this.initCurrentSessionState(ctx);
		
		this.initEntrySessionState(ctx);
		
		debugViewScope("postalEditView", entryCustomer, entrySelectedAddress, entryPaymentDetails);
		
		boolean expectedOnEnter = entryPaymentDetails == null; //Customer may be null if creating		
		 
		boolean expectedOnRender = entryCustomer == currentCustomer && currentPaymentDetails == null;				
			
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
						entryPaymentDetails, detail);
				
				EhrLogger.throwIllegalArg(this.getClass(), method, error);
			}
			else if(phase.contentEquals("on-render")) {
				
                error = genErrText("on-render", currentCustomer, currentSelectedAddress, 
                		currentPaymentDetails, detail);
				
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
