package uz.pet.utils;

import com.google.gson.JsonObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Properties;

public class ServiceFactory extends CommonUtils {
    SSLConnectionSocketFactory sslConnectionSocketFactory;
    HttpHost proxy;
    Properties properties;
    DefaultProxyRoutePlanner routePlanner;
    CloseableHttpClient httpclient;
    HttpPost httpPost;
    HttpGet httpGet;
    HttpPut httpPut;
    HttpDelete httpDelete;
    StringEntity stringEntity;
    RequestConfig requestConfig;
    HttpEntity httpEntity;
    String entityUtils;
    int status;

    //Constructor
    public ServiceFactory() {
        this.sslConnectionSocketFactory = super.getSslConnectionSocketFactory();
        this.properties = super.getProps();
        this.proxy = new HttpHost(properties.getProperty("network.proxy.host"), Integer.parseInt(properties.getProperty("network.proxy.port")), "http");
        this.routePlanner = new DefaultProxyRoutePlanner(proxy);
        this.httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .setRoutePlanner(routePlanner)
                .build();
        this.requestConfig = RequestConfig.custom()
                .setSocketTimeout(Integer.parseInt(properties.getProperty("endpoint.socket_timeout")))
                .setConnectTimeout(Integer.parseInt(properties.getProperty("endpoint.connect_timeout")))
                .setConnectionRequestTimeout(Integer.parseInt(properties.getProperty("endpoint.connection_request_timeout")))
                .setAuthenticationEnabled(true)
                .build();
    }

    public CommonResponse successResponses(JsonObject jsonObject) throws Exception {
        JSONParser parser = new JSONParser();

        CommonResponse commonResponse = new CommonResponse();
        System.out.println(jsonObject);
        String url = jsonObject.get("url").getAsString();
        String method = jsonObject.get("method").getAsString();
        String category = jsonObject.get("category").getAsString();
        JsonObject params = jsonObject.getAsJsonObject("params");
        JsonObject body = jsonObject.getAsJsonObject("body");
        log.info(url);
        log.info(params);
        log.info(body);
        String endpoint = properties.get("base.endpoint.url") + "/" + jsonObject.get("category").getAsString();

        switch (method) {
            case "POST": {

                if (url.equals("/order")) {
                    endpoint += url;
                }
                commonResponse = POSTREQUESTS(commonResponse, endpoint, parser, body);
                break;
            }
            case "PUT": {
                commonResponse = PUTREQUESTS(commonResponse, endpoint, parser, body);
                break;
            }
            case "GET": {
                if (url.equals("/findByStatus")) {
                    int len = params.entrySet().size();
                    endpoint += url;
                    for (int i = 0; i < len; i++) {
                        if (i == 0) {
                            endpoint += "?status=" + params.get("param1").getAsString();
                            if (len == 1) break;
                        } else if (len == 2) {
                            endpoint += "&status=" + params.get("param2").getAsString();
                        } else {
                            endpoint += "&status=" + params.get("param3").getAsString();
                        }

                    }
                } else if (url.equals("/")) {
                    if (params.has("petId"))
                        endpoint += "/" + params.get("petId").getAsString();
                    else
                        endpoint += "/" + params.get("username").getAsString();

                } else if (url.equals("/inventory")) {
                    endpoint += url;
                } else if (url.equals("/order")) {
                    endpoint += url + "/" + params.get("param1").getAsString();
                }
                commonResponse = GETREQUESTS(commonResponse, endpoint, parser);
                break;
            }
            case "DELETE": {
                endpoint += url + "/" + params.get("orderId").getAsString();
                commonResponse = DELETEREQUESTS(commonResponse, endpoint, parser);

                break;
            }
            default: {
                break;
            }
        }
        return commonResponse;
    }

    // PUT REQUEST FUNCTION
    public CommonResponse PUTREQUESTS(CommonResponse commonResponse, String endpoint, JSONParser parser, JsonObject body) throws IOException, ParseException {
        httpPut = new HttpPut(endpoint);
        stringEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);
        CloseableHttpResponse res = httpclient.execute(httpPut);
        httpEntity = res.getEntity();
        entityUtils = EntityUtils.toString(httpEntity);
        Object json = (Object) parser.parse(entityUtils);
        status = res.getStatusLine().getStatusCode();
        if (status != 200) {
            commonResponse.setHttpStatus(Integer.toString(status));
            commonResponse.setErrorCode("1");
            commonResponse.setErrorMessage("Something went wrong");
            commonResponse.setResponse(json);
            return commonResponse;
        }
        commonResponse.setHttpStatus(Integer.toString(status));
        commonResponse.setErrorCode("0");
        commonResponse.setErrorMessage("Success");
        commonResponse.setResponse(json);
        return commonResponse;

    }

    // POST REQUEST FUNCTION
    public CommonResponse POSTREQUESTS(CommonResponse commonResponse, String endpoint, JSONParser parser, JsonObject body) throws IOException, ParseException {
        log.info(endpoint);
        httpPost = new HttpPost(endpoint);
        stringEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setConfig(requestConfig);
        CloseableHttpResponse res = httpclient.execute(httpPost);
        httpEntity = res.getEntity();
        entityUtils = EntityUtils.toString(httpEntity);
        Object json = parser.parse(entityUtils);
        status = res.getStatusLine().getStatusCode();
        if (status != 200) {
            commonResponse.setHttpStatus(Integer.toString(status));
            commonResponse.setErrorCode("1");
            commonResponse.setErrorMessage("Something went wrong");
            commonResponse.setResponse(json);
            System.out.println("2");
            return commonResponse;
        }
        commonResponse.setHttpStatus(Integer.toString(status));
        commonResponse.setErrorCode("0");
        commonResponse.setErrorMessage("Success");
        commonResponse.setResponse(json);
        return commonResponse;
    }

    //DELETE REQUEST FUNCTION
    public CommonResponse DELETEREQUESTS(CommonResponse commonResponse, String endpoint, JSONParser parser) throws IOException, ParseException {
        log.info(endpoint);
        httpDelete = new HttpDelete(endpoint);
        CloseableHttpResponse res = httpclient.execute(httpDelete);
        httpEntity = res.getEntity();
        entityUtils = EntityUtils.toString(httpEntity);
        Object json = (Object) parser.parse(entityUtils);
        status = res.getStatusLine().getStatusCode();
        if (status != 200) {
            commonResponse.setHttpStatus(Integer.toString(status));
            commonResponse.setErrorCode("1");
            commonResponse.setErrorMessage("Something went wrong");
            commonResponse.setResponse(json);
            return commonResponse;
        }
        commonResponse.setHttpStatus(Integer.toString(status));
        commonResponse.setErrorCode("0");
        commonResponse.setErrorMessage("Success");
        commonResponse.setResponse(json);
        return commonResponse;
    }

    // GET REQUEST FUNCTION
    public CommonResponse GETREQUESTS(CommonResponse commonResponse, String endpoint, JSONParser parser) throws IOException, ParseException {
        log.info(endpoint);
        httpGet = new HttpGet(endpoint);
        CloseableHttpResponse res = httpclient.execute(httpGet);
        httpEntity = res.getEntity();
        entityUtils = EntityUtils.toString(httpEntity);
        Object json = parser.parse(entityUtils);
        status = res.getStatusLine().getStatusCode();
        if (status != 200) {
            commonResponse.setHttpStatus(Integer.toString(status));
            commonResponse.setErrorCode("1");
            commonResponse.setErrorMessage("Something went wrong");
            commonResponse.setResponse(json);
            return commonResponse;
        }
        commonResponse.setHttpStatus(Integer.toString(status));
        commonResponse.setErrorCode("0");
        commonResponse.setErrorMessage("Success");
        commonResponse.setResponse(json);
        return commonResponse;
    }

    public CommonResponse failedResponses(int httpStatus, String ex) throws Exception {
        log.info(ex);
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setHttpStatus(String.valueOf(httpStatus));
        commonResponse.setErrorCode("-999");
        commonResponse.setErrorMessage(ex);
        commonResponse.setErrorType("Ошибка при обработке запроса");
        return commonResponse;
    }
}
