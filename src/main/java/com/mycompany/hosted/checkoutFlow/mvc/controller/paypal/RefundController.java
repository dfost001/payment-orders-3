package com.mycompany.hosted.checkoutFlow.mvc.controller.paypal;

import java.io.IOException;
import java.util.List;


import javax.persistence.PersistenceException;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mycompany.hosted.checkoutFlow.WebFlowConstants;
import com.mycompany.hosted.checkoutFlow.exceptions.CheckoutHttpException;
import com.mycompany.hosted.checkoutFlow.jpa.CustomerJpa;
import com.mycompany.hosted.checkoutFlow.paypal.orders.GetOrderDetails;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PayPalClient;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails.CaptureStatusEnum;
import com.mycompany.hosted.errordetail.ErrorDetailBean;
import com.mycompany.hosted.exception_handler.EhrLogger;
import com.mycompany.hosted.exception_handler.MvcNavigationException;
import com.mycompany.hosted.model.Customer;
import com.mycompany.hosted.model.order.LineItemPayment;
import com.mycompany.hosted.model.order.OrderPayment;
import com.mycompany.hosted.model.order.OrderShipTo;
import com.paypal.http.HttpResponse;
import com.paypal.payments.CapturesRefundRequest;
import com.paypal.payments.Refund;
import com.paypal.payments.RefundRequest;

@Controller
public class RefundController {
	
	private final String ALREADY_REFUNDED= "ALREADY_REFUNDED";
	
	private final String REDIRECT_STATUS_URL = "/spring/payment/status?orderId=";
	
	private final String REDIRECT_HTTP_ERROR = "/spring/paymentException/initErrorModel";

	@Autowired
	private PayPalClient payPalClient;
	
	@Autowired
	private CustomerJpa jpa;
	
	
	private ErrorDetailBean errorBean;
	
	@RequestMapping(value="/refund/request", method=RequestMethod.GET)
	public String refund(HttpSession session, 
			RedirectAttributes redirectAttrs) throws CheckoutHttpException, MvcNavigationException {
		
		PaymentDetails details = (PaymentDetails)session.getAttribute(WebFlowConstants.PAYMENT_DETAILS);
		
		OrderPayment order = (OrderPayment)session.getAttribute(WebFlowConstants.ORDER_ENTITY_VALUE);		
		
		if(details == null || order == null)
		   EhrLogger.throwIllegalArg(this.getClass(), "refund", "PaymentDetails or OrderPayment is null");
		
		if(alreadyRefunded(details, redirectAttrs)) {
			
			return "redirect:" + REDIRECT_STATUS_URL + order.getOrderId();
		}
		
		evalTransactionId(details); //throws navigation exception if transaction id is empty
		
		CapturesRefundRequest request = new CapturesRefundRequest(details.getTransactionId());
		
		request.requestBody(new RefundRequest());
		
		request.prefer("return=representation");
		
		HttpResponse<Refund> response;
		
		try {
		
		 response = payPalClient.client().execute(request);
		 
		 System.out.println("RefundController#refund: status=" + response.statusCode());
		
		} catch (IOException e) {
			
			CheckoutHttpException checkoutEx = new CheckoutHttpException(e, "refund");
			
			session.setAttribute("checkoutHttpException", checkoutEx);	
			
			return "redirect:" + this.REDIRECT_HTTP_ERROR;
			
		}
		
		Refund refund = response.result();
		
		debugPrint(refund);
		
        String json = GetOrderDetails.debugPrintJson(response);	    
	    
	    initPaymentDetails(details, refund, json);	    
	   
	    this.updateOrderStatus(order, details, session);	    
		
	    return "redirect:" + REDIRECT_STATUS_URL + order.getOrderId();
		
	}
	
	private boolean alreadyRefunded(PaymentDetails details,  RedirectAttributes attrs) {
		
		if(details.getRefundId() != null) {
			
		
			attrs.addFlashAttribute(ALREADY_REFUNDED, true);
			
			return true;
					
		}	
		
		return false;
	}
	
	private void evalTransactionId(PaymentDetails details) throws MvcNavigationException {
		
		String err = "";

		if(details.getTransactionId() == null) {
			err = EhrLogger.doMessage(this.getClass(), "refund", "Transaction Id is null.");
			
			err += "Assuming browser navigation after details obtained during a 2nd transaction";
			
			throw new MvcNavigationException(err);
		}
	}
	
	private void initPaymentDetails(PaymentDetails details, Refund refund, String json) {
		
		details.setRefundAmount(refund.amount().value());
		
		details.setRefundId(refund.id());
		
		details.setRefundJson(json);
		
		details.setRefundTime(refund.createTime());
		
		
	}
	
	private void updateOrderStatus(OrderPayment order, PaymentDetails paymentDetails,
			HttpSession session) {
		
		order.setPaymentStatus(CaptureStatusEnum.REFUNDED.name());
		
		System.out.println("RefundController#updateOrderStatus: entering: "
				+ "orderId:" + order.getOrderId());
		
		if(order.getOrderId() <= 0) {
			
			String err = "Payment successfuly refunded for error Order: #" + order.getOrderId();		
			
			errorBean = WebFlowConstants.getErrorsFromSession(session);
			
			errorBean.addDetailToList(order, err, paymentDetails,
					this.getClass().getCanonicalName() + 
					"#updateOrderStatus");
			
			return;
		}
		
		try {
			
			jpa.updateOrderStatus(order);
			
			debugPrintOrder(order);
			
		} catch (PersistenceException ex ) {	
			
			System.out.println("Printing message trace: " +  ex.getMessage());	
			
			System.out.println(EhrLogger.getMessages(ex, "\\n"));
			
			errorBean =  WebFlowConstants.getErrorsFromSession(session);
			
			errorBean.addDetailToList(order, paymentDetails, ex,
					this.getClass().getCanonicalName() + 
					"#updateOrderStatus");
			
		}
		
	}
	
	private void debugPrint(Refund refund) {
		
		System.out.println("RefundController#debugPrint: id=" 
			 + refund.id() + " amount="
		     + refund.amount().value());	
		
		
	}
	
	private void debugPrintOrder(OrderPayment order) {
		
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
			
		
		System.out.println("RefundController#debugPrintOrder: "
				+ custId.getId() + " "
				+ "status=" + order.getPaymentStatus() + " "
				+ shipTo.getFirstName() + " "
				+ list.get(0).getDescription());
		
	}
	
} //end class
