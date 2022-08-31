package me.zhangjh.collector.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author zhangjh
 * @date 2022/8/29
 */
@Getter
public enum BiliTypeEnum {

    USER("user", "用户类型"),
    ITEM("item", "节目类型"),
    ;

    private String type;
    private String desc;

    BiliTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static Optional<BiliTypeEnum> getEnumByType(String type) {
        return Arrays.stream(BiliTypeEnum.values())
                .filter(item -> item.type.equals(type))
                .findFirst();
    }
}
