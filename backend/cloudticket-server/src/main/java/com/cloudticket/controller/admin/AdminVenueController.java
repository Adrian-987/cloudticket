package com.cloudticket.controller.admin;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AdminVenuePageQuery;
import com.cloudticket.pojo.dto.AdminVenueSaveDTO;
import com.cloudticket.pojo.vo.IdVO;
import com.cloudticket.pojo.vo.AdminVenueVO;
import com.cloudticket.pojo.vo.VenueVO;
import com.cloudticket.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminVenueController {

    @Autowired
    VenueService venueService;

    @GetMapping("/venues/page")
    public Result<PageResult<VenueVO>> venuePageQuery(AdminVenuePageQuery venuePageQuery){
        PageResult<VenueVO> pageResult=venueService.venuePageQuery(venuePageQuery);
        return Result.success(pageResult);
    }

    @PostMapping("/venues")
    public Result<IdVO> venueSave(@RequestBody @Valid AdminVenueSaveDTO venueSaveDTO){
        IdVO idVO=venueService.venueSave(venueSaveDTO);
        return Result.success(idVO);
    }

    @PutMapping("/venues/{id}")
    public Result venueUpdate(@PathVariable Long id,@RequestBody @Valid AdminVenueSaveDTO venueSaveDTO){
        venueService.venueUpdate(id,venueSaveDTO);
        return Result.success();
    }

    @DeleteMapping("/venues/{id}")
    public Result venueDelete(@PathVariable Long id){
        venueService.venueDelete(id);
        return Result.success();
    }

    @GetMapping("/venues/{id}")
    public Result<AdminVenueVO> venueInfo(@PathVariable Long id){
        AdminVenueVO venueInfoVO=venueService.venueInfo(id);
        return Result.success(venueInfoVO);
    }
}
