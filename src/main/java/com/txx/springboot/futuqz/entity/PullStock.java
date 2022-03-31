package com.txx.springboot.futuqz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class PullStock {

    @TableId
    private Integer id;
    private Date pullDateTime;
    private String stockCode;
    private String name;
    private String category;


}
