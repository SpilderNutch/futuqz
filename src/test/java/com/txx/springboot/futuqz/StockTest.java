package com.txx.springboot.futuqz;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.txx.springboot.futuqz.entity.Stock;
import com.txx.springboot.futuqz.mapper.StockMapper;
import com.txx.springboot.futuqz.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class StockTest {

    @Autowired
    private StockService stockService;


    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        LambdaQueryWrapper<Stock> lambdaQueryWrapper =
                new LambdaQueryWrapper<Stock>().eq (Stock::getPlate,"创业板")
                .eq (Stock::getIndustry,"M 科研服务");
        List<Stock> stocks = stockService.list (lambdaQueryWrapper);
        stocks.forEach(System.out::println);
        System.out.println (""+stocks.size ());
    }



}
