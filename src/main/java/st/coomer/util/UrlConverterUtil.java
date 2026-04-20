package st.coomer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL转换工具类
 * 负责将用户输入的URL转换为API请求URL
 */
public class UrlConverterUtil {

    private static final String URL_PATTERN = "^(https?://coomer\\.st)/(\\w+)/user/(.+)$";

    /**
     * 将用户输入的URL转换为API请求URL
     *
     * @param userUrl 用户输入的URL
     * @return API请求URL
     */
    public static String convertToApiUrl(String userUrl) {
        // 例如: https://coomer.st/fansly/user/549327668156313600
        // 转换为: https://coomer.st/api/v1/fansly/user/549327668156313600/posts

        Pattern pattern = Pattern.compile(URL_PATTERN);
        Matcher matcher = pattern.matcher(userUrl);

        if (matcher.matches()) {
            String baseUrl = matcher.group(1);  // https://coomer.st
            String service = matcher.group(2);  // fansly 或 onlyfans
            String username = matcher.group(3); // 用户名或ID

            return baseUrl + "/api/v1/" + service + "/user/" + username + "/posts";
        }

        // 如果不匹配，返回原始URL
        return userUrl;
    }
}
