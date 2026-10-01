package com.artverse.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Payment;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public Order createOrder(long amountInPaise, String receipt) throws Exception {

        RazorpayClient razorpayClient =
                new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        return razorpayClient.orders.create(orderRequest);
    }

    public String getKeyId() {
        return keyId;
    }

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature,
            Long artworkId,
            long expectedAmountInPaise) throws Exception {

        RazorpayClient razorpayClient =
                new RazorpayClient(keyId, keySecret);

        // 1. Verify payment signature
        JSONObject options = new JSONObject();

        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        boolean signatureValid =
                Utils.verifyPaymentSignature(options, keySecret);

        if (!signatureValid) {
            return false;
        }

        // 2. Fetch Razorpay order
        Order order = razorpayClient.orders.fetch(orderId);

        String receipt = String.valueOf(order.get("receipt"));

        // 3. Make sure this order belongs to this artwork
        if (!receipt.startsWith("artwork_" + artworkId + "_")) {
            return false;
        }

        // 4. Make sure the amount is correct
        long orderAmount = Long.parseLong(
                String.valueOf(order.get("amount"))
        );

        if (orderAmount != expectedAmountInPaise) {
            return false;
        }

        // 5. Fetch payment from Razorpay
        Payment payment = razorpayClient.payments.fetch(paymentId);

        String paymentOrderId =
                String.valueOf(payment.get("order_id"));

        // 6. Make sure payment belongs to this order
        if (!orderId.equals(paymentOrderId)) {
            return false;
        }

        // 7. Make sure payment was captured
        String status =
                String.valueOf(payment.get("status"));

        return "captured".equalsIgnoreCase(status);
    }
}