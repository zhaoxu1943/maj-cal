package com.z.majcal.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MajPlayerResult implements Comparable<MajPlayerResult> {


    private Integer scoreSum;

    private BigDecimal percent;

    private BigDecimal money;

    private BigDecimal changeMoney;

    private String nickName;

    private Integer rank;

    private boolean isWin;

    @Override
    public int compareTo(MajPlayerResult o) {
        return o.scoreSum - this.scoreSum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("第").append(rank).append("名:").append(nickName)
                .append(",总点数:").append(scoreSum)
                .append(",总金额:").append(money).append("元")
                .append(isWin ? ",狂赚" : ",输钱")
                .append(changeMoney)
                .append("元")
                .append(isWin ? "!!!" : "");
        return sb.toString();
    }
}
