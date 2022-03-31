package com.txx.springboot.futuqz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.txx.springboot.futuqz.entity.PullStock;
import com.txx.springboot.futuqz.mapper.PullStockMapper;
import com.txx.springboot.futuqz.service.PullStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pullStockService")
public class PullStockServiceImpl extends ServiceImpl<PullStockMapper, PullStock> implements PullStockService {


    @Autowired
    private PullStockMapper pullStockMapper;



}
