<template><view class="page">
<view class="hero"><view class="title">附近充电站</view><button size="mini" @click="scan">扫码</button></view>
<view class="card" v-for="s in stations" :key="s.stationId" @click="open(s)">
  <view class="name">{{s.stationName}}</view><view>{{s.stationCode}}</view>
  <view class="available">可用 {{s.availableConnectors}} / {{s.connectorCount}}</view>
</view></view></template>
<script setup>
import {ref,onMounted} from 'vue';import {request} from '../../services/http'
const stations=ref([])
async function load(){const tenant=uni.getStorageSync('tenantId')||1;stations.value=await request({url:`/app-api/v1/stations?tenantId=${tenant}`,auth:false})}
function open(s){uni.navigateTo({url:`/pages/station/detail?id=${s.stationId}`})}
function scan(){uni.scanCode({success:r=>uni.navigateTo({url:`/pages/charging/index?connectorCode=${encodeURIComponent(r.result)}`})})}
onMounted(load)
</script>
<style>.page{padding:24rpx}.hero{display:flex;justify-content:space-between;align-items:center}.title{font-size:42rpx;font-weight:700}.card{background:#fff;padding:28rpx;margin-top:20rpx;border-radius:20rpx}.name{font-size:32rpx;font-weight:600}.available{color:#16a34a;margin-top:12rpx}</style>
