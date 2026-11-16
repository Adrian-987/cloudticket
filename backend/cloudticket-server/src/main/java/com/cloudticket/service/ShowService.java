package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.ShowAuditDTO;
import com.cloudticket.pojo.dto.ShowCreateDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.dto.UserShowPageQuery;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.vo.*;
import jakarta.validation.Valid;

import java.util.List;

public interface ShowService extends IService<ShowInfo> {
    PageResult<ShowAdminVO> showAdminPage(ShowPageQuery showPageQuery);

    ShowAuditDetailVO showAdminDetail(Long id);

    void showAudit(Long id, @Valid ShowAuditDTO showAuditDTO);

    IdVO showcreate(@Valid ShowCreateDTO showCreateDTO);

    void showUpdate(Long id, @Valid ShowCreateDTO showCreateDTO);

    PageResult<ShowMerchantVO> showMerchantPage(ShowPageQuery showPageQuery);

    ShowMerchantDetailVO showMerchantDetail(Long id);

    void showReview(Long id);

    void showOffSale(Long id);

    /** 演出分类列表（03 浏览：前端下拉用） */
    List<CategoryVO> categoryList();

    /** 用户端演出详情（静态：演出 + 场馆 + 场次骨架） */
    UserShowDetailVO showUserDetail(Long id);

    PageResult<UserShowVO> showUserPage(UserShowPageQuery userShowPageQuery);

    SessionStockVO showUserStock(Long sessionId);
}
