package org.dromara.system.controller.system;

import org.dromara.common.core.domain.R;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一消息中心（消息盒）
 * <p>
 * plus-ui 前端布局登录后拉取消息盒：/resource/message/box
 * 本工程未内置 sys_message 站内信模块，这里将"通知公告(sys_notice)"映射为消息盒内容，
 * 保证前端不报 404 且公告可在消息铃铛展示。
 */
@RestController
@RequestMapping("/resource/message")
public class SysMessageController {

    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 消息盒：systemList / noticeList / workflowList
     */
    @GetMapping("/box")
    public R<Map<String, Object>> box() {
        Map<String, Object> box = new LinkedHashMap<>();
        List<SysNoticeVo> notices = noticeService.selectNoticeList(new SysNoticeBo());
        List<Map<String, Object>> noticeList = notices.stream().map(n -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("messageId", n.getNoticeId());
            item.put("title", n.getNoticeTitle());
            item.put("message", n.getNoticeTitle());
            item.put("content", n.getNoticeContent());
            item.put("category", "notice");
            item.put("type", n.getNoticeType());
            item.put("source", "system");
            item.put("path", "/system/notice");
            item.put("createTime", n.getCreateTime());
            return item;
        }).toList();
        box.put("systemList", List.of());
        box.put("noticeList", noticeList);
        box.put("workflowList", List.of());
        return R.ok(box);
    }
}
