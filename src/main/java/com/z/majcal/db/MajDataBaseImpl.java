package com.z.majcal.db;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.z.majcal.core.MajContext;
import com.z.majcal.db.service.MajDataBase;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
public class MajDataBaseImpl implements MajDataBase {

    @Override
    public void saveAfterQuery(MajContext majContext) {

        List<MajContext> oldList = queryAllFromFile();
        oldList.add(majContext);
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
        List<MajContext> majContexts = JSONUtil.toList(array,MajContext.class);
        return majContexts;
    }
}
