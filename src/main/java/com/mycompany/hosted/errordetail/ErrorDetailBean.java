package com.mycompany.hosted.errordetail;

import java.util.ArrayList;

import java.util.GregorianCalendar;
import java.util.List;



import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails;
import com.mycompany.hosted.model.order.OrderPayment;


public class ErrorDetailBean {
	
	private List<ErrorDetail> errorDetailList = new ArrayList<>();	
	
	
	public List<ErrorDetail> getErrorDetailList() {
		return errorDetailList;
	}

	public ErrorDetail addDetailToList(OrderPayment order, PaymentDetails paymentDetails,
			Exception ex, String errMethod) {
		
		ErrorDetail detail = new ErrorDetail();
		
		detail.setException(ex);
		
		detail.setExceptionClass(ex.getClass());
		
		detail.setErrMessage(ex.getMessage());
		
		initDetail(detail, errMethod, order, paymentDetails);
		
		errorDetailList.add(detail);
		
		return detail;
		
	}
	
	public ErrorDetail addDetailToList( 
			OrderPayment order, String errMsg, PaymentDetails paymentDetails, String method) {
		
		ErrorDetail detail = new ErrorDetail();
		
		detail.setErrMessage(errMsg);
		
		initDetail(detail, method, order, paymentDetails);
		
		errorDetailList.add(detail);
		
		return detail;
		
	}
	
	
	private void initDetail(ErrorDetail detail, String method,
			OrderPayment order, PaymentDetails paymentDetails) {
		
        detail.setErrMethod(method);
		
		detail.setOrder(order);
		
		detail.setSvcTransactionId(paymentDetails.getTransactionId());
		
		detail.setPaymentDetails(paymentDetails);
		
		detail.setErrTime(new GregorianCalendar().toString());
		
	}
	

}
