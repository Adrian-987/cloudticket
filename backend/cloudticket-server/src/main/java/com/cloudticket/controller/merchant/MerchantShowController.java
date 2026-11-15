package com.cloudticket.controller.merchant;

import com.cloudticket.common.result.PageResult;
import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.ShowCreateDTO;
import com.cloudticket.pojo.dto.ShowPageQuery;
import com.cloudticket.pojo.vo.IdVO;
import com.cloudticket.pojo.vo.ShowMerchantDetailVO;
import com.cloudticket.pojo.vo.ShowMerchantVO;
import com.cloudticket.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/merchant")
@RestController
public class MerchantShowController {

    @Autowired
    ShowService showService;

    @PostMapping("/shows")
    public Result<IdVO> showCreate(@RequestBody @Valid ShowCreateDTO showCreateDTO){
        IdVO idVO=showService.showcreate(showCreateDTO);
        return Result.success(idVO);
    }

    @PutMapping("/shows/{id}")
    public Result showUpdate(@RequestBody @Valid ShowCreateDTO showCreateDTO, @PathVariable Long id){
            showService.showUpdate(id,showCreateDTO);
            return Result.success();
    }

    @GetMapping("/shows/page")
    public Result<PageResult<ShowMerchantVO>> showPage(ShowPageQuery showPageQuery){
        PageResult<ShowMerchantVO> pageResult=showService.showMerchantPage(showPageQuery);
        return Result.success(pageResult);
    }

    @GetMapping("/shows/{id}")
    public Result<ShowMerchantDetailVO> showDetail(@PathVariable Long id){
        ShowMerchantDetailVO showMerchantDetailVO=showService.showMerchantDetail(id);
        return Result.success(showMerchantDetailVO);
    }

    @PostMapping("/shows/{id}/submit")
    public Result showReview(@PathVariable Long id){
        showService.showReview(id);
        return Result.success();
    }

    @PostMapping("/shows/{id}/off-sale")
    public Result showOffSale(@PathVariable Long id){
        showService.showOffSale(id);
        return Result.success();
    }
}
