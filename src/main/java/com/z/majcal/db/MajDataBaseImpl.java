package com.z.majcal.db;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.z.majcal.core.MajContext;
import com.z.majcal.db.service.MajDataBase;
import com.z.majcal.entity.dto.MajPlayerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MajDataBaseImpl implements MajDataBase {

    @Override
    public void saveAfterQuery(MajContext majContext) {

        List<MajContext> oldList = queryAllFromFile();
        //保存最新的查询结果
        //根据对局hash-id去重
        //获取所有id
        List<String> idList = oldList.stream().map(MajContext::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(idList)) {
            if (!idList.contains(majContext.getId())) {
                //如果不包含，添加
                oldList.add(majContext);
            } else {
                log.info("已经存在相同的对局,不再保存,hash:{}", majContext.getId());
            }
        }

        JSONArray array = JSONUtil.parseArray(oldList);
        String majContextJsonStr = JSONUtil.toJsonStr(array);

        // 创建目录
        File directory = new File("maj-db");
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.out.println("Failed to create directory");
                return;
            }
        }

        // 创建文件
        File file = new File(directory, "maj.db");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(majContextJsonStr);
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to create file: " + e.getMessage());
        }
    }

    @Override
    public List<MajContext> queryAllFromFile() {
        JSONArray array = JSONUtil.readJSONArray(new File("maj-db/maj.db"), StandardCharsets.UTF_8);
        List<MajContext> majContexts = JSONUtil.toList(array, MajContext.class);
        return majContexts;
    }


    /**
     * 分析历史对局
     *
     * @param
     * @return
     * @throws
     * @author zhaoxu
     */
    @Override
    public List<String> analyzeHistoryMaj(List<MajContext> majContexts) {
        List<MajContext> oldList = queryAllFromFile();
        //提取对局信息集合(每一圈)
        List<MajPlayerResult> majPlayerResultList = oldList.stream().flatMap(majContext -> majContext.getMatchResultList().stream()).collect(Collectors.toList());

        //使用map统计历史总输赢
        Map<String, BigDecimal> historyMoneyMap = Maps.newTreeMap();
        for (MajPlayerResult majPlayerResult : majPlayerResultList) {
            String nickName = majPlayerResult.getNickName();
            BigDecimal money = majPlayerResult.getChangeMoney();
            if (historyMoneyMap.containsKey(nickName)) {
                BigDecimal oldMoney = historyMoneyMap.get(nickName);
                if (majPlayerResult.isWin()) {
                    historyMoneyMap.put(nickName, oldMoney.add(money));
                } else {
                    historyMoneyMap.put(nickName, oldMoney.subtract(money));
                }
            } else {
                if (majPlayerResult.isWin()) {
                    historyMoneyMap.put(nickName, money);
                } else {
                    historyMoneyMap.put(nickName, money.negate());
                }
            }
        }
        List<Map.Entry<String, BigDecimal>> mapList = new ArrayList<>(historyMoneyMap.entrySet());
        mapList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        List<MajContext> majContextList = majContexts.subList(majContexts.size() - 5, majContexts.size());
        return mapList.stream().map(entry -> {
            StringBuilder result = new StringBuilder();
            result.append(entry.getKey());
            result.append(" : ");
            result.append(entry.getValue());
            result.append("元  ,最近五局: ");
            List<String> tendency = majContextList.stream().map(maj -> maj.getChangeByNickName(entry.getKey())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(tendency)) {
                for (String s : tendency) {
                    result.append(s);
                    result.append(" ");
                }
            }
            return result.toString();
        }).collect(Collectors.toList());
    }
}
