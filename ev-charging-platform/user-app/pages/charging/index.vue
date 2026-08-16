<template><view class="page"><view class="card">
<view class="title">{{sessionNo?'充电进行中':'准备充电'}}</view><view>枪口：{{connectorCode}}</view>
<view v-if="session"><view>状态：{{displayStatus}}</view><view>电量：{{((session.energyWh||0)/1000).toFixed(2)}} kWh</view>
<view v-if="session.powerW">功率：{{(session.powerW/1000).toFixed(1)}} kW</view><view v-if="session.soc!=null">SOC：{{session.soc}}%</view></view>
<view class="realtime">{{realtimeMode}}</view>
<button v-if="!sessionNo" type="primary" @click="start">开始充电</button>
<button v-else-if="!finished" type="warn" @click="stop">停止充电</button>
</view></view></template>
<script setup>
import {computed,ref,onUnmounted} from 'vue';import {onLoad} from '@dcloudio/uni-app';import {request,websocketUrl} from '../../services/http'
const connectorCode=ref(''),sessionNo=ref(''),session=ref(null),realtimeMode=ref('');let timer=null,socket=null
const finished=computed(()=>['FINISHED','CHARGE_FINISHED'].includes(String(session.value?.status||'')))
const displayStatus=computed(()=>session.value?.status||'-')
onLoad(q=>{connectorCode.value=decodeURIComponent(q.connectorCode||'')})
async function start(){
 const r=await request({url:'/app-api/v1/charging/sessions',method:'POST',data:{requestId:`app-${Date.now()}`,connectorCode:connectorCode.value,vehicleId:null}})
 sessionNo.value=r.sessionNo;session.value=r;await connectRealtime();startFallback()
}
async function connectRealtime(){
 try{
  const ticket=await request({url:`/app-api/v1/charging/sessions/${sessionNo.value}/realtime-ticket`,method:'POST'})
  socket=uni.connectSocket({url:websocketUrl(`/ws/charging?ticket=${encodeURIComponent(ticket.ticket)}`)})
  socket.onOpen(()=>{realtimeMode.value='实时连接';if(timer){clearInterval(timer);timer=null}})
  socket.onMessage(e=>{try{const live=JSON.parse(e.data);session.value={...(session.value||{}),...live};if(live.status==='FINISHED')cleanup()}catch{}})
  socket.onError(()=>{realtimeMode.value='实时连接失败，已切换轮询';startFallback()})
  socket.onClose(()=>{if(!finished.value){realtimeMode.value='连接已断开，轮询中';startFallback()}})
 }catch{realtimeMode.value='轮询中';startFallback()}
}
function startFallback(){if(timer||!sessionNo.value||finished.value)return;realtimeMode.value='轮询中';timer=setInterval(refresh,2000)}
async function refresh(){if(!sessionNo.value||finished.value)return;try{session.value=await request({url:`/app-api/v1/charging/sessions/${sessionNo.value}`})}catch{}}
async function stop(){
 const r=await request({url:`/app-api/v1/charging/sessions/${sessionNo.value}/stop`,method:'POST',data:{requestId:`stop-${Date.now()}`}})
 session.value={...(session.value||{}),...r};uni.showToast({title:'停止请求已提交'})
}
function cleanup(){if(timer)clearInterval(timer);timer=null;try{socket?.close({})}catch{}}
onUnmounted(cleanup)
</script>
<style>.page{padding:24rpx}.card{background:#fff;padding:32rpx;border-radius:20rpx}.title{font-size:38rpx;font-weight:700;margin-bottom:24rpx}.realtime{color:#64748b;margin:20rpx 0}</style>
