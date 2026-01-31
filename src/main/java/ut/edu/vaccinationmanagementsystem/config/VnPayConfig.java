package ut.edu.vaccinationmanagementsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VnPayConfig {
    
    @Value("${vnpay.tmnCode:}")
    private String tmnCode;
    
    @Value("${vnpay.hashSecret:}")
    private String hashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String url;
    
    @Value("${vnpay.returnUrl:http://localhost:8080/api/payment/vnpay-return}")
    private String returnUrl;
    
    @Value("${vnpay.version:2.1.0}")
    private String version;
    
    @Value("${vnpay.command:pay}")
    private String command;
    
    @Value("${vnpay.orderType:other}")
    private String orderType;
    
    @Value("${vnpay.locale:vn}")
    private String locale;
    
    @Value("${vnpay.currCode:VND}")
    private String currCode;

    public String getTmnCode() {
        return tmnCode;
    }

    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public void setHashSecret(String hashSecret) {
        this.hashSecret = hashSecret;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getCurrCode() {
        return currCode;
    }

    public void setCurrCode(String currCode) {
        this.currCode = currCode;
    }
}


