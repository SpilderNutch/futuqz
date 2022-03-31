package com.txx.springboot.futuqz.bussiness;


import com.futu.openapi.*;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotSub;
import com.futu.openapi.pb.QotUpdateBasicQot;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/***
 * 实时成交价格显示
 *
 * {
 *   "retType": 0,
 *   "s2c": {
 *     "basicQotList": [{
 *        //股票信息
 *       "security": {
 *         "market": 1,
 *         "code": "00700"
 *       },
 *       "isSuspended": false,
 *       "listTime": "2004-06-16",
 *       "priceSpread": 0.2,
 *       //当前时间
 *       "updateTime": "2022-03-14 15:19:19",
 *       "highPrice": 359.6,
 *       "openPrice": 353.0,
 *       "lowPrice": 340.2,
 *       //当前价格
 *       "curPrice": 341.8,
 *       "lastClosePrice": 367.8,
 *       "volume": "45990530",
 *       "turnover": 1.6171494847E10,
 *       "turnoverRate": 0.479,
 *       "amplitude": 5.275,
 *       "darkStatus": 0,
 *       "listTimestamp": 1.0873152E9,
 *       "updateTimestamp": 1.647242359E9,
 *       "secStatus": 1
 *     }]
 *   }
 * }
 *
 *
 */
public class QotDemo implements FTSPI_Qot, FTSPI_Conn {
    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

    public static String CODE = "300142";
    public static int NUM = 100;
    public static double COST = 55.76;


    public QotDemo() {
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

        QotCommon.Security sec = QotCommon.Security.newBuilder()
                .setMarket(QotCommon.QotMarket.QotMarket_CNSZ_Security_VALUE)
                .setCode(CODE)
                .build();


        QotSub.C2S c2s = QotSub.C2S.newBuilder()
                //.addSecurityList(sec)
                //多个股票信息
                .addAllSecurityList (Lists.newArrayList (sec))
                .addSubTypeList(QotCommon.SubType.SubType_Basic_VALUE)
                .setIsSubOrUnSub(true)
                .setIsRegOrUnRegPush (true)
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
    public void onPush_UpdateBasicQuote(FTAPI_Conn client, QotUpdateBasicQot.Response rsp) {

        if (rsp.getRetType() != 0) {
            System.out.printf("QotUpdateBasicQuote failed: %s\n", rsp.getRetMsg());
        }
        else {
            try {
                String json = JsonFormat.printer().print(rsp);
                QotCommon.BasicQot basicQot = rsp.getS2C ().getBasicQotList (0);
                double curPrise = basicQot.getCurPrice ();
                //System.out.printf("Receive QotUpdateBasicQuote: %s\n", json);
                double price = (curPrise-COST)*NUM;
                System.out.print (DateTime.now ().toString ("yyyy-MM-dd HH:mm:ss")+"  "+curPrise +"，成本价："+COST);
                BigDecimal money = new BigDecimal(price);
                price   =  money.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                System.out.print (" >  " +price);

                double lastClosePrice = basicQot.getLastClosePrice ();
                double rate = (curPrise-lastClosePrice)/lastClosePrice*100;
                BigDecimal rateDecimal = new BigDecimal(rate);
                rate = rateDecimal.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                System.out.println ("  涨跌幅："+rate +"%");
            }catch (Exception e){
                e.printStackTrace ();
            }
        }
    }




    public static void main(String[] args) {
        FTAPI.init();
        QotDemo qot = new QotDemo();
        qot.start();

        while (true) {
            try {
                Thread.sleep(1000 * 60000);
            } catch (InterruptedException exc) {

            }
        }
    }
}

