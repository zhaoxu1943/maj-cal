package com.z.majcal;

import cn.hutool.core.util.StrUtil;
import com.z.majcal.core.MajCalculate;
import com.z.majcal.core.MajContext;
import com.z.majcal.db.MajDataBaseImpl;
import com.z.majcal.db.service.MajDataBase;
import com.z.majcal.exception.MajException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@SpringBootApplication
@Controller
public class MajCalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MajCalApplication.class, args);
    }




    @PostMapping("/submit")
    public String handleSubmit(@RequestParam("inputValue") String originStr, Model model) {
        MajContext majContext = null;
        try {
            // 在这里执行处理输入值的逻辑
            if (StrUtil.isNotEmpty(originStr)) {
                MajDataBase db = new MajDataBaseImpl();
                MajCalculate cal = new MajCalculate();
                majContext = cal.calculate(originStr);
                db.saveAfterQuery(majContext);
            }
        } catch (MajException e) {
            majContext = new MajContext();
            majContext.setErrorInfo(e.getMessage());
            model.addAttribute("majContext", majContext);
        }

        // 将MajContext对象添加到模型中
        model.addAttribute("majContext", majContext);
        // 返回Thymeleaf模板名称
        return "index";
    }


    @GetMapping("/latest")
    public String getLatestMajContext(Model model) {
        MajDataBase db = new MajDataBaseImpl();
        List<MajContext> majContexts = db.queryAllFromFile();
        majContexts.sort(Comparator.comparing(MajContext::getCreateTime).reversed());
        model.addAttribute("majContexts", majContexts);
        return "index";
    }



}
