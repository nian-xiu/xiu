package com.example.ssmshop.controller;

import com.example.ssmshop.domain.User;
import com.example.ssmshop.service.WechatPaySessionService;
import com.example.ssmshop.service.WechatPaySessionService.WechatPaySession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Map;

@Controller
public class WechatPayController extends BaseController {
    private final WechatPaySessionService wechatPaySessionService;

    public WechatPayController(WechatPaySessionService wechatPaySessionService) {
        this.wechatPaySessionService = wechatPaySessionService;
    }

    @PostMapping("/wechat-pay/sessions")
    public ResponseEntity<Map<String, Object>> create(@RequestParam String amount,
                                                      HttpServletRequest request,
                                                      HttpSession session) {
        User currentUser = currentUser(session);
        WechatPaySession paySession = wechatPaySessionService.create(currentUser.getId(), amount);
        String publicBaseUrl = publicBaseUrl(request);
        return ResponseEntity.ok(Map.of(
                "token", paySession.token(),
                "confirmUrl", publicBaseUrl + "/wechat-pay/confirm/" + paySession.token(),
                "expiresAt", paySession.expiresAt().toString()
        ));
    }

    @GetMapping("/wechat-pay/status/{token}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String token) {
        WechatPaySession paySession = wechatPaySessionService.find(token);
        return ResponseEntity.ok(Map.of(
                "valid", paySession != null,
                "confirmed", paySession != null && paySession.isConfirmed()
        ));
    }

    @GetMapping("/wechat-pay/confirm/{token}")
    public String confirmPage(@PathVariable String token, Model model) {
        WechatPaySession paySession = wechatPaySessionService.find(token);
        model.addAttribute("paySession", paySession);
        model.addAttribute("token", token);
        return "shop/wechat-confirm";
    }

    @PostMapping("/wechat-pay/confirm/{token}")
    public String confirm(@PathVariable String token, Model model) {
        boolean confirmed = wechatPaySessionService.confirm(token);
        model.addAttribute("confirmed", confirmed);
        return "shop/wechat-confirmed";
    }

    private String publicBaseUrl(HttpServletRequest request) {
        String localIp = firstForwardedHost(request);
        if (localIp == null || localIp.isBlank()) {
            localIp = localIpv4();
        }
        return request.getScheme() + "://" + localIp + ":" + request.getServerPort();
    }

    private String firstForwardedHost(HttpServletRequest request) {
        String host = request.getHeader("X-Local-Host");
        if (host == null || host.isBlank() || "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
            return null;
        }
        int colon = host.indexOf(':');
        return colon > 0 ? host.substring(0, colon) : host;
    }

    private String localIpv4() {
        try {
            var interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                var addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    var address = addresses.nextElement();
                    if (address instanceof Inet4Address inet4Address && !inet4Address.isLoopbackAddress()) {
                        return inet4Address.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
            return "127.0.0.1";
        }
        return "127.0.0.1";
    }
}
