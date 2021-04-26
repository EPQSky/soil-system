/**************************************************************************************************
  Filename:       GenericApp.c
*********************************************************************/
#include "OSAL.h"
#include "AF.h"
#include "ZDApp.h"
#include "ZDObject.h"
#include "ZDProfile.h"

#include "GenericApp.h"
#include "DebugTrace.h"

#if !defined( WIN32 )
  #include "OnBoard.h"
#endif

/* HAL */
#include "hal_lcd.h"
#include "hal_led.h"
#include "hal_key.h"
#include "hal_uart.h"

#include "MT_UART.h"

#include <string.h>
#include <stdio.h>
#include <math.h>

// 设备集群ID号
const cId_t GenericApp_ClusterList[GENERICAPP_MAX_CLUSTERS] =
{
  GENERICAPP_CLUSTERID
};

const SimpleDescriptionFormat_t GenericApp_SimpleDesc =
{
  GENERICAPP_ENDPOINT,              //  int Endpoint;
  GENERICAPP_PROFID,                //  uint16 AppProfId[2];
  GENERICAPP_DEVICEID,              //  uint16 AppDeviceId[2];
  GENERICAPP_DEVICE_VERSION,        //  int   AppDevVer:4;
  GENERICAPP_FLAGS,                 //  int   AppFlags:4;
  GENERICAPP_MAX_CLUSTERS,          //  byte  AppNumInClusters;
  (cId_t *)GenericApp_ClusterList,  //  byte *pAppInClusterList;
  GENERICAPP_MAX_CLUSTERS,          //  byte  AppNumInClusters;
  (cId_t *)GenericApp_ClusterList   //  byte *pAppInClusterList;
};

endPointDesc_t GenericApp_epDesc; // 端点

byte GenericApp_TaskID;   

devStates_t GenericApp_NwkState;

byte GenericApp_TransID;  // 唯一计数器ID号

afAddrType_t GenericApp_DstAddr;

const int ENDID = 2;
/*
 * 保存串口回调数据 
 */
extern unsigned char uartbuf[7];
extern int flag;

int index = 0;
int task = 0;
bool jump = true;
// 湿度、温度、盐度问询帧
unsigned char Read[3][8] = {{0x01, 0x03, 0x00, 0x12, 0x00, 0x01, 0x24, 0x0F},
                            {0x01, 0x03, 0x00, 0x13, 0x00, 0x01, 0x75, 0xCF},
                            {0x01, 0x03, 0x00, 0x15, 0x00, 0x01, 0x95, 0xCE}};

int humidity;
int temp;
int ec;

void GenericApp_SendTheMessage( void );

int binHexOct(char data[]); // 进制转换

/*********************************************************************
 * @fn      GenericApp_Init
 *
 * @brief   设备初始化设置
 *
 * @param   task_id - 任务ID号
 *
 * @return  none
 */
void GenericApp_Init( byte task_id )
{
  GenericApp_TaskID = task_id;
  // 初始化网络状态为未连接
  GenericApp_NwkState = DEV_INIT;
  GenericApp_TransID = 0;
  
  // 串口初始化
  MT_UartInit();
  MT_UartRegisterTaskID(task_id);

  GenericApp_DstAddr.addrMode = (afAddrMode_t)AddrBroadcast; // 广播
  GenericApp_DstAddr.endPoint = GENERICAPP_ENDPOINT;
  GenericApp_DstAddr.addr.shortAddr = 0xFFFC; // 发送给协调器

  // 设备描述
  GenericApp_epDesc.endPoint = GENERICAPP_ENDPOINT; // 端点
  GenericApp_epDesc.task_id = &GenericApp_TaskID; // 任务ID
  GenericApp_epDesc.simpleDesc
            = (SimpleDescriptionFormat_t *)&GenericApp_SimpleDesc;
  GenericApp_epDesc.latencyReq = noLatencyReqs;

  // AF初始化注册
  afRegister( &GenericApp_epDesc );

  // 注册所有Key事件
  RegisterForKeys( GenericApp_TaskID );

  // 更新显示
#if defined ( LCD_SUPPORTED )
    HalLcdWriteString( "GenericApp", HAL_LCD_LINE_1 );
#endif
  
  // 订阅设备消息
  ZDO_RegisterForZDOMsg( GenericApp_TaskID, End_Device_Bind_rsp );
  ZDO_RegisterForZDOMsg( GenericApp_TaskID, Match_Desc_rsp );
}

/*********************************************************************
 * @fn      GenericApp_ProcessEvent
 *
 * @brief   任务事件处理
 *
 * @param   task_id 任务ID号
 * @param   events - 事件
 *
 * @return  none
 */
UINT16 GenericApp_ProcessEvent( byte task_id, UINT16 events )
{
  afIncomingMSGPacket_t *MSGpkt;

  (void)task_id;  // 任务ID参数

  if ( events & SYS_EVENT_MSG ) // 判断该事件是否来自系统事件
  {
    // 获取该任务事件消息指针
    MSGpkt = (afIncomingMSGPacket_t *)osal_msg_receive( GenericApp_TaskID );
    while ( MSGpkt )
    {
      // 根据事件类型做出相对应的动作
      switch ( MSGpkt->hdr.event )
      {
        // 设备状态发生变化
        case ZDO_STATE_CHANGE:
          GenericApp_NwkState = (devStates_t)(MSGpkt->hdr.status);
          if ( (GenericApp_NwkState == DEV_ZB_COORD)
              || (GenericApp_NwkState == DEV_ROUTER)
              || (GenericApp_NwkState == DEV_END_DEVICE) )
          {
            // 开始定时发送消息
            osal_start_timerEx( GenericApp_TaskID,
                                GENERICAPP_SEND_MSG_EVT,
                              GENERICAPP_SEND_MSG_TIMEOUT );
          }
          break;

        default:
          break;
      }

      // 释放内存，防止栈溢出
      osal_msg_deallocate( (uint8 *)MSGpkt );

      // 执行下一个任务
      MSGpkt = (afIncomingMSGPacket_t *)osal_msg_receive( GenericApp_TaskID );
    }

    // 返回未处理的事件 /^ 异或 
    return (events ^ SYS_EVENT_MSG);
  }

  // 发送一个消息定时器事件
  //  (设置 GenericApp_Init()).
  if ( events & GENERICAPP_SEND_MSG_EVT )
  {
    // 发送土壤采集信息数据包
    GenericApp_SendTheMessage();

    // 定时发送数据包
    osal_start_timerEx( GenericApp_TaskID,
                        GENERICAPP_SEND_MSG_EVT,
                      GENERICAPP_SEND_MSG_TIMEOUT );
    
    // 返回未处理的事件 /^ 异或
    return (events ^ GENERICAPP_SEND_MSG_EVT);
  }

  return 0;
}

/*********************************************************************
 * @fn      GenericApp_SendTheMessage
 *
 * @brief   发送数据包
 *
 * @param   none
 *
 * @return  none
 */
void GenericApp_SendTheMessage( void )
{ 
  // 向传感器发送Modbus格式的数据
  HalUARTWrite(0, Read[index], 8);  
  
  if(flag != 1){
    if(!jump){
      char message[32];
      task++;
      switch(task) {
        case 1:
          humidity = binHexOct(uartbuf);
          break;
        case 2:
          temp = binHexOct(uartbuf);
          break;
        case 3:
          ec = binHexOct(uartbuf);
          // 获取16位短地址
          unsigned int addr16 = NLME_GetShortAddr();
          // 字符串格式化
          sprintf(message, "%d:%d,%.1f,%.1f,%d" , ENDID, addr16, humidity/10.0, temp/10.0, ec);
          task = 0;
          break;
      }  
      if (task == 0 && AF_DataRequest( &GenericApp_DstAddr, &GenericApp_epDesc,
                           GENERICAPP_CLUSTERID,
                           (byte)osal_strlen(message) + 1,
                           (byte *)&message,
                           &GenericApp_TransID,
                           AF_DISCV_ROUTE, AF_DEFAULT_RADIUS ) == afStatus_SUCCESS )
      {
        // 提示成功
      }
      else
      {
        // 提示失败
      }
    }
    // 对传感器轮询通讯
    flag = 1;
    index++;
    if(index >= 3){
      index = 0;
    }
    jump = false;
  }
}

/*********************************************************************
 *
 * 二进制转十进制
 *
 */
int binHexOct(char data[]){
  int result[16];

  // 获取 Char 中的每一位二进制数
  for (int i = 0; i < 8; ++i) {
    if (data[3] << i & 0x80) {
      result[i] = 1;
    } else {
      result[i] = 0;
    }
  }

  for (int i = 0; i < 8; ++i) {
    if (data[4] << i & 0x80) {
      result[i + 8] = 1;
    } else {
      result[i + 8] = 0;
    }
  }


  double num = 0;

  // 将二进制转换为十进制
  for (int i = 0; i < 16; ++i) {
    if (result[i] == 1) {
      // 2的n次方
      num += pow(2, 15 - i);
    }
  }
    
  return (int)num;
}

/*********************************************************************
*********************************************************************/
