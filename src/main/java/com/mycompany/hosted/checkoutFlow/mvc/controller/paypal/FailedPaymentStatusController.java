package com.mycompany.hosted.checkoutFlow.mvc.controller.paypal;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.mycompany.hosted.checkoutFlow.WebFlowConstants;
import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails;

/*
 * To do: Correlate status with a documented error message.
 * 
 * Note: Failed status will occur either on GetDetails or on Capture
 * 
 */

@Controller
public class FailedPaymentStatusController {
	
	@GetMapping(value="/failedStatus/handle")
	public String handleFailedStatus(HttpSession session, ModelMap model) {
		
		model.addAttribute(WebFlowConstants.PAYMENT_DETAILS, 
				(PaymentDetails)session.getAttribute(WebFlowConstants.PAYMENT_DETAILS));
		
		return "jsp/paymentFailedStatus";
	}

}
