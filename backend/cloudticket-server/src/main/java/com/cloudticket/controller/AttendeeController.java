package com.cloudticket.controller;

import com.cloudticket.common.result.Result;
import com.cloudticket.pojo.dto.AttendeeSaveDTO;
import com.cloudticket.pojo.vo.AttendeeVO;
import com.cloudticket.pojo.vo.IdVO;
import com.cloudticket.service.AttendeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/use")
public class AttendeeController {
    @Autowired
    AttendeeService attendeeService;

    @PostMapping("/attendee")
    public Result<IdVO> attendeeSave(@RequestBody @Valid AttendeeSaveDTO attendeeSaveDTO){
        Long id=attendeeService.attendeeSave(attendeeSaveDTO);
        return Result.success(new IdVO(id));
    }

    @DeleteMapping("/attendee/{id}")
    public Result attendeeDelect(@PathVariable Long id){
        attendeeService.attendeeDelete(id);
        return Result.success();
    }

    @PutMapping("/attendee/{id}")
    public Result attendeeUpdate(@PathVariable Long id,@RequestBody @Valid AttendeeSaveDTO attendeeSaveDTO){
        attendeeService.attendeeUpdate(id,attendeeSaveDTO);
        return Result.success();
    }

    @GetMapping("/attendee/list")
    public Result<List<AttendeeVO>> attendeeList(){
        List<AttendeeVO> list=attendeeService.attendeeList();
        return Result.success(list);
    }
}
