<template><view class="page"><view class="card" v-for="o in rows" :key="o.orderNo">
<view class="name">{{o.orderNo}}</view><view>{{(o.energyWh/1000).toFixed(2)}} kWh · ¥{{(o.receivableAmountFen/100).toFixed(2)}}</view>
<view>已付 ¥{{(o.paidAmountFen/100).toFixed(2)}}</view><button v-if="o.paidAmountFen<o.receivableAmountFen" size="mini" type="primary" @click="pay(o)">支付</button>
</view></view></template>
<script setup>
import {ref,onMounted} from 'vue';import {request} from '../../services/http'
const rows=ref([])
async function load(){if(!uni.getStorageSync('accessToken'))return;rows.value=await request({url:'/app-api/v1/orders'})}
async function pay(o){await request({url:'/app-api/v1/payments',method:'POST',data:{requestId:`pay-${Date.now()}`,orderNo:o.orderNo,channel:'MOCK'}});uni.showToast({title:'支付单已创建'});setTimeout(load,800)}
onMounted(load)
</script>
<style>.page{padding:24rpx}.card{background:#fff;padding:24rpx;border-radius:16rpx;margin-bottom:16rpx}.name{font-weight:700}</style>
