package st.coomer.util;

/**
 * 文件名提取工具类
 * 负责从URL中提取service和username用于生成文件名
 */
public class FileNameExtractor {

    /**
     * 从URL中提取service名称
     *
     * @param url API URL
     * @return service名称（fansly或onlyfans）
     */
    public static String extractServiceFromUrl(String url) {
        // 例如: https://coomer.st/api/v1/fansly/user/xxx/posts
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("api".equals(parts[i]) && i + 2 < parts.length) {
                return parts[i + 2]; // 返回fansly或onlyfans
            }
        }
        return "unknown";
    }

    /**
     * 从URL中提取username
     *
     * @param url API URL
     * @return username或ID
     */
    public static String extractUsernameFromUrl(String url) {
        // 例如: https://coomer.st/api/v1/fansly/user/549327668156313600/posts
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("user".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1]; // 返回用户名或ID
            }
        }
        return "unknown";
    }

    /**
     * 根据URL生成文件名
     *
     * @param url API URL
     * @return 文件名（格式：service + username + .txt）
     */
    public static String generateFileName(String url) {
        String service = extractServiceFromUrl(url);
        String username = extractUsernameFromUrl(url);
        return service + username + ".txt";
    }
}
