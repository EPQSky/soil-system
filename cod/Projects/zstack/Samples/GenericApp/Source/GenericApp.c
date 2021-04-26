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

// �豸��ȺID��
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

endPointDesc_t GenericApp_epDesc; // �˵�

byte GenericApp_TaskID;   // ����ID��

devStates_t GenericApp_NwkState;

byte GenericApp_TransID;  // Ψһ������ID��

afAddrType_t GenericApp_DstAddr;

void GenericApp_MessageMSGCB( afIncomingMSGPacket_t *pckt );

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

/*********************************************************************
 * @fn      GenericApp_Init
 *
 * @brief   �豸��ʼ������
 *
 * @param   task_id - the ID assigned by OSAL.  This ID should be
 *                    used to send messages and set timers.
 *
 * @return  none
 */
void GenericApp_Init( byte task_id )
{
  GenericApp_TaskID = task_id;
  // ��ʼ������״̬Ϊδ����
  GenericApp_NwkState = DEV_INIT;
  GenericApp_TransID = 0;
  
  // ���ڳ�ʼ��
  MT_UartInit();
  MT_UartRegisterTaskID(task_id);

  // �豸����
  GenericApp_epDesc.endPoint = GENERICAPP_ENDPOINT;
  GenericApp_epDesc.task_id = &GenericApp_TaskID;
  GenericApp_epDesc.simpleDesc
            = (SimpleDescriptionFormat_t *)&GenericApp_SimpleDesc;
  GenericApp_epDesc.latencyReq = noLatencyReqs;

  // AF��ʼ��ע��
  afRegister( &GenericApp_epDesc );

  // ע������Key�¼�
  RegisterForKeys( GenericApp_TaskID );
}

/*********************************************************************
 * @fn      GenericApp_ProcessEvent
 *
 * @brief   �����¼�����
 *
 * @param   task_id ����ID��
 * @param   events - �¼�
 *
 * @return  none
 */
UINT16 GenericApp_ProcessEvent( byte task_id, UINT16 events )
{
  afIncomingMSGPacket_t *MSGpkt;
  
  (void)task_id;  // ����ID����

  if ( events & SYS_EVENT_MSG ) // �жϸ��¼��Ƿ�����ϵͳ�¼�
  {
    // ��ȡ�������¼���Ϣָ��
    MSGpkt = (afIncomingMSGPacket_t *)osal_msg_receive( GenericApp_TaskID );
    while ( MSGpkt )
    {
      // �����¼������������Ӧ�Ķ���
      switch ( MSGpkt->hdr.event )
      {
        // ���յ��ڵ���Ϣ��AF
        case AF_INCOMING_MSG_CMD:
          GenericApp_MessageMSGCB( MSGpkt );
          break;
        default:
          break;
      }

      // �ͷ��ڴ棬��ֹջ���
      osal_msg_deallocate( (uint8 *)MSGpkt );

      // ִ����һ������
      MSGpkt = (afIncomingMSGPacket_t *)osal_msg_receive( GenericApp_TaskID );
    }

    // ����δ������¼� /^ ���
    return (events ^ SYS_EVENT_MSG);
  }
  
  return 0;
}

/*********************************************************************
 * @fn      GenericApp_MessageMSGCB
 *
 * @brief   ���ݻص���Ϣ����
 *
 * @param   none
 *
 * @return  none
 */
void GenericApp_MessageMSGCB( afIncomingMSGPacket_t *pkt )
{ 
  char msg[128];
  char rssiBUF[4];
  // ��ȡRSSI�ź�ǿ��
  pkt->rssi = 0xFF - pkt->rssi;
  rssiBUF[0] = '-';
  rssiBUF[1] = pkt->rssi/10 + 0x30;
  rssiBUF[2] = pkt->rssi%10 + 0x30;
  rssiBUF[3] = '\0'; 
  // ��ȡ�ն˽ڵ����ݰ���������','�ָ�
  const char *addr16;
  addr16 = strtok(pkt->cmd.Data, ",");
  char *humidity;
  humidity = strtok(NULL, ",");
  char *temp;
  temp = strtok(NULL, ",");
  char *ec;
  ec = strtok(NULL, ",");
  // �ַ�����ʽ��ΪJSON���ݰ�
  sprintf(msg,"{\"addr16\":\"%s\", \"rssi\":\"%s\", \"humidity\":%s, \"temp\":%s, \"ec\":%s}", addr16, rssiBUF, humidity, temp, ec); 
  
  switch ( pkt->clusterId )
  {
    case GENERICAPP_CLUSTERID:
      // ��ESP8266����д������
      HalUARTWrite(0, (byte *)&msg, (byte)osal_strlen(msg) + 1);
      break;
  }
}


/*********************************************************************
*********************************************************************/
