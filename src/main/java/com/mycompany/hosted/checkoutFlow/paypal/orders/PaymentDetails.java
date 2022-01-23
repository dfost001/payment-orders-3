package com.mycompany.hosted.checkoutFlow.paypal.orders;

import java.io.Serializable;

//import com.mycompany.hosted.model.order.PaymentStatusEnum;

@SuppressWarnings("serial")
public class PaymentDetails implements Serializable{
	
	public enum CaptureStatusEnum {
		COMPLETED, 
		DECLINED, 
		PARTIALLY_REFUNDED, 
		PENDING, 
		REFUNDED, 
		FAILED 
	}
	
	public enum GetDetailsStatus {
		CREATED, 
		SAVED, 
		APPROVED, 
		VOIDED, 
		COMPLETED, 
		PAYER_ACTION_REQUIRED
	}
	
	public enum FailedReasonEnum {
		BUYER_COMPLAINT, 
		CHARGEBACK, 
		ECHECK, 
		INTERNATIONAL_WITHDRAWAL, 
		OTHER, 
		PENDING_REVIEW, 
		RECEIVING_PREFERENCE_MANDATES_MANUAL_ACTION, 
		REFUNDED, 
		TRANSACTION_APPROVED_AWAITING_FUNDING, 
		UNILATERAL, 
		VERIFICATION_REQUIRED 
	}
	
	
	private String payPalResourceId;
	
	private String billingName;
	
	private String billingEmail;
	
	private String json;
	
	private String refundJson;
	
	private String payerId;
	
	private String transactionId;
	
	private String createTime;
	
	private String captureTime;
	
	private String refundTime;
	
	private String refundId;
	
	private String refundAmount;	
	
	private GetDetailsStatus createdStatus;
	
	private CaptureStatusEnum captureStatus;
	
	private FailedReasonEnum statusReason;
	
	public PaymentDetails() {}
	
	public PaymentDetails(String id) {
		
		this.payPalResourceId = id;
	}	

	public String getPayPalResourceId() {
		return payPalResourceId;
	}

	public void setPayPalResourceId(String payPalResourceId) {
		this.payPalResourceId = payPalResourceId;
	}

	public String getBillingName() {
		return billingName;
	}

	public void setBillingName(String billingName) {
		this.billingName = billingName;
	}

	public String getBillingEmail() {
		return billingEmail;
	}

	public void setBillingEmail(String billingEmail) {
		this.billingEmail = billingEmail;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		
		this.json = json;
	}

	public String getPayerId() {
		return payerId;
	}

	public void setPayerId(String payerId) {
		this.payerId = payerId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCaptureTime() {
		return captureTime;
	}

	public void setCaptureTime(String captureTime) {
		this.captureTime = captureTime;
	}

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	

	public CaptureStatusEnum getCaptureStatus() {
		return captureStatus;
	}

	public void setCaptureStatus(CaptureStatusEnum captureStatus) {
		this.captureStatus = captureStatus;
	}

	public FailedReasonEnum getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(FailedReasonEnum statusReason) {
		this.statusReason = statusReason;
	}	

	public GetDetailsStatus getCreatedStatus() {
		return createdStatus;
	}

	public void setCreatedStatus(GetDetailsStatus createdStatus) {
		this.createdStatus = createdStatus;
	}

	public String getRefundTime() {
		return refundTime;
	}

	public void setRefundTime(String refundTime) {
		this.refundTime = refundTime;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundJson() {
		return refundJson;
	}

	public void setRefundJson(String refundJson) {
		this.refundJson = refundJson;
	}
	
	

}
