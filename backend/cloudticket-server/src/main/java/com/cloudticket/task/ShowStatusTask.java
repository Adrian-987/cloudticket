package com.cloudticket.task;

import com.cloudticket.mapper.SessionMapper;
import com.cloudticket.mapper.ShowInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShowStatusTask {

    @Autowired
    ShowInfoMapper showMapper;

    @Autowired
    SessionMapper sessionMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void flowStatus(){
        try {
            int s1 = sessionMapper.startSale();    // 场次 0→1
            int s2 = sessionMapper.endSession();   // 场次 1→2
            int i1 = showMapper.startSale();       // 演出 3→4
            int i2 = showMapper.endShow();         // 演出 4→5
            if (s1 + s2 + i1 + i2 > 0) {
                log.info("状态流转：场次开售{} 场次结束{} 演出开售{} 演出结束{}", s1, s2, i1, i2);
            }
        } catch (Exception e) {
            log.error("状态流转任务失败", e);   // 吞掉异常，不影响下一次调度
        }
    }
}
