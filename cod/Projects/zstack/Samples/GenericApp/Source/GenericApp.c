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

byte GenericApp_TaskID;   // 任务ID号

devStates_t GenericApp_NwkState;

byte GenericApp_TransID;  // 唯一计数器ID号

afAddrType_t GenericApp_DstAddr;

void GenericApp_MessageMSGCB( afIncomingMSGPacket_t *pckt );

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

/*********************************************************************
 * @fn      GenericApp_Init
 *
 * @brief   设备初始化设置
 *
 * @param   task_id - the ID assigned by OSAL.  This ID should be
 *                    used to send messages and set timers.
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

  // 设备描述
  GenericApp_epDesc.endPoint = GENERICAPP_ENDPOINT;
  GenericApp_epDesc.task_id = &GenericApp_TaskID;
  GenericApp_epDesc.simpleDesc
            = (SimpleDescriptionFormat_t *)&GenericApp_SimpleDesc;
  GenericApp_epDesc.latencyReq = noLatencyReqs;

  // AF初始化注册
  afRegister( &GenericApp_epDesc );

  // 注册所有Key事件
  RegisterForKeys( GenericApp_TaskID );
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
        // 接收到节点消息的AF
        case AF_INCOMING_MSG_CMD:
          GenericApp_MessageMSGCB( MSGpkt );
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
  
  return 0;
}

/*********************************************************************
 * @fn      GenericApp_MessageMSGCB
 *
 * @brief   数据回调消息处理
 *
 * @param   none
 *
 * @return  none
 */
void GenericApp_MessageMSGCB( afIncomingMSGPacket_t *pkt )
{ 
  char msg[128];
  char rssiBUF[4];
  // 获取RSSI信号强度
  pkt->rssi = 0xFF - pkt->rssi;
  rssiBUF[0] = '-';
  rssiBUF[1] = pkt->rssi/10 + 0x30;
  rssiBUF[2] = pkt->rssi%10 + 0x30;
  rssiBUF[3] = '\0'; 
  // 获取终端节点数据包，并且以','分割
  const char *addr16;
  addr16 = strtok(pkt->cmd.Data, ",");
  char *humidity;
  humidity = strtok(NULL, ",");
  char *temp;
  temp = strtok(NULL, ",");
  char *ec;
  ec = strtok(NULL, ",");
  // 字符串格式化为JSON数据包
  sprintf(msg,"{\"addr16\":\"%s\", \"rssi\":\"%s\", \"humidity\":%s, \"temp\":%s, \"ec\":%s}", addr16, rssiBUF, humidity, temp, ec); 
  
  switch ( pkt->clusterId )
  {
    case GENERICAPP_CLUSTERID:
      // 向ESP8266串口写入数据
      HalUARTWrite(0, (byte *)&msg, (byte)osal_strlen(msg) + 1);
      break;
  }
}


/*********************************************************************
*********************************************************************/
