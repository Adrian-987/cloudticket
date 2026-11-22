package com.cloudticket.common.constant;

/**
 * Redis 缓存 key 统一前缀与模板（P2）
 * 约定：cloudticket:{业务域}:{对象}:{维度...}
 */
public final class CacheKey {

    private CacheKey() {
    }

    public static final String PREFIX = "cloudticket:";

    /** 用户端演出列表：前缀 + city:category:pageNum:pageSize:keyword */
    public static final String SHOW_LIST = PREFIX + "show:list:";

    /** 演出详情（P2 多级缓存用）：前缀 + showId */
    public static final String SHOW_DETAIL = PREFIX + "show:detail:";

    /** 场次票档余量：前缀 + sessionId */
    public static final String SESSION_STOCK = PREFIX + "stock:";

    /** 热门演出 ZSet：前缀 + 日期（yyyyMMdd） */
    public static final String SHOW_HOT = PREFIX + "hot:shows:";
}
