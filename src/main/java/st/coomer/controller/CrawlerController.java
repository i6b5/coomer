package st.coomer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import st.coomer.service.PostCrawlerService;
import st.coomer.util.UrlConverterUtil;

/**
 * 爬虫控制器
 * 处理用户请求并触发异步抓取任务
 */
@Controller
public class CrawlerController {

    private final PostCrawlerService postCrawlerService;

    public CrawlerController(PostCrawlerService postCrawlerService) {
        this.postCrawlerService = postCrawlerService;
    }

    /**
     * 根路径，跳转到主页面
     */
    @GetMapping("/")
    public String home() {
        return "main";
    }

    /**
     * 处理执行请求 - 异步执行
     */
    @GetMapping("/execute")
    public String execute(@RequestParam(value = "url", required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            // 启动异步任务
            new Thread(() -> {
                try {
                    String apiUrl = UrlConverterUtil.convertToApiUrl(query.trim());
                    postCrawlerService.executeCrawlTask(apiUrl);
                } catch (Exception e) {
                    throw new RuntimeException("执行任务失败", e);
                }
            }).start();

            model.addAttribute("message", "提交成功！任务已在后台执行。");
        }

        return "main";
    }
}
