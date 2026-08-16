<template><view class="page" v-if="detail">
<view class="card"><view class="title">{{detail.station.stationName}}</view><view>{{detail.station.availableConnectors}} 个可用充电枪</view></view>
<view class="card" v-for="c in detail.connectors" :key="c.connectorId">
 <view>{{c.chargerCode}} / 枪 {{c.connectorNo}}</view><view>{{c.connectorCode}}</view>
 <view>{{c.ratedPowerW?`${(c.ratedPowerW/1000).toFixed(0)} kW`:''}}</view>
 <button type="primary" :disabled="c.onlineStatus!==1||c.runningStatus!==0" @click="charge(c)">开始充电</button>
</view></view></template>
<script setup>
import {ref} from 'vue';import {onLoad} from '@dcloudio/uni-app';import {request} from '../../services/http'
const detail=ref(null)
onLoad(async q=>{const tenant=uni.getStorageSync('tenantId')||1;detail.value=await request({url:`/app-api/v1/stations/${q.id}?tenantId=${tenant}`,auth:false})})
function charge(c){if(!uni.getStorageSync('accessToken')){uni.navigateTo({url:'/pages/login/index'});return}
uni.navigateTo({url:`/pages/charging/index?connectorCode=${encodeURIComponent(c.connectorCode)}`})}
</script>
<style>.page{padding:24rpx}.card{background:#fff;padding:28rpx;border-radius:20rpx;margin-bottom:20rpx}.title{font-size:38rpx;font-weight:700}</style>
