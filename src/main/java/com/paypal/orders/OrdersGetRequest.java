// This class was generated on Thu, 16 May 2019 09:53:44 PDT by version 0.1.0-dev+8fcb5f of Braintree SDK Generator
// OrdersGetRequest.java
// @version 0.1.0-dev+8fcb5f
// @type request
// @data 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
// DO NOT EDIT
package com.paypal.orders;

import com.paypal.http.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
/**
 * Shows details for an order, by ID.
 * Coding fields=payment_source returns an empty response
 */
public class OrdersGetRequest extends HttpRequest<Order> {

    public OrdersGetRequest(String orderId) {
        //super("/v2/checkout/orders/{order_id}?fields=payment_source", "GET", Order.class);
    	super("/v2/checkout/orders/{order_id}?", "GET", Order.class);
        try {
            path(path().replace("{order_id}", URLEncoder.encode(String.valueOf(orderId), "UTF-8")));
           
            System.out.println("OrdersGetRequest: path=" + path());
        } catch (UnsupportedEncodingException ignored) {}

        header("Content-Type", "application/json");
        header("Prefer", "return=representation");
    }
    public OrdersGetRequest authorization(String authorization) {
        header("Authorization", String.valueOf(authorization));
        return this;
    }

    public OrdersGetRequest contentType(String contentType) {
        header("Content-Type", String.valueOf(contentType));
        return this;
    }


}
