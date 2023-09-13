package com.z.majcal.core;

import cn.hutool.core.util.StrUtil;
import com.z.majcal.entity.dto.MajPlayerResult;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * maj上下文,
 * 初始化对局者
 *
 * @author zhaoxu
 */
@Data
public class MajContext {


    private String id;

    /**
     * 玩家昵称列表
     *
     * @author zhaoxu
     */
    private Set<String> playerNickNameSet;

    /**
     * 玩家排名map
     *
     * @author zhaoxu
     */
    private Map<String, Integer> allScoreMap;

    /**
     * 玩家数,根据上下文判定三麻还是四麻
     *
     * @author zhaoxu
     */
    private int type;

    /**
     * 每万分多少钱
     *
     * @param
     * @return
     * @throws
     * @author zhaoxu
     */
    private BigDecimal everyTenThousandScoreRMB;

    /**
     * 局数
     *
     * @author zhaoxu
     */
    private int time;

    /**
     * 每局点数
     *
     * @author zhaoxu
     */
    private int scoreEveryTime;

    private LocalDateTime createTime;


    //计算后的字段
    private String matchInfo;

    private List<Map<String, Integer>> matchDetailList;

    private List<MajPlayerResult> matchResultList;


    private List<MajPlayerResult> winnerList;

    private List<MajPlayerResult> loserList;

    private String transactionInfo;

    private String errorInfo;


    @Override
    public String toString() {
        if (StrUtil.isNotEmpty(errorInfo)) {
            return errorInfo;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(matchInfo)
                .append("\n分数详情:\n");

        for (Map<String, Integer> map : matchDetailList) {
            stringBuilder.append(map)
                    .append("\n");
        }
        stringBuilder.append("对局结果:\n");

        stringBuilder.append("赢钱:\n");
        for (MajPlayerResult s : winnerList) {
            stringBuilder.append(s)
                    .append("\n");
        }

        stringBuilder.append("输钱:\n");
        for (MajPlayerResult s : loserList) {
            stringBuilder.append(s)
                    .append("\n");
        }

        stringBuilder.append("转账信息:\n");
        stringBuilder.append(transactionInfo);


        return stringBuilder.toString();
    }

    public String getChangeByNickName(String nickName) {
        StringBuilder resultStr = new StringBuilder();
        for (MajPlayerResult result : matchResultList) {
            if (nickName.equals(result.getNickName())) {
                String temp = result.isWin() ? "+" : "-";
                resultStr.append(temp);
                resultStr.append(result.getChangeMoney());
                resultStr.append("元");
            }
        }
        return resultStr.toString();
    }

}
