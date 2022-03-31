package com.txx.springboot.futuqz.bussiness;

import com.futu.openapi.*;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotGetCapitalFlow;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.math.BigDecimal;
import java.util.List;

/**
 * 获取资金的流向
 *
 *  每一分钟间断的资金流向数据
 *
 *{
 *   "retType": 0,
 *   "retMsg": "",
 *   "errCode": 0,
 *   "s2c": {
 *     "flowItemList": [{
 *       "inFlow": -2.24316E7,
 *       "time": "2022-03-14 09:30:00",
 *       "timestamp": 1.6472214E9
 *     }, {
 *       "inFlow": -1.768186E7,
 *       "time": "2022-03-14 09:31:00",
 *       "timestamp": 1.64722146E9
 *     },
 */
public class FundFlowDemo implements FTSPI_Qot, FTSPI_Conn {
    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

    public FundFlowDemo() {
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
        QotGetCapitalFlow.C2S c2s = QotGetCapitalFlow.C2S.newBuilder()
                .setSecurity(sec)
                .build();
        QotGetCapitalFlow.Request req = QotGetCapitalFlow.Request.newBuilder().setC2S(c2s).build();
        int seqNo = qot.getCapitalFlow(req);
        System.out.printf("Send QotGetCapitalFlow: %d\n", seqNo);
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        System.out.printf("Qot onDisConnect: %d\n", errCode);
    }

    @Override
    public void onReply_GetCapitalFlow(FTAPI_Conn client, int nSerialNo, QotGetCapitalFlow.Response rsp) {
        if (rsp.getRetType() != 0) {
            System.out.printf("QotGetCapitalFlow failed: %s\n", rsp.getRetMsg());
        }
        else {
            try {
                String json = JsonFormat.printer().print(rsp);
                System.out.printf("Receive QotGetCapitalFlow: %s\n", json);
                BigDecimal inFlow = BigDecimal.valueOf (rsp.getS2C ().getFlowItemList (rsp.getS2C ().getFlowItemListCount ()-1).getInFlow ());
                System.out.println ("InFlow        "+inFlow.toString ());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        FTAPI.init();
        FundFlowDemo qot = new FundFlowDemo();
        qot.start();

        while (true) {
            try {
                Thread.sleep(1000 * 600);
            } catch (InterruptedException exc) {

            }
        }
    }
}

