package st.coomer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON解析工具类
 * 负责从JSON响应中提取ID和下载链接
 */
public class JsonParserUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonParserUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从单个帖子详情JSON中提取下载链接
     *
     * @param json JSON字符串
     * @return 下载链接列表，每行一个
     */
    public static String extractDownloadLinks(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            StringBuilder links = new StringBuilder();

            // 获取attachments数组，如果没有则直接返回空字符串
            JsonNode attachments = rootNode.get("attachments");
            if (attachments == null || !attachments.isArray()) {
                return "";
            }

            for (JsonNode attachment : attachments) {
                JsonNode serverNode = attachment.get("server");
                JsonNode pathNode = attachment.get("path");
                JsonNode nameNode = attachment.get("name");

                if (serverNode != null && !serverNode.isNull() &&
                        pathNode != null && !pathNode.isNull()) {

                    String path = pathNode.asText();

                    // 过滤掉jpg和png格式的图片
                    String lowerPath = path.toLowerCase();
                    if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".png")) {
                        continue;
                    }

                    // 拼接完整URL: https://{server}/data/{path}?f={name}
                    StringBuilder fullUrl = new StringBuilder();
                    fullUrl.append("https://");
                    fullUrl.append(serverNode.asText());
                    fullUrl.append("/data");
                    fullUrl.append(path);

                    // 如果有name字段，添加查询参数
                    if (nameNode != null && !nameNode.isNull()) {
                        fullUrl.append("?f=");
                        fullUrl.append(nameNode.asText());
                    }

                    if (!links.isEmpty()) {
                        links.append("\n");
                    }
                    links.append(fullUrl);
                }
            }

            return links.toString();
        } catch (Exception e) {
            log.error("解析下载链接失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 提取JSON数组中所有对象的ID字段
     *
     * @param json JSON字符串
     * @return ID列表，每行一个
     */
    public static String extractIds(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            StringBuilder ids = new StringBuilder();

            // 如果是数组，遍历每个元素
            if (rootNode.isArray()) {
                for (JsonNode item : rootNode) {
                    JsonNode idNode = item.get("id");
                    if (idNode != null && !idNode.isNull()) {
                        if (!ids.isEmpty()) {
                            ids.append("\n");
                        }
                        ids.append(idNode.asText());
                    }
                }
            }

            return ids.toString();
        } catch (Exception e) {
            return "解析失败: " + e.getMessage();
        }
    }
}
