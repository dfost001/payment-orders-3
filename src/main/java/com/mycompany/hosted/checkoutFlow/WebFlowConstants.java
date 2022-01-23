package com.mycompany.hosted.checkoutFlow;

import javax.servlet.http.HttpSession;

import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import com.mycompany.hosted.errordetail.ErrorDetailBean;

public class WebFlowConstants {
	
	public static final String CART = "cart";
	
	public static final String CUSTOMER_KEY = "customer";
	
	public static final String SELECTED_POSTAL_ADDR = "selectedAddress";
	
	public static final String ORDER_ENTITY_VALUE = "order";
	
	public static final String CUSTOMER_UNIT = "springMvcSample"; 
	
	public static final String SUPPORT_UNIT = "supportedValidation";
	
	public static final String PAYMENT_DETAILS = "details";	
	
	public static final String PAYPAL_SERVER_ID = "serverId";
	
	public static final String PAYPAL_SCRIPT_ID = "scriptId";
	
	public static final String PAYPAL_GETDETAILS_COMPLETED = "getDetailsCompleted";
	
	public static final String CHECKOUT_ERR_MODEL = "checkoutErrModel";
	
	public static final String ERROR_DETAIL_BEAN = "errorDetailBean";
	
    public static ErrorDetailBean getErrorsFromSession(RequestContext request) {
		
		SharedAttributeMap<Object> sessionMap = request.getExternalContext()
			    .getSessionMap();
		
		ErrorDetailBean errorDetailBean = (ErrorDetailBean) sessionMap.get("errorDetailBean");
		
		if(errorDetailBean == null)
			errorDetailBean = new ErrorDetailBean();
		
		sessionMap.put(ERROR_DETAIL_BEAN, errorDetailBean);
		
		return errorDetailBean;
	}
    
    public static ErrorDetailBean getErrorsFromSession(HttpSession session) {
    	
    	
    ErrorDetailBean errorDetailBean = (ErrorDetailBean) session.getAttribute("errorDetailBean");
		
		if(errorDetailBean == null)
			errorDetailBean = new ErrorDetailBean();
		
		session.setAttribute(ERROR_DETAIL_BEAN, errorDetailBean);
		
		return errorDetailBean;
    	
    }
	

}
