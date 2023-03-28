package com.z.majcal.config;

import com.google.common.collect.Lists;

import java.util.List;

public enum MajPlayer {

    zhaoxu("zhaoxu"),
    zhengyang("吃肉的山羊"),
    zhanglei("毒瘤小张"),
    haoge("FeiClusell"),
    zhaolu("清寒亦寒"),
    wangyu("雨神"),
    hengshen("无敌衡神");

    private String nickName;

    MajPlayer(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public List<String> getAllNickName() {
        List<String> all = Lists.newArrayList();
        for (MajPlayer m:MajPlayer.values()){
            all.add(m.getNickName());
        }
        return all;
    }

}
