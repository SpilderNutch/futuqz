package com.txx.springboot.futuqz.bussiness;


import com.futu.openapi.*;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotSub;
import com.futu.openapi.pb.QotUpdateBasicQot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
public class QotDetectStock implements FTSPI_Qot, FTSPI_Conn {

    public static Map STOCK_CODE_NAME_MAP = Maps.newHashMap ();

    static {
        STOCK_CODE_NAME_MAP.put ("300482","万孚生物");
        STOCK_CODE_NAME_MAP.put ("300676","华大基因");
        STOCK_CODE_NAME_MAP.put ("300003","乐普医疗");
        //STOCK_CODE_NAME_MAP.put ("603392","万泰生物");
        STOCK_CODE_NAME_MAP.put ("002932","明德生物");
        STOCK_CODE_NAME_MAP.put ("300942","易瑞生物");
    }

    public static List<String> STOCKS = Lists.newArrayList (STOCK_CODE_NAME_MAP.keySet ());


    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();


    public QotDetectStock() {
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

        List<QotCommon.Security> securities = Lists.newArrayList ();
        STOCKS.forEach(code->{
            securities.add (QotCommon.Security.newBuilder()
                    .setMarket(QotCommon.QotMarket.QotMarket_CNSZ_Security_VALUE)
                    .setCode(code)
                    .build());
        });

        QotSub.C2S c2s = QotSub.C2S.newBuilder()
                //.addSecurityList(sec)
                //多个股票信息
                .addAllSecurityList (securities)
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
                //String json = JsonFormat.printer().print(rsp);
                List<QotCommon.BasicQot> basicQots = rsp.getS2C ().getBasicQotListList ();
                basicQots.forEach (basicQot -> {
                    double curPrise = basicQot.getCurPrice ();
                    double lastClosePrice = basicQot.getLastClosePrice ();
                    String code = basicQot.getSecurity ().getCode ();

                    double rate = (curPrise-lastClosePrice)/lastClosePrice*100;
                    BigDecimal money = new BigDecimal(rate);
                    rate = money.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();

                    System.out.println (STOCK_CODE_NAME_MAP.get (code)+"    现价："+curPrise+",昨收价："+lastClosePrice+",涨跌幅："+rate +"%");
                });
            }catch (Exception e){
                e.printStackTrace ();
            }
        }
    }




    public static void main(String[] args) {
        FTAPI.init();
        QotDetectStock qot = new QotDetectStock ();
        qot.start();

        while (true) {
            try {
                Thread.sleep(1000 * 6000);
            } catch (InterruptedException exc) {

            }
        }
    }
}

