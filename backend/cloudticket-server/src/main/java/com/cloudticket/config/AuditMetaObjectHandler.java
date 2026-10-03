package com.cloudticket.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloudticket.common.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MP 内置 CRUD（BaseMapper/IService.save 等）的审计字段自动填充。
 * 生效条件：实体审计字段标注 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE)。
 *
 * 责任划分：
 *   BaseMapper / IService 内置方法 → 本类（MP 参数处理阶段填充）
 *   自定义 XML SQL                → @AutoFill AOP 切面
 *
 * strictXxxFill 语义：字段已有值则跳过，因此两条链路同时存在也不会互相覆盖。
 * updateUser 取自 ThreadLocal，定时任务/异步线程中为 null 属预期。
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = BaseContext.getCurrentId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createUser", Long.class, userId);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateUser", Long.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId());
    }
}
