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

byte GenericApp_TaskID;   

devStates_t GenericApp_NwkState;

byte GenericApp_TransID;  // Ψһ������ID��

afAddrType_t GenericApp_DstAddr;

const int ENDID = 2;
/*
 * ���洮�ڻص����� 
 */
extern unsigned char uartbuf[7];
extern int flag;

int index = 0;
int task = 0;
bool jump = true;
// ʪ�ȡ��¶ȡ��ζ���ѯ֡
unsigned char Read[3][8] = {{0x01, 0x03, 0x00, 0x12, 0x00, 0x01, 0x24, 0x0F},
                            {0x01, 0x03, 0x00, 0x13, 0x00, 0x01, 0x75, 0xCF},
                            {0x01, 0x03, 0x00, 0x15, 0x00, 0x01, 0x95, 0xCE}};

int humidity;
int temp;
int ec;

void GenericApp_SendTheMessage( void );

int binHexOct(char data[]); // ����ת��

/*********************************************************************
 * @fn      GenericApp_Init
 *
 * @brief   �豸��ʼ������
 *
 * @param   task_id - ����ID��
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

  GenericApp_DstAddr.addrMode = (afAddrMode_t)AddrBroadcast; // �㲥
  GenericApp_DstAddr.endPoint = GENERICAPP_ENDPOINT;
  GenericApp_DstAddr.addr.shortAddr = 0xFFFC; // ���͸�Э����

  // �豸����
  GenericApp_epDesc.endPoint = GENERICAPP_ENDPOINT; // �˵�
  GenericApp_epDesc.task_id = &GenericApp_TaskID; // ����ID
  GenericApp_epDesc.simpleDesc
            = (SimpleDescriptionFormat_t *)&GenericApp_SimpleDesc;
  GenericApp_epDesc.latencyReq = noLatencyReqs;

  // AF��ʼ��ע��
  afRegister( &GenericApp_epDesc );

  // ע������Key�¼�
  RegisterForKeys( GenericApp_TaskID );

  // ������ʾ
#if defined ( LCD_SUPPORTED )
    HalLcdWriteString( "GenericApp", HAL_LCD_LINE_1 );
#endif
  
  // �����豸��Ϣ
  ZDO_RegisterForZDOMsg( GenericApp_TaskID, End_Device_Bind_rsp );
  ZDO_RegisterForZDOMsg( GenericApp_TaskID, Match_Desc_rsp );
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
        // �豸״̬�����仯
        case ZDO_STATE_CHANGE:
          GenericApp_NwkState = (devStates_t)(MSGpkt->hdr.status);
          if ( (GenericApp_NwkState == DEV_ZB_COORD)
              || (GenericApp_NwkState == DEV_ROUTER)
              || (GenericApp_NwkState == DEV_END_DEVICE) )
          {
            // ��ʼ��ʱ������Ϣ
            osal_start_timerEx( GenericApp_TaskID,
                                GENERICAPP_SEND_MSG_EVT,
                              GENERICAPP_SEND_MSG_TIMEOUT );
          }
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

  // ����һ����Ϣ��ʱ���¼�
  //  (���� GenericApp_Init()).
  if ( events & GENERICAPP_SEND_MSG_EVT )
  {
    // ���������ɼ���Ϣ���ݰ�
    GenericApp_SendTheMessage();

    // ��ʱ�������ݰ�
    osal_start_timerEx( GenericApp_TaskID,
                        GENERICAPP_SEND_MSG_EVT,
                      GENERICAPP_SEND_MSG_TIMEOUT );
    
    // ����δ������¼� /^ ���
    return (events ^ GENERICAPP_SEND_MSG_EVT);
  }

  return 0;
}

/*********************************************************************
 * @fn      GenericApp_SendTheMessage
 *
 * @brief   �������ݰ�
 *
 * @param   none
 *
 * @return  none
 */
void GenericApp_SendTheMessage( void )
{ 
  // �򴫸�������Modbus��ʽ������
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
          // ��ȡ16λ�̵�ַ
          unsigned int addr16 = NLME_GetShortAddr();
          // �ַ�����ʽ��
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
        // ��ʾ�ɹ�
      }
      else
      {
        // ��ʾʧ��
      }
    }
    // �Դ�������ѯͨѶ
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
 * ������תʮ����
 *
 */
int binHexOct(char data[]){
  int result[16];

  // ��ȡ Char �е�ÿһλ��������
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

  // ��������ת��Ϊʮ����
  for (int i = 0; i < 16; ++i) {
    if (result[i] == 1) {
      // 2��n�η�
      num += pow(2, 15 - i);
    }
  }
    
  return (int)num;
}

/*********************************************************************
*********************************************************************/
