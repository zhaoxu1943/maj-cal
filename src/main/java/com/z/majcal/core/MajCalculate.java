package com.z.majcal.core;

import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.z.majcal.config.MajPlayer;
import com.z.majcal.entity.dto.MajPlayerResult;
import com.z.majcal.exception.MajException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.z.majcal.config.MajConfig.*;


public class MajCalculate {


    private MajContext context;

    private final List<String> allNickNameList = MajPlayer.haoge.getAllNickName();

    /**
     * 算钱主程序
     *
     * @param originStr 原始ocr串
     * @author zhaoxu
     */
    public MajContext calculate(String originStr) {
        //字符串预处理
        String processStr = preProcess(originStr);

        //计算数据准备
        //获取玩家昵称列表,判定麻将种类
        Set<String> playerSet = playerAnalyse(processStr);
        //字符串分割,解析局数
        List<String> originResultList = convertStringToList(processStr);


        //初始化上下文
        //基于上述信息,初始化上下文
        initMajContext(playerSet, originResultList.size());
        //将每局解析为map
        List<Map<String, Integer>> resultMapList = convertToMap(originResultList);
        //生成对局信息
        createMatchInfo();
        //计算总分,总钱数,如果OCR异常则抛出
        calculateAndValidate(resultMapList);

        //输出结果
        System.out.println(context);
        return context;
    }

    /**
     * 字符串预处理: 解决昵称扫描,与分数无空格,导致扫描不到该玩家的问题
     *
     * @param
     * @return
     * @throws
     * @author zhaoxu
     */
    private String preProcess(String originStr) {

        //字符串预处理: 1. 解决昵称扫描问题 ,采用昵称精确扫描
        for (String nickName : allNickNameList) {
            originStr = originStr.replaceAll(nickName, " " + nickName + " ");

        }
        // 预处理2: 替换名字有数字的id
        String str = originStr.replace("sldk19", "雨神");
        str = str.replace("stephen1943", "zhaoxu");


        //预处理2: 处理n 和位之间的空格
        String pattern = "\\s+位"; // 匹配一个或多个空格后面跟着"位"
        String replacement = "位"; // 替换为"-位"
        str = str.replaceAll(pattern, replacement);


        // 预处理3 : 避免1位,2位,3位,4位,中的数字扫描
        str = str.replace("1位", " 一位");
        str = str.replace("2位", " 二位");
        str = str.replace("3位", " 三位");
        str = str.replace("4位", " 四位");
        return str;
    }


    /**
     * 原始字符串提取玩家
     *
     * @param originStr 原始字符串
     * @author zhaoxu
     */
    private Set<String> playerAnalyse(String originStr) {
        Set<String> matches = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b(" + String.join("|", allNickNameList) + ")\\b");
        Matcher matcher = pattern.matcher(originStr);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }


    /**
     * 对字符串做基础处理
     *
     * @param originStr 原始OCR后字符串
     * @return List<String> 每局形成的List
     * @author zhaoxu
     */
    private static List<String> convertStringToList(String originStr) {
        if (originStr.contains(CHANG_CI)) {
            return spilt(originStr, CHANG_CI);
        } else if (originStr.contains(CHANG_CI_TRAD)) {
            return spilt(originStr, CHANG_CI_TRAD);
        } else if (originStr.contains(CHANG_CI_TRAD_PC)) {
            return spilt(originStr, CHANG_CI_TRAD_PC);
        } else {
            throw new MajException("spilt异常,无友人场或相关字样");
        }

    }

    /**
     * @param originStr 原始OCR后字符串
     * @param spiltStr  分割字符串
     * @author zhaoxu
     */

    private static List<String> spilt(String originStr, String spiltStr) {
        List<String> originList = CharSequenceUtil.splitTrim(originStr, spiltStr);
        List<String> resultList = Lists.newArrayList();
        for (String s : originList) {
            if (CharSequenceUtil.isNotEmpty(s)) {
                resultList.add(s);
            }
        }
        return resultList;
    }


    /**
     * 初始化上下文
     *
     * @param playerSet 玩家集合
     * @author zhaoxu
     */
    private void initMajContext(Set<String> playerSet, int times) {
        context = new MajContext();
        context.setId(UUID.randomUUID().toString());
        context.setPlayerNickNameSet(playerSet);
        context.setType(playerSet.size());
        context.setTime(times);
        if (context.getType() == 3) {
            context.setEveryTenThousandScoreRMB(TYPE_3_EVERY_TEN_THOUSAND_SCORE_RMB);
            context.setScoreEveryTime(TYPE_3_SCORE);
        } else if (context.getType() == 4) {
            context.setEveryTenThousandScoreRMB(TYPE_4_EVERY_TEN_THOUSAND_SCORE_RMB);
            context.setScoreEveryTime(TYPE_4_SCORE);
        }
        context.setCreateTime(LocalDateTime.now());
    }


    /**
     * 输出对局信息
     *
     * @author zhaoxu
     */
    private void createMatchInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("对局信息:\n");
        stringBuilder.append("时间:");
        stringBuilder.append(context.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        stringBuilder.append("\n");
        stringBuilder.append(context.getType());
        stringBuilder.append("人局,");
        stringBuilder.append("玩家:");
        stringBuilder.append(context.getPlayerNickNameSet());
        stringBuilder.append(",");
        stringBuilder.append("共打了");
        stringBuilder.append(context.getTime());
        stringBuilder.append("圈,");
        stringBuilder.append("每万分");
        stringBuilder.append(context.getEveryTenThousandScoreRMB());
        stringBuilder.append("元");
        context.setMatchInfo(stringBuilder.toString());
    }

    /**
     * 算总分,总钱数,如果OCR异常则抛出
     *
     * @param resultMapList 对局Map
     * @throws RuntimeException OCR异常
     * @author zhaoxu
     */
    private void calculateAndValidate(List<Map<String, Integer>> resultMapList) {
        BigDecimal moneyAll = null;
        int sumAll = 0;
        if (context.getType() == 3) {
            moneyAll = context.getEveryTenThousandScoreRMB()
                    .multiply(new BigDecimal(TYPE_3_SCORE))
                    .multiply(new BigDecimal(context.getTime()))
                    .multiply(BigDecimal.valueOf(0.0001))
                    .multiply(new BigDecimal(context.getType())
                            .setScale(4, RoundingMode.HALF_UP));
            sumAll = TYPE_3_SCORE * context.getTime() * context.getType();
        } else if (context.getType() == 4) {
            moneyAll = context.getEveryTenThousandScoreRMB()
                    .multiply(new BigDecimal(TYPE_4_SCORE))
                    .multiply(new BigDecimal(context.getTime()))
                    .multiply(new BigDecimal(context.getType())
                            .multiply(BigDecimal.valueOf(0.0001))
                            .setScale(4, RoundingMode.HALF_UP));
            sumAll = TYPE_4_SCORE * context.getTime() * context.getType();
        }

        int actuaSum = 0;
        // 创建选手结果存储,并根据分数排序
        List<MajPlayerResult> matchResultList = Lists.newArrayList();
        for (String player : context.getPlayerNickNameSet()) {
            MajPlayerResult playerResult = new MajPlayerResult();
            int scoreSum = getSum(resultMapList, player);
            int baseScoreSum = context.getTime() * context.getScoreEveryTime();
            BigDecimal baseMoney = moneyAll.divide(BigDecimal.valueOf(context.getType())).setScale(2, RoundingMode.HALF_UP);
            actuaSum += scoreSum;
            BigDecimal percent = new BigDecimal(scoreSum).divide(new BigDecimal(sumAll), 4, RoundingMode.HALF_UP);
            BigDecimal percentNum = percent.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal money = percent.multiply(moneyAll).setScale(2, RoundingMode.HALF_UP);
            playerResult.setScoreSum(scoreSum);
            playerResult.setPercent(percentNum);
            playerResult.setMoney(money);
            playerResult.setNickName(player);
            playerResult.setWin(scoreSum >= baseScoreSum);
            playerResult.setChangeMoney(money.subtract(baseMoney).abs());
            matchResultList.add(playerResult);
            matchResultList.sort(Comparator.comparing(MajPlayerResult::getScoreSum).reversed());
            //排序后赋予名次
            for (int i = 0; i < matchResultList.size(); i++) {
                matchResultList.get(i).setRank(i + 1);
            }
        }


        context.setMatchResultList(matchResultList);

        List<MajPlayerResult> winnerList = matchResultList.stream().filter(MajPlayerResult::isWin)
                .sorted(Comparator.comparing(MajPlayerResult::getScoreSum).reversed()).collect(Collectors.toList());

        List<MajPlayerResult> loserList = matchResultList.stream().filter(maj -> !maj.isWin())
                .sorted(Comparator.comparing(MajPlayerResult::getScoreSum).reversed()).collect(Collectors.toList());

        context.setWinnerList(winnerList);
        context.setLoserList(loserList);


        //validate ocr数据
        if (actuaSum != sumAll) {
            throw new MajException("扫描异常,扫描总分" + (actuaSum) + "不等于总分" + sumAll + "请重新扫描");
        }

        transCal(winnerList, loserList);


    }

    /**
     * 转账计算
     *
     * @param
     * @return
     * @throws
     * @author zhaoxu
     */
    private void transCal(List<MajPlayerResult> winnerList, List<MajPlayerResult> loserList) {
        //转账计算
        StringBuilder transactionInfo = new StringBuilder();
        if (context.getType() == 3) {
            if (winnerList.size() == 1) {
                //两次
                for (MajPlayerResult loser : loserList) {
                    transactionInfo.append(loser.getNickName())
                            .append(" 向 ")
                            .append(winnerList.get(0).getNickName())
                            .append(" 转 ")
                            .append(loser.getChangeMoney())
                            .append("元\n");
                }
            } else {
                for (MajPlayerResult winner : winnerList) {
                    transactionInfo.append(loserList.get(0).getNickName()).append(" 向 ").append(winner.getNickName()).append(" 转 ").append(winner.getChangeMoney()).append("元\n");
                }
            }
        } else if (context.getType() == 4) {
            if (winnerList.size() == 1) {
                for (MajPlayerResult loser : loserList) {
                    transactionInfo.append(loser.getNickName()).append(" 向 ").append(winnerList.get(0).getNickName()).append(" 转 ").append(loser.getChangeMoney()).append("元\n");
                }

            } else if (winnerList.size() == 2) {
                //first step
                // w1>w2 ,abs(l3)<abs(l4)
                // w1+w2 = abs(l3)+abs(l4)
                //so ,abs(l3)<= w1,即输钱少的人，一定小于等于第一位赢钱的
                transactionInfo.append(loserList.get(0).getNickName()).append(" 向 ").append(winnerList.get(0).getNickName()).append(" 转 ").append(loserList.get(0).getChangeMoney()).append("元\n");

                if (loserList.get(0).getChangeMoney().equals(winnerList.get(0).getChangeMoney())) {
                    transactionInfo.append(loserList.get(0).getNickName()).append(" 向 ").append(winnerList.get(0).getNickName()).append(" 转").append(loserList.get(0).getChangeMoney()).append("元\n");
                } else {
                    transactionInfo.append(loserList.get(1).getNickName()).append(" 向 ").append(winnerList.get(0).getNickName()).append(" 转 ").append(winnerList.get(0).getChangeMoney().subtract(loserList.get(0).getChangeMoney())).append("元\n");
                    transactionInfo.append(loserList.get(1).getNickName()).append(" 向 ").append(winnerList.get(1).getNickName()).append(" 转 ").append(winnerList.get(1).getChangeMoney()).append("元\n");

                }
            } else {
                for (MajPlayerResult winner : winnerList) {
                    transactionInfo.append(loserList.get(0).getNickName()).append(" 向 ").append(winner.getNickName()).append(" 转 ").append(winner.getChangeMoney()).append("元\n");
                }

            }

        }
        context.setTransactionInfo(transactionInfo.toString());
    }

    /**
     * 将spilt后的字符串转换为map
     *
     * @param originResultList 每一局stringList转化为MapList
     * @author zhaoxu
     */

    private List<Map<String, Integer>> convertToMap(List<String> originResultList) {
        List<Map<String, Integer>> resultMapList = Lists.newArrayList();
        for (String originResultString : originResultList) {
            if (CharSequenceUtil.isNotEmpty(originResultString)) {
                resultMapList.add(convertStringToMap(originResultString));
            }
        }
        context.setMatchDetailList(resultMapList);
        return resultMapList;
    }


    /**
     * 将字符串识别为分数Map
     *
     * @param originResultString 每一局的结果
     * @return Map<String, Integer>  key为玩家名字，value为分数
     * @author zhaoxu
     */
    private Map<String, Integer> convertStringToMap(String originResultString) {
        //首先扫描分数
        //有序的分数list
        List<Integer> sortedScoreList = getSortedScoreList(originResultString);
        //扫描玩家次序,形成Map
        return getPlayerMap(originResultString, sortedScoreList);


    }


    private int getSum(List<Map<String, Integer>> resultMapList, String name) {
        int sum = 0;
        for (Map<String, Integer> map : resultMapList) {
            if (map.containsKey(name)) {
                sum += map.get(name);
            }
        }
        return sum;
    }


    /**
     * 获取有序分数列表
     *
     * @param originResultString 原始字符串
     * @author zhaoxu
     */
    private static List<Integer> getSortedScoreList(String originResultString) {
        List<Integer> sortedScoreList = Lists.newArrayList();
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(originResultString);
        while (matcher.find()) {
            String number = matcher.group();
            Integer score = Integer.valueOf(number);
            sortedScoreList.add(score);
        }
        sortedScoreList.sort((o1, o2) -> o2 - o1);
        return sortedScoreList;
    }


    /**
     * 根据原始字符串,分数列表
     * 匹配玩家
     *
     * @param originResultString 原始字符串
     * @param sortedScoreList    有序分数列表
     * @return Map<String, Integer> key为玩家名字，value为分数
     * @author zhaoxu
     */
    private Map<String, Integer> getPlayerMap(String originResultString, List<Integer> sortedScoreList) {
        Map<String, Integer> scoreMap = Maps.newTreeMap();
        String s = originResultString.trim();

        Set<String> nickNameSet = context.getPlayerNickNameSet();
        Map<String, String> rankMap = Maps.newTreeMap();
        for (String nickName : nickNameSet) {
            int index = s.indexOf(nickName);
            //寻找玩家名字前面的排名
            int rankIndex = index - 1;
            while (" ".equals(String.valueOf(s.charAt(rankIndex)))) {
                rankIndex--;
            }
            //SubString,左闭右开[)
            String rank = s.substring(rankIndex - 1, rankIndex + 1);
            rankMap.put(rank, nickName);
        }

        for (int i = 0; i < sortedScoreList.size(); i++) {
            String rank;
            switch (i) {
                case 0:
                    rank = "一位";
                    break;
                case 1:
                    rank = "二位";
                    break;
                case 2:
                    rank = "三位";
                    break;
                case 3:
                    rank = "四位";
                    break;
                default:
                    rank = "异常";
            }
            String nickName = rankMap.get(rank);
            scoreMap.put(nickName, sortedScoreList.get(i));
        }
        return scoreMap;
    }


}



