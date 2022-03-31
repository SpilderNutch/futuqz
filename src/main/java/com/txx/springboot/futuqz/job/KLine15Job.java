package com.txx.springboot.futuqz.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.futu.openapi.*;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotSub;
import com.futu.openapi.pb.QotUpdateKL;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.txx.springboot.futuqz.entity.PullStock;
import com.txx.springboot.futuqz.entity.Stock;
import com.txx.springboot.futuqz.service.PullStockService;
import com.txx.springboot.futuqz.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/****
 * 这是一个15分钟K线策略，涨幅大于5就纳入策略中，监控所有创业板中股票。
 *
 */
@Component
public class KLine15Job implements CommandLineRunner, FTSPI_Qot, FTSPI_Conn {

    @Autowired
    private StockService stockService;

    @Autowired
    private PullStockService pullStockService;

    //保存所有条件下的所有股票信息数据
    public static List<Stock> STOCKS = Lists.newArrayList ();
    //对股票code与名称进行一一对应
    public static Map<String,String> STOCK_CODE_NAME = Maps.newHashMap ();


    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

    public KLine15Job(){
        qot.setClientInfo("javaclient", 1);  //设置客户端信息
        qot.setConnSpi(this);  //设置连接回调
        qot.setQotSpi(this);   //设置交易回调
    }

    public void start() {
        qot.initConnect("127.0.0.1", (short)11111, false);
    }


    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc)
    {
        System.out.printf("Qot onInitConnect: ret=%b desc=%s connID=%d\n", errCode, desc, client.getConnectID());
        if (errCode != 0)
            return;

        //将所有深市创业板数据纳入监控代码中
        List<QotCommon.Security> securities = Lists.newArrayList ();
        AtomicInteger i= new AtomicInteger ();
        STOCKS.forEach (stock -> {
            i.getAndIncrement ();
            if(i.get ()<100){
                securities.add (QotCommon.Security.newBuilder()
                        .setMarket(QotCommon.QotMarket.QotMarket_CNSZ_Security_VALUE)
                        .setCode(stock.getCode ())
                        .build());
            }
        });


        QotSub.C2S c2s = QotSub.C2S.newBuilder()
                .addAllSecurityList (securities)
                .addSubTypeList(QotCommon.SubType.SubType_KL_5Min_VALUE)
                .setIsSubOrUnSub(true)
                .setIsRegOrUnRegPush(true)
                .build();
        QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
        int seqNo = qot.sub(req);
        System.out.printf("Send QotSub: %d\n", seqNo);
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        System.out.printf("Qot onDisConnect: %d\n", errCode);
    }

    @Override
    public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp) {
        if (rsp.getRetType() != 0) {
            System.out.printf("QotSub failed: %s\n", rsp.getRetMsg());
        }
        else {
            try {
                String json = JsonFormat.printer().print(rsp);
                System.out.printf("Receive QotSub: %s\n", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPush_UpdateKL(FTAPI_Conn client, QotUpdateKL.Response rsp) {
        System.out.println ("KLine15Job onPush_UpdateKL");

        if (rsp.getRetType() != 0) {
            System.out.printf("QotUpdateKL failed: %s\n", rsp.getRetMsg());
        }
        else {
            try {
                QotCommon.KLine kLine = rsp.getS2C ().getKlList (0);
                double openPrice = kLine.getOpenPrice ();
                double closePrice = kLine.getClosePrice ();
                double rate = (closePrice-openPrice)/openPrice*100;
                BigDecimal money = new BigDecimal(rate);
                rate = money.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                if(rate>5){
                    QotCommon.Security security = rsp.getS2C ().getSecurity ();
                    String code = security.getCode ();

                    PullStock pullStock = new PullStock ();
                    pullStock.setPullDateTime (new Date ());
                    pullStock.setStockCode (code);
                    pullStock.setName (STOCK_CODE_NAME.get (code));
                    pullStockService.save (pullStock);
                }
            }catch (Exception e){
                e.printStackTrace ();
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {

        LambdaQueryWrapper<Stock> lambdaQueryWrapper =
                new LambdaQueryWrapper<Stock>().eq (Stock::getPlate,"创业板");
        STOCKS = stockService.list (lambdaQueryWrapper);

        STOCKS.forEach (item->{
            STOCK_CODE_NAME.put (item.getCode (),item.getName ());
        });

        //先睡个5秒后再执行
        Thread.sleep (10000);

        FTAPI.init();
        KLine15Job qot = new KLine15Job();
        qot.start();


        while (true) {
            try {
                Thread.sleep(1000 * 60000);
            } catch (InterruptedException exc) {

            }
        }


    }









}
