package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.pojo.dto.AttendeeSaveDTO;
import com.cloudticket.pojo.entity.Attendee;
import com.cloudticket.pojo.vo.AttendeeVO;
import jakarta.validation.Valid;

import java.util.List;

public interface AttendeeService extends IService<Attendee> {
    Long attendeeSave(@Valid AttendeeSaveDTO attendeeSaveDTO);

    void attendeeDelete(Long id);

    void attendeeUpdate(Long id, @Valid AttendeeSaveDTO attendeeSaveDTO);

    List<AttendeeVO> attendeeList();
}
