package com.mycompany.hosted.checkoutFlow.mvc.controller.paypal;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import com.mycompany.hosted.cart.Cart;
import com.mycompany.hosted.checkoutFlow.WebFlowConstants;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails;

import com.mycompany.hosted.exception_handler.EhrLogger;
import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.order.LineItemPayment;
import com.mycompany.hosted.model.order.OrderPayment;
import com.mycompany.hosted.model.order.OrderShipTo;
/*
 * To do: 
 * get items (status) from Order
 * Remove PaymentDetails from session.
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class PaymentStatusController {
	
	
	@Autowired 
	private Cart cart;	
	
	@GetMapping(value="/payment/status")
	public String showOrderStatus(@RequestParam("orderId") Integer orderId, 			         
			HttpSession session, ModelMap model) {
		
		PaymentDetails details = (PaymentDetails)session.getAttribute(WebFlowConstants.PAYMENT_DETAILS);
		
		if(details == null)
			EhrLogger.throwIllegalArg(this.getClass(), "showOrderStatus", 
					"PaymentDetails session attribute is null");
		
		OrderPayment order = (OrderPayment)	session.getAttribute(WebFlowConstants.ORDER_ENTITY_VALUE);		
		
		debugPrintOrThrowOrder (order);		
		
		model.addAttribute(WebFlowConstants.PAYMENT_DETAILS, details);
		
		model.addAttribute(WebFlowConstants.CART, cart);	
		
		model.addAttribute(WebFlowConstants.ERROR_DETAIL_BEAN, 
				WebFlowConstants.getErrorsFromSession(session));
		
		model.addAttribute(WebFlowConstants.ORDER_ENTITY_VALUE, order);
		
		return "jsp/paymentStatus";
		
	}
	
 /*	@GetMapping(value="/payment/statusOnError")
	public String showOrderOnError(HttpSession session, ModelMap model) {
		
		PaymentDetails details = (PaymentDetails)session.getAttribute(WebFlowConstants.PAYMENT_DETAILS);
		
		Exception exception = (Exception)session.getAttribute(WebFlowConstants.ORDER_EXCEPTION);
		
		OrderPayment order = (OrderPayment)session.getAttribute(WebFlowConstants.ERR_ORDER);
				
		model.addAttribute(WebFlowConstants.PAYMENT_DETAILS, details);
		
		model.addAttribute("exception", exception);	
		
		model.addAttribute(WebFlowConstants.ERR_ORDER, order);
		
		return "jsp/paymentStatus";
		
	} */
	
	
	@GetMapping(value="/payment/receipt")
	public String showReceipt(ModelMap model) {
		
		
		
		return "jsp/receipt";
		
	}
	
	private void debugPrintOrThrowOrder(OrderPayment order) {
		
		if(order == null)
			EhrLogger.throwIllegalArg(this.getClass(), "showOrderStatus", "Persisted Order is not in the session. ");
		
        String err="";
		
		Customer custId = order.getCustomerId();
		
		if(custId == null)
			err="Customer ";
		
		OrderShipTo shipTo = order.getOrderShipTo();
		
		if(shipTo == null)
			err = "OrderShipTo ";
		
		List<LineItemPayment> list= order.getLineItemPayments();
		
		if(list == null)
			err = "List<LineItemPayment>";
		
		if(!err.isEmpty()) {
			err = "Null attributes: " + err;
			EhrLogger.throwIllegalArg(this.getClass(),"debugPrintOrder", err);
		}
			
		
		System.out.println("PaymentStatusController#debugPrintOrder: "
				+ custId.getId() + " "
				+ shipTo.getFirstName() + " "
				+ list.get(0).getDescription());
		
	}

}
