package com.cloudticket.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudticket.mapper.ShowInfoMapper;
import com.cloudticket.pojo.entity.ShowInfo;
import com.cloudticket.service.ShowService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BloomWarmUpRunner implements ApplicationRunner {

    @Autowired
    RBloomFilter<Long> showBloomFilter;

    @Autowired
    ShowInfoMapper showInfoMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // ① 只查 id 列（轻量，不要 select *）
        List<Long> ids = showInfoMapper.selectList(
                        new LambdaQueryWrapper<ShowInfo>().select(ShowInfo::getId))
                .stream().map(ShowInfo::getId).toList();

        // ② 逐个加（add 幂等：重复加不会产生重复/错误，重启多次没关系）
        ids.forEach(showBloomFilter::add);

        log.info("演出布隆过滤器预热完成：{} 个 id", ids.size());
    }
}
