package st.coomer.util;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * HTTP客户端工具类
 * 负责执行HTTP请求并返回响应
 */
public class HttpClientUtil {

    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7897;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

    /**
     * 执行HTTP GET请求并返回状态码和响应内容
     *
     * @param url 请求URL
     * @return HttpResponse对象，包含状态码和响应内容
     * @throws Exception 异常
     */
    public static HttpResponse executeHttpGetWithStatus(String url) throws Exception {
        HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build()) {

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "text/css");
            httpGet.setHeader("User-Agent", USER_AGENT);

            return httpClient.execute(httpGet, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                return new HttpResponse(statusCode, responseBody);
            });
        }
    }

    /**
     * HTTP响应对象
     */
    public record HttpResponse(int statusCode, String body) {

    }
}
