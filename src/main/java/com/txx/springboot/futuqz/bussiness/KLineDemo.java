package com.txx.springboot.futuqz.bussiness;

import com.futu.openapi.*;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotSub;
import com.futu.openapi.pb.QotUpdateKL;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.joda.time.DateTime;

import java.util.List;

/**
 * 实时K线代码
 *
 * {
 *   "retType": 0,
 *   "s2c": {
 *     "rehabType": 1,
 *     "klType": 6,
 *     "security": {
 *       "market": 22,
 *       "code": "300439"
 *     },
 *     "klList": [{
 *       "time": "2022-03-30 15:00:00",
 *       "isBlank": false,
 *       "highPrice": 16.67,
 *       "openPrice": 16.67,
 *       "lowPrice": 16.64,
 *       "closePrice": 16.66,
 *       "lastClosePrice": 0.0,
 *       "volume": "571000",
 *       "turnover": 9510590.0,
 *       "turnoverRate": 0.0,
 *       "pe": 0.0,
 *       "timestamp": 1.6486236E9
 *     }]
 *   }
 * }
 *
 */
public class KLineDemo implements FTSPI_Qot, FTSPI_Conn {
    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

    public static List<String> STOCK_CODES = Lists.newArrayList ("300439","","");


    public KLineDemo() {
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
                .setCode("300439")
                .build();

        QotCommon.Security sec2 = QotCommon.Security.newBuilder()
                .setMarket(QotCommon.QotMarket.QotMarket_CNSZ_Security_VALUE)
                .setCode("000818")
                .build();

        QotSub.C2S c2s = QotSub.C2S.newBuilder()
                .addAllSecurityList (Lists.newArrayList (sec,sec2))
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
        if (rsp.getRetType() != 0) {
            System.out.printf("QotUpdateKL failed: %s\n", rsp.getRetMsg());
        }
        else {
            try {
                String json = JsonFormat.printer().print(rsp);
                System.out.println (DateTime.now ().toString ("yyyy-MM-dd HH:mm:ss"));
                System.out.printf("Receive QotUpdateKL: %s\n", json);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        FTAPI.init();
        KLineDemo qot = new KLineDemo();
        qot.start();

        while (true) {
            try {
                Thread.sleep(1000 * 600);
            } catch (InterruptedException exc) {

            }
        }
    }
}

