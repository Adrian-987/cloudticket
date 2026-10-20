package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.AuditLog;
import com.cloudticket.pojo.vo.AuditRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 演出的审核历史：audit_log LEFT JOIN admin 补审核人名字，
     * 并从 params 参数快照中直接提取 approved / remark（MySQL JSON 函数）。
     */
    List<AuditRecordVO> selectShowAuditRecords(@Param("showId") Long showId);
}
