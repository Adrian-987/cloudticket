package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.ShowAuditDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.vo.ShowAdminVO;
import com.cloudticket.pojo.vo.ShowAuditDetailVO;
import jakarta.validation.Valid;

public interface ShowService extends IService<ShowInfo> {
    PageResult<ShowAdminVO> showPage(ShowPageQuery showPageQuery);

    ShowAuditDetailVO showDetail(Long id);

    void showAudit(Long id, @Valid ShowAuditDTO showAuditDTO);
}
