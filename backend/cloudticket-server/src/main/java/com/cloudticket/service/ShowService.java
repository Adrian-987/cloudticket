package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.ShowAuditDTO;
import com.cloudticket.pojo.dto.ShowCreateDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.vo.*;
import jakarta.validation.Valid;

public interface ShowService extends IService<ShowInfo> {
    PageResult<ShowAdminVO> showAdminPage(ShowPageQuery showPageQuery);

    ShowAuditDetailVO showAdminDetail(Long id);

    void showAudit(Long id, @Valid ShowAuditDTO showAuditDTO);

    IdVO showcreate(@Valid ShowCreateDTO showCreateDTO);

    void showUpdate(Long id, @Valid ShowCreateDTO showCreateDTO);

    PageResult<ShowMerchantVO> showMerchantPage(ShowPageQuery showPageQuery);

    ShowMerchantDetailVO showMerchantDetail(Long id);
}
