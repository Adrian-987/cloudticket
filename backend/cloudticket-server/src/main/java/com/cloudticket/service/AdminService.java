package com.cloudticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudticket.pojo.dto.AdminLoginDTO;
import com.cloudticket.pojo.entity.Admin;
import com.cloudticket.pojo.vo.AdminInfoVO;
import com.cloudticket.pojo.vo.AdminLoginVO;
import jakarta.validation.Valid;

public interface AdminService extends IService<Admin> {
    AdminLoginVO adminLogin(@Valid AdminLoginDTO adminLoginDTO);

    AdminInfoVO adminInfo();
}
