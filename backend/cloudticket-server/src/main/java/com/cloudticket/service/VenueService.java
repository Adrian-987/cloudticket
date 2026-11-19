package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.pojo.dto.AdminVenuePageQuery;
import com.cloudticket.pojo.dto.AdminVenueSaveDTO;
import com.cloudticket.pojo.entity.Venue;
import com.cloudticket.pojo.vo.IdVO;
import com.cloudticket.pojo.vo.AdminVenueVO;
import com.cloudticket.pojo.vo.VenueVO;

import java.util.List;

public interface VenueService extends IService<Venue> {
    PageResult<VenueVO> venuePageQuery(AdminVenuePageQuery venuePageQuery);

    IdVO venueSave(AdminVenueSaveDTO venueSaveDTO);

    void venueUpdate(Long id,AdminVenueSaveDTO venueSaveDTO);

    void venueDelete(Long id);

    AdminVenueVO venueInfo(Long id);

    List<VenueVO> venueList(String city);
}
