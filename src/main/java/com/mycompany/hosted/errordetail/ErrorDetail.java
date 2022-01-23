package com.mycompany.hosted.errordetail;

import com.mycompany.hosted.checkoutFlow.paypal.orders.PaymentDetails;
import com.mycompany.hosted.model.order.OrderPayment;

public class ErrorDetail {
	
	private OrderPayment order;
	private PaymentDetails paymentDetails;
	private String svcTransactionId;
	private String errMethod;
	private Class<?> exceptionClass;
	private String errMessage;
	private Exception exception;
	private String errTime;
	
	
	public String getErrTime() {
		return errTime;
	}
	public void setErrTime(String errTime) {
		this.errTime = errTime;
	}
	public String getSvcTransactionId() {
		return svcTransactionId;
	}
	public void setSvcTransactionId(String svcPaymentTransactionId) {
		this.svcTransactionId = svcPaymentTransactionId;
	}
	public String getErrMethod() {
		return errMethod;
	}
	public void setErrMethod(String errMethod) {
		this.errMethod = errMethod;
	}
	public Class<?> getExceptionClass() {
		return exceptionClass;
	}
	public void setExceptionClass(Class<?> exception) {
		this.exceptionClass = exception;
	}
	public String getErrMessage() {
		return errMessage;
	}
	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}
	
	public OrderPayment getOrder() {
		return order;
	}
	public void setOrder(OrderPayment order) {
		this.order = order;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public PaymentDetails getPaymentDetails() {
		return paymentDetails;
	}
	public void setPaymentDetails(PaymentDetails paymentDetails) {
		this.paymentDetails = paymentDetails;
	}	

}
