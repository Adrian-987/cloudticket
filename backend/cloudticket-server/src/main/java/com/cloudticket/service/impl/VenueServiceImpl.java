package com.cloudticket.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudticket.common.exception.BaseException;
import com.cloudticket.common.result.PageResult;
import com.cloudticket.mapper.ShowInfoMapper;
import com.cloudticket.mapper.VenueMapper;
import com.cloudticket.pojo.dto.VenuePageQuery;
import com.cloudticket.pojo.dto.VenueSaveDTO;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.pojo.entity.Venue;
import com.cloudticket.pojo.vo.IdVO;
import com.cloudticket.pojo.vo.VenueInfoVO;
import com.cloudticket.pojo.vo.VenueVO;
import com.cloudticket.service.VenueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue>implements VenueService {

    @Autowired
    ShowInfoMapper showInfoMapper;

    @Override
    public PageResult<VenueVO> venuePageQuery(VenuePageQuery venuePageQuery) {
        Page<Venue> page=new Page<>(venuePageQuery.getPageNum(),venuePageQuery.getPageSize());
        String name = venuePageQuery.getName();
        String city=venuePageQuery.getCity();
        page=lambdaQuery()
                .like(!StrUtil.isBlank(name),Venue::getName,name)
                .eq(!StrUtil.isBlank(city),Venue::getCity,city)
                .orderByDesc(Venue::getId)
                .page(page);
        List<VenueVO> list=page.getRecords().stream().map(venue -> {
            VenueVO venueVO=new VenueVO();
            BeanUtils.copyProperties(venue,venueVO);
            return venueVO;
        }).toList();
        return PageResult.of(page.getTotal(),list);
    }

    @Override
    public IdVO venueSave(VenueSaveDTO venueSaveDTO) {
        Venue one = lambdaQuery().eq(Venue::getCity, venueSaveDTO.getCity())
                .eq(Venue::getName, venueSaveDTO.getName())
                .one();
        if(one!=null){
            throw new BaseException("该城市已经存在同名场馆");
        }
        Venue venue=new Venue();
        BeanUtils.copyProperties(venueSaveDTO,venue);
        try {
            save(venue);
        } catch (DuplicateKeyException e) {
            throw new BaseException("该场馆已存在");
        }
        return new IdVO(venue.getId());
    }

    @Override
    public void venueUpdate(Long id,VenueSaveDTO venueSaveDTO) {
        Long dup = lambdaQuery()
                .eq(Venue::getCity, venueSaveDTO.getCity())
                .eq(Venue::getName, venueSaveDTO.getName())
                .ne(Venue::getId, id)          // 排除自己
                .count();
        if (dup > 0){
            throw new BaseException("该城市已经存在同名场馆");
        }
        Venue venue = new Venue();
        BeanUtils.copyProperties(venueSaveDTO, venue);
        venue.setId(id);
        boolean success = false;
        try {
            success = updateById(venue);
        } catch (DuplicateKeyException e) {
            throw new BaseException("修改失败,请重新尝试");
        }
        if (!success) throw new BaseException("场馆不存在");
    }

    @Override
    public void venueDelete(Long id) {
        Long count = showInfoMapper.selectCount(new LambdaQueryWrapper<ShowInfo>().eq(ShowInfo::getVenueId, id));
        if(count>0){
            throw new BaseException("该场馆已被演出引用，无法删除");
        }
        boolean ok = removeById(id);
        if(!ok){
            throw new BaseException("场馆不存在");
        }
    }

    @Override
    public VenueInfoVO venueInfo(Long id) {
        Venue venue = getById(id);
        if(venue==null){
            throw new BaseException("该场馆不存在");
        }
        VenueInfoVO venueInfoVO=new VenueInfoVO();
        BeanUtils.copyProperties(venue,venueInfoVO);
        Long count = showInfoMapper.selectCount(new LambdaQueryWrapper<ShowInfo>().eq(ShowInfo::getVenueId, id));
        venueInfoVO.setShowCount(count.intValue());
        return venueInfoVO;
    }
}
