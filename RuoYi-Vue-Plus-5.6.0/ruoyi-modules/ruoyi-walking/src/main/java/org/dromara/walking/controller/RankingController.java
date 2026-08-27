package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.dromara.common.core.domain.R;
import org.dromara.walking.domain.vo.RankItemVo;
import org.dromara.walking.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排行接口（公开）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /** @param board today=当日排名 total=总排名 points=积分排名 */
    @GetMapping("/list")
    public R<List<RankItemVo>> list(String board) {
        return R.ok(rankingService.getRanking(board == null ? "today" : board));
    }
}
