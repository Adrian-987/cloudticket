package com.cloudticket.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
//serializable接口用于java原生序列化识别
public class PageResult <T> implements Serializable {
    private Long count;
    private List<T> objects;
}
