package com.hotelreserve.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.hotelreserve.config.AlipayConfig;
import com.hotelreserve.repository.ReservationRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/callback/alipay")
public class AlipayCallbackController {

    private final AlipayConfig alipayConfig;
    private final ReservationRepository reservationRepo;

    public AlipayCallbackController(AlipayConfig alipayConfig, ReservationRepository reservationRepo) {
        this.alipayConfig = alipayConfig;
        this.reservationRepo = reservationRepo;
    }

    @GetMapping("/return")
    public String returnUrl(HttpServletRequest request, HttpSession session) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                    : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(
            params,
            alipayConfig.getAlipayPublicKey(),
            alipayConfig.getCharset(),
            alipayConfig.getSignType()
        );

        if (signVerified) {
            String outTradeNo = params.get("out_trade_no");
            Long reservationId = Long.parseLong(outTradeNo.replace("RSV", ""));
            reservationRepo.findById(reservationId).ifPresent(r -> {
                if ("UNPAID".equals(r.getStatus())) {
                    r.setStatus("PENDING");
                    reservationRepo.save(r);
                }
            });
            session.setAttribute("flashMessage", "支付成功！您的预约已确认。");
            session.setAttribute("flashType", "success");
        } else {
            session.setAttribute("flashMessage", "支付验证失败，请联系管理员。");
            session.setAttribute("flashType", "error");
        }

        return "redirect:/user/home";
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                    : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(
            params,
            alipayConfig.getAlipayPublicKey(),
            alipayConfig.getCharset(),
            alipayConfig.getSignType()
        );

        if (signVerified) {
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                Long reservationId = Long.parseLong(outTradeNo.replace("RSV", ""));
                reservationRepo.findById(reservationId).ifPresent(r -> {
                    r.setStatus("PENDING");
                    reservationRepo.save(r);
                });
            }
            return "success";
        } else {
            return "fail";
        }
    }
}
