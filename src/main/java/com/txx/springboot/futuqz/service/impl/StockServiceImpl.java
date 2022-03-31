package com.txx.springboot.futuqz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.txx.springboot.futuqz.entity.Stock;
import com.txx.springboot.futuqz.mapper.StockMapper;
import com.txx.springboot.futuqz.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("stockService")
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {


    @Autowired
    private StockMapper stockMapper;



}
