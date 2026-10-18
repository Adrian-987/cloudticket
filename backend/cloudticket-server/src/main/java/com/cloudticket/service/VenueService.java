package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.VenuePageQuery;
import com.cloudticket.pojo.dto.VenueSaveDTO;
import com.cloudticket.pojo.entity.Venue;
import com.cloudticket.pojo.vo.IdVO;
import com.cloudticket.pojo.vo.VenueInfoVO;
import com.cloudticket.pojo.vo.VenueVO;

public interface VenueService extends IService<Venue> {
    PageResult<VenueVO> venuePageQuery(VenuePageQuery venuePageQuery);

    IdVO venueSave(VenueSaveDTO venueSaveDTO);

    void venueUpdate(Long id,VenueSaveDTO venueSaveDTO);

    void venueDelete(Long id);

    VenueInfoVO venueInfo(Long id);
}
