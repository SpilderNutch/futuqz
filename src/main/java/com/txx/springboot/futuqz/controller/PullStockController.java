package com.txx.springboot.futuqz.controller;

import com.txx.springboot.futuqz.common.core.AjaxResult;
import com.txx.springboot.futuqz.entity.PullStock;
import com.txx.springboot.futuqz.service.PullStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/***
 *   股票拉升代码
 *
 */
@RestController
@RequestMapping("/pullStock")
public class PullStockController {


    @Autowired
    private PullStockService pullStockService;

    /**
     * 显示所有的list数据
     * @return
     */
    @RequestMapping(value = "/list")
    public AjaxResult list(){
        List<PullStock> pullStockList = pullStockService.list ();
        return AjaxResult.success (pullStockList);
    }





}
