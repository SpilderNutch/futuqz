package com.txx.springboot.futuqz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Stock {

    @TableId
    private String code;
    private String name;
    private String plate;
    private String company;
    private String companyEn;
    private String regAddress;
    private Date marketDate;
    private BigDecimal totalCapital;
    private BigDecimal circulCapital;
    private String area;
    private String province;
    private String city;
    private String industry;
    private String website;
    private String market;
    private String createdBy;
    private Date createdTime;
    private String updatedBy;
    private Date updatedTime;




}
