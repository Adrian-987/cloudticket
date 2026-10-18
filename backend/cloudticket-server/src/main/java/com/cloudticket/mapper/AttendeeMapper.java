package com.cloudticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudticket.pojo.entity.Attendee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendeeMapper extends BaseMapper<Attendee> {
}
