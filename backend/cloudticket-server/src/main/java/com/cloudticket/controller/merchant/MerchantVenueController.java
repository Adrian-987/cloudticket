package com.cloudticket.controller.merchant;

import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.vo.VenueVO;
import com.cloudticket.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/merchant")
public class MerchantVenueController {

    @Autowired
    VenueService venueService;

    @GetMapping("/venues/list")
    public Result<List<VenueVO>> venueList(@RequestParam(required = false) String city){
        List<VenueVO> list=venueService.venueList(city);
        return Result.success(list);
    }
}
