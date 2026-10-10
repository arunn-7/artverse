package com.artverse.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
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

    public Order createOrder(long amountInPaise, String receipt)
            throws Exception {

        if (amountInPaise <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject request = new JSONObject();
        request.put("amount", amountInPaise);
        request.put("currency", "INR");
        request.put("receipt", receipt);

        return client.orders.create(request);
    }

    public String getKeyId() {
        return keyId;
    }

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature,
            String artworkId,
            long expectedAmountInPaise) throws Exception {

        return verifyPaymentCommon(
                orderId, paymentId, signature, expectedAmountInPaise,
                receipt -> receipt.startsWith("artwork_" + artworkId + "_")
        );
    }

    public boolean verifyAuctionPayment(
            String orderId,
            String paymentId,
            String signature,
            String auctionId,
            long expectedAmountInPaise) throws Exception {

        return verifyPaymentCommon(
                orderId, paymentId, signature, expectedAmountInPaise,
                receipt -> receipt.startsWith("auction_" + auctionId + "_")
        );
    }

    public boolean verifyCommissionPayment(
            String orderId,
            String paymentId,
            String signature,
            Long commissionId,
            long expectedAmountInPaise) throws Exception {

        return verifyPaymentCommon(
                orderId, paymentId, signature, expectedAmountInPaise,
                receipt -> receipt.startsWith("commission_" + commissionId + "_")
        );
    }

    private boolean verifyPaymentCommon(
            String orderId,
            String paymentId,
            String signature,
            long expectedAmountInPaise,
            java.util.function.Predicate<String> receiptValidator)
            throws Exception {

        if (orderId == null || paymentId == null || signature == null) {
            return false;
        }

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        if (!Utils.verifyPaymentSignature(options, keySecret)) {
            return false;
        }

        Order order = client.orders.fetch(orderId);

        String receipt = String.valueOf(order.get("receipt"));
        if (!receiptValidator.test(receipt)) {
            return false;
        }

        long orderAmount = Long.parseLong(
                String.valueOf(order.get("amount"))
        );

        if (orderAmount != expectedAmountInPaise) {
            return false;
        }

        Payment payment = client.payments.fetch(paymentId);

        if (!orderId.equals(String.valueOf(payment.get("order_id")))) {
            return false;
        }

        return "captured".equalsIgnoreCase(
                String.valueOf(payment.get("status"))
        );
    }
}