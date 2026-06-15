package com.hotelreserve.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.hotelreserve.config.AlipayConfig;
import org.springframework.stereotype.Service;

@Service
public class AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    public AlipayService(AlipayClient alipayClient, AlipayConfig alipayConfig) {
        this.alipayClient = alipayClient;
        this.alipayConfig = alipayConfig;
    }

    public String createPayPage(String orderId, String totalAmount, String subject) throws Exception {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayConfig.getReturnUrl());
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        request.setBizContent("{" +
            "    \"out_trade_no\":\"" + orderId + "\"," +
            "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
            "    \"total_amount\":" + totalAmount + "," +
            "    \"subject\":\"" + subject + "\"," +
            "    \"timeout_express\":\"1h\"" +
            "}");

        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create Alipay payment: " + response.getMsg());
        }
    }
}
