package com.translate.wublub;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by Liang on 2017-10-21.
 */

class GetURL {

    // 合法的 url 格式：http://openapi.youdao.com/api?q=待翻译文本&from=源语言&to=目的语言&appKey=申请到的应用ID&salt=随机数&sign=签名
    // 其中 sign = MD5(appKey + q + salt + 申请到的密钥)
    // 详细的 url 格式资料：http://ai.youdao.com/docs/doc-trans-api.s#p01

    private final static String HTTP = "http://openapi.youdao.com/api";         // 有道翻译API HTTP 地址
    private final static String APP_KEY = "5998a370375262ca";                   // 申请到的应用ID
    private final static String KEY = "Q3h7SEcehRlCKHAOMrCbiHOVX5CAfFZk";       // 申请到的密钥
    private final static String FROM = "auto";                                  // 自动翻译（无需指明 英->中 或 中->英）
    private final static String TO = "auto";                                    // 自动翻译（无需指明 英->中 或 中->英）
    private String salt = String.valueOf(System.currentTimeMillis());           // 盐值，随机数即可
    private String toTranlateText;                                              // 待翻译文本
    private String sign;                                                        // 签名，用于安全性认证
    private StringBuilder url = new StringBuilder();                            // 最终的请求url


    private void generateUrl(){

        sign = generateSign();

        url.append(HTTP);

        try{
            url.append("?q=" + URLEncoder.encode(toTranlateText, "UTF-8"));
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        url.append("&from=" + FROM);
        url.append("&to=" + TO);
        url.append("&appKey=" + APP_KEY);
        url.append("&salt=" + salt);
        url.append("&sign=" + sign);
    }

    private String generateSign(){
        return md5(APP_KEY + toTranlateText + salt + KEY);
    }

    private String md5(String string){
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};

        try{
            byte[] btInput = string.getBytes("utf-8");
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        }catch(NoSuchAlgorithmException | UnsupportedEncodingException e){
            return null;
        }
    }

    public String getUrl(String text){
        clear();    // 清空缓存的上一个url
        toTranlateText = text;
        generateUrl();
        return url.toString();
    }

    private void clear(){
        url.setLength(0);
    }
}