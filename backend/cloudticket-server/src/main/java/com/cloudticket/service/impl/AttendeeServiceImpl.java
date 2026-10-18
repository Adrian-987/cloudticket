package com.cloudticket.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.constant.BaseConstant;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.mapper.AttendeeMapper;
import com.cloudticket.pojo.context.UserContext;
import com.cloudticket.pojo.dto.AttendeeSaveDTO;
import com.cloudticket.pojo.entity.Attendee;
import com.cloudticket.pojo.vo.AttendeeVO;
import com.cloudticket.service.AttendeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttendeeServiceImpl extends ServiceImpl<AttendeeMapper, Attendee>implements AttendeeService {
    @Override
    public Long attendeeSave(AttendeeSaveDTO attendeeSaveDTO) {
        Long userId= BaseConstant.getCurrentId();
        //校验身份证是否正确
        boolean ok = IdcardUtil.isValidCard(attendeeSaveDTO.getIdCardNo());
        if(!ok){
            throw new BaseException("身份证错误");
        }
        //1.校验该身份信息是否存在(同用户和身份证)
        Attendee one = lambdaQuery().eq(Attendee::getUserId, userId)
                .eq(Attendee::getIdCardNo, attendeeSaveDTO.getIdCardNo()).one();
        if(one!=null){
            throw new BaseException("该观演人信息已存在,请勿重复添加");
        }
        //2.该同户的保存的信息数量
        Long count = lambdaQuery().eq(Attendee::getUserId, userId).count();
        if(count>= UserContext.ATTENDEE_COUNT_MAX){
            throw new BaseException("您保存的观演人信息数量达到上限");
        }
        //3.加库返回
        Attendee attendee=new Attendee();
        BeanUtils.copyProperties(attendeeSaveDTO,attendee);
        attendee.setUserId(userId);
        try {
            save(attendee);
        } catch (DuplicateKeyException e) {
            throw new BaseException("该证件号已添加");
        }
        return attendee.getId();
    }

    @Override
    public void attendeeDelete(Long id) {
        Long userId= BaseConstant.getCurrentId();
        LambdaQueryWrapper<Attendee> wrapper = Wrappers.<Attendee>lambdaQuery()
                .eq(Attendee::getUserId, userId)
                .eq(Attendee::getId, id);
        boolean ok = remove(wrapper);
        if(!ok){
            throw new BaseException("观演人不存在");
        }
    }

    @Override
    public void attendeeUpdate(Long id, AttendeeSaveDTO attendeeSaveDTO) {
        Long userId= BaseConstant.getCurrentId();
        String idCardNo = attendeeSaveDTO.getIdCardNo();
        boolean ok = IdcardUtil.isValidCard(idCardNo);
        if(!ok){
            throw new BaseException("身份证错误");
        }
        Long count = lambdaQuery().eq(Attendee::getUserId, userId)
                .eq(Attendee::getIdCardNo, idCardNo)
                .ne(Attendee::getId, id).count();
        if(count>0){
            throw new BaseException("该证件号已添加");
        }
        boolean success= false;
        try {
            success = lambdaUpdate().eq(Attendee::getId, id)
                    .eq(Attendee::getUserId, userId)
                    .set(Attendee::getName, attendeeSaveDTO.getName())
                    .set(Attendee::getPhone, attendeeSaveDTO.getPhone())
                    .set(Attendee::getIdCardNo, attendeeSaveDTO.getIdCardNo())
                    .update();
        } catch (DuplicateKeyException e) {
            throw new BaseException("该证件号已添加");
        }
        if(!success){
            throw new BaseException("该观演人不存在");
        }

    }

    @Override
    public List<AttendeeVO> attendeeList() {
        LambdaQueryWrapper<Attendee> wrapper=Wrappers.<Attendee>lambdaQuery()
                .eq(Attendee::getUserId, BaseConstant.getCurrentId())
                .orderByDesc(Attendee::getId);
        List<Attendee> list = list(wrapper);
        List<AttendeeVO> voList=new ArrayList<>();
        for (Attendee attendee:list){
            AttendeeVO attendeeVO=new AttendeeVO();
            BeanUtils.copyProperties(attendee,attendeeVO);
            attendeeVO.setIdCardNo(DesensitizedUtil.idCardNum(attendee.getIdCardNo(), 4, 4)); // 覆盖成脱敏值
            attendeeVO.setPhone(DesensitizedUtil.mobilePhone(attendee.getPhone()));
            voList.add(attendeeVO);
        }
        return voList;
    }
}
