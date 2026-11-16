package com.cloudticket.pojo.enums;

import lombok.Getter;

import java.util.List;

/**
 * 演出分类（show_info.category）
 * 有限取值 → 存数字 code，中文 label 只在展示层用（和 ShowStatus 一致）。
 */
@Getter
public enum ShowCategory {

    CONCERT(1, "演唱会"),
    DRAMA(2, "话剧"),
    MUSICAL(3, "音乐会"),
    TALK_SHOW(4, "脱口秀"),
    FESTIVAL(5, "音乐节"),
    OTHER(6, "其他");

    private final int code;
    private final String label;

    // 手写构造器（枚举 + Lombok 构造器曾踩过坑）
    ShowCategory(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 按数据库存储的数字反查枚举，未匹配返回 null */
    public static ShowCategory of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShowCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return null;
    }

    /** 全部分类（分类列表接口 / 前端下拉 / 创建校验共用，顺序 = 声明顺序） */
    public static List<ShowCategory> all() {
        return List.of(values());
    }

    /** 全部标签（只要文案的场景） */
    public static List<String> labels() {
        return all().stream().map(ShowCategory::getLabel).toList();
    }
}
