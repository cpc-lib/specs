<template><view class="page"><view class="card"><view class="title">EV Charging</view>
<input v-model="tenantId" type="number" placeholder="Tenant"/><input v-model="username" placeholder="Username"/>
<input v-model="password" password placeholder="Password"/><button type="primary" @click="login">登录</button></view></view></template>
<script setup>
import {ref} from 'vue';import {request} from '../../services/http'
const tenantId=ref(1),username=ref('driver'),password=ref('driver123456')
async function login(){try{const r=await request({url:'/auth-api/v1/login',method:'POST',auth:false,data:{tenantId:Number(tenantId.value),username:username.value,password:password.value}});
if(!r.principal.roles.includes('MEMBER'))throw new Error('Not member');uni.setStorageSync('accessToken',r.accessToken);uni.setStorageSync('refreshToken',r.refreshToken);uni.setStorageSync('sessionId',r.sessionId);uni.setStorageSync('tenantId',Number(tenantId.value));uni.switchTab({url:'/pages/index/index'})}
catch(e){uni.showToast({title:'登录失败',icon:'none'})}}
</script>
<style>.page{padding:48rpx}.card{background:#fff;padding:32rpx;border-radius:20rpx}.title{font-size:44rpx;font-weight:700;margin-bottom:32rpx}input{background:#f5f5f5;padding:20rpx;margin-bottom:20rpx}</style>
