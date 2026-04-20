package st.coomer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import st.coomer.util.FileNameExtractor;
import st.coomer.util.HttpClientUtil;
import st.coomer.util.JsonParserUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子抓取服务
 * 负责执行分页请求、收集帖子ID、获取详情并保存下载链接
 */
@Service
public class PostCrawlerService {

    private static final Logger log = LoggerFactory.getLogger(PostCrawlerService.class);
    private static final int MAX_RETRY = 3;
    private static final int RETRY_DELAY_MS = 300;
    private static final int REQUEST_INTERVAL_MS = 300;
    private static final int DETAIL_REQUEST_INTERVAL_MS = 100;
    private static final int PAGE_SIZE = 50;

    /**
     * 执行分页抓取任务
     *
     * @param baseUrl API基础URL
     */
    public void executeCrawlTask(String baseUrl) {
        try {
            log.info("========== 开始执行任务 ==========");
            log.info("API地址: {}", baseUrl);

            // 第一步：收集所有帖子ID
            List<String> allIds = collectAllPostIds(baseUrl);

            if (allIds.isEmpty()) {
                log.info("没有收集到任何帖子ID，任务结束");
                return;
            }

            log.info("共收集到 {} 个帖子ID", allIds.size());

            // 第二步：获取帖子详情并提取下载链接
            List<String> downloadLinks = collectDownloadLinks(allIds, baseUrl);

            log.info("共提取到 {} 个下载链接", downloadLinks.size());

            // 第三步：保存到文件
            saveToFile(downloadLinks, baseUrl);

            log.info("========== 任务执行完成 ==========");

        } catch (Exception e) {
            log.error("执行任务失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 收集所有帖子ID
     *
     * @param baseUrl API基础URL
     * @return 帖子ID列表
     */
    private List<String> collectAllPostIds(String baseUrl) throws InterruptedException {
        List<String> allIds = new ArrayList<>();
        int offset = 0;

        while (true) {
            String url = offset == 0 ? baseUrl : baseUrl + "?o=" + offset;

            boolean success = false;
            String ids = null;

            // 重试最多3次
            for (int retry = 0; retry < MAX_RETRY; retry++) {
                try {
                    HttpClientUtil.HttpResponse response = HttpClientUtil.executeHttpGetWithStatus(url);

                    if (response.statusCode() == 200) {
                        ids = JsonParserUtil.extractIds(response.body());
                        success = true;
                        break;
                    }

                    log.warn("响应码: {}, 第{}次重试", response.statusCode(), retry + 1);
                    Thread.sleep(RETRY_DELAY_MS);

                } catch (Exception e) {
                    log.error("请求异常: {}", e.getMessage());
                    Thread.sleep(RETRY_DELAY_MS);
                }
            }

            if (!success) {
                log.info("达到最大重试次数，停止请求");
                break;
            }

            if (ids.isEmpty()) {
                break;
            }

            // 添加ID到列表
            String[] idArray = ids.split("\n");
            for (String id : idArray) {
                if (!id.trim().isEmpty()) {
                    allIds.add(id.trim());
                }
            }

            offset += PAGE_SIZE;
            Thread.sleep(REQUEST_INTERVAL_MS);
        }

        return allIds;
    }

    /**
     * 收集所有下载链接
     *
     * @param allIds  帖子ID列表
     * @param baseUrl API基础URL
     * @return 下载链接列表
     */
    private List<String> collectDownloadLinks(List<String> allIds, String baseUrl) {
        List<String> downloadLinks = new ArrayList<>();
        String baseApiUrl = baseUrl.replaceAll("/posts$", "");

        for (int i = 0; i < allIds.size(); i++) {
            String postId = allIds.get(i);
            String postUrl = baseApiUrl + "/post/" + postId;

            log.info("[{}] 开始处理帖子 {}/{}: {}", System.currentTimeMillis(), i + 1, allIds.size(), postId);

            try {
                HttpClientUtil.HttpResponse response = HttpClientUtil.executeHttpGetWithStatus(postUrl);

                if (response.statusCode() == 200) {
                    String links = JsonParserUtil.extractDownloadLinks(response.body());
                    if (!links.isEmpty()) {
                        String[] linkArray = links.split("\n");
                        int linkCount = 0;
                        for (String link : linkArray) {
                            if (!link.trim().isEmpty()) {
                                downloadLinks.add(link.trim());
                                linkCount++;
                            }
                        }
                        log.info("[{}] 帖子 {} 提取到 {} 个下载链接", System.currentTimeMillis(), postId, linkCount);
                    } else {
                        log.info("[{}] 帖子 {} 没有下载链接", System.currentTimeMillis(), postId);
                    }
                } else {
                    log.info("[{}] 帖子 {} 请求失败 (状态码: {})", System.currentTimeMillis(), postId, response.statusCode());
                }

                log.info("[{}] 完成进度: {}/{}", System.currentTimeMillis(), i + 1, allIds.size());

                Thread.sleep(DETAIL_REQUEST_INTERVAL_MS);

            } catch (Exception e) {
                log.error("[{}] 获取帖子 {} 详情失败: {}", System.currentTimeMillis(), postId, e.getMessage());
                log.info("[{}] 完成进度: {}/{}", System.currentTimeMillis(), i + 1, allIds.size());
            }
        }

        return downloadLinks;
    }

    /**
     * 保存下载链接到文件
     *
     * @param downloadLinks 下载链接列表
     * @param baseUrl       API基础URL
     */
    private void saveToFile(List<String> downloadLinks, String baseUrl) {
        String fileName = FileNameExtractor.generateFileName(baseUrl);

        Path downloadsDir = Paths.get("downloads");
        if (!Files.exists(downloadsDir)) {
            try {
                Files.createDirectories(downloadsDir);
            } catch (IOException e) {
                log.error("创建downloads目录失败: {}", e.getMessage(), e);
                return;
            }
        }

        Path filePath = downloadsDir.resolve(fileName);
        log.info("结果将保存到文件: {}", filePath.toAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (String link : downloadLinks) {
                writer.write(link);
                writer.newLine();
            }
            writer.flush();
            log.info("文件保存成功: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("保存文件失败: {}", e.getMessage(), e);
        }
    }
}
