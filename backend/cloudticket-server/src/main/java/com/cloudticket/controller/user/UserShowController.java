package com.cloudticket.controller.user;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.UserShowPageQuery;
import com.cloudticket.pojo.vo.*;
import com.cloudticket.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserShowController {

    @Autowired
    ShowService showService;

    /** 演出分类列表（游客可看，免登录） */
    @GetMapping("/shows/category-list")
    public Result<List<CategoryVO>> showCategory() {
        return Result.success(showService.categoryList());
    }

    @GetMapping("/shows/page")
    public Result<PageResult<UserShowVO>> showUserPage(UserShowPageQuery userShowPageQuery){
        PageResult<UserShowVO> pageResult=showService.showUserPage(userShowPageQuery);
        return Result.success(pageResult);
    }

    /** 演出详情（静态内容，游客可看） */
    @GetMapping("/shows/{id}")
    public Result<UserShowDetailVO> showDetail(@PathVariable Long id) {
        UserShowDetailVO data = showService.showUserDetail(id);
        return Result.success(data);
    }

    @GetMapping("/sessions/{sessionId}/categories")
    public Result<SessionStockVO> showUserStock(@PathVariable Long sessionId){
        SessionStockVO sessionStockVO=showService.showUserStock(sessionId);
        return Result.success(sessionStockVO);
    }

    @GetMapping("/shows/hot")
    public Result<List<UserShowVO>> showHot(){
        List<UserShowVO> showHotVO=showService.showHot();
        return Result.success(showHotVO);
    }
}
