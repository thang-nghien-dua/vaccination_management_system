package ut.edu.vaccinationmanagementsystem.service;

import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ut.edu.vaccinationmanagementsystem.config.VnPayConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnPayService {
    
    @Autowired
    private VnPayConfig vnPayConfig;
    
    /**
     * Tạo URL thanh toán VNPay
     * @param amount Số tiền (VND)
     * @param orderInfo Thông tin đơn hàng
     * @param orderId Mã đơn hàng (booking code)
     * @param ipAddress IP của khách hàng
     * @return URL thanh toán VNPay
     */
    public String createPaymentUrl(long amount, String orderInfo, String orderId, String ipAddress) {
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_HashSecret = vnPayConfig.getHashSecret();
        String vnp_Url = vnPayConfig.getUrl();
        String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu số tiền nhân 100
        vnp_Params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
        
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", vnPayConfig.getLocale());
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        // Sắp xếp params theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                // Build query URL
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        // Tạo chữ ký
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSha512(vnp_HashSecret, hashData.toString()).toUpperCase();
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        return vnp_Url + "?" + queryUrl;
    }
    
    /**
     * Xác thực chữ ký từ VNPay callback (sử dụng raw query string)
     * @param queryString Raw query string từ request (chưa decode)
     * @return true nếu chữ ký hợp lệ
     */
    public boolean verifySignatureFromQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return false;
        }
        
        // Parse query string thành map
        Map<String, String> params = new HashMap<>();
        String[] pairs = queryString.split("&");
        String vnp_SecureHash = null;
        
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                
                if ("vnp_SecureHash".equals(key)) {
                    vnp_SecureHash = value;
                } else {
                    params.put(key, value);
                }
            }
        }
        
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }
        
        // Sắp xếp params theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        // Build hashData từ raw query string (không decode)
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                // Sử dụng giá trị từ query string (đã được encode bởi VNPay)
                hashData.append(fieldValue);
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        // Tính hash
        String calculatedHash = hmacSha512(vnPayConfig.getHashSecret(), hashData.toString()).toUpperCase();
        
        return calculatedHash.equals(vnp_SecureHash.toUpperCase());
    }
    
    /**
     * Xác thực chữ ký từ VNPay callback (sử dụng Map params - giữ lại để tương thích)
     * @param params Các tham số từ VNPay callback (đã được Spring decode)
     * @return true nếu chữ ký hợp lệ
     */
    public boolean verifySignature(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }
        
        // Loại bỏ vnp_SecureHash khỏi params để tính hash
        Map<String, String> paramsForHash = new HashMap<>(params);
        paramsForHash.remove("vnp_SecureHash");
        
        // Sắp xếp params theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(paramsForHash.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = paramsForHash.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                // VNPay trả về params đã được URL decode, nhưng khi verify cần encode lại
                // để khớp với cách tạo signature ban đầu
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        // Tính hash
        String calculatedHash = hmacSha512(vnPayConfig.getHashSecret(), hashData.toString()).toUpperCase();
        
        return calculatedHash.equals(vnp_SecureHash);
    }
    
    /**
     * Helper method để tính HMAC SHA512 và trả về hex string
     */
    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Chuyển đổi byte array sang hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC SHA512", e);
        }
    }
}

