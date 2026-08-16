<template><view class="page">
<view class="card"><view>当前账号：{{logged?'已登录':'未登录'}}</view>
<button v-if="!logged" @click="login">登录</button><template v-else>
<input v-model="currentPassword" password placeholder="当前密码"/><input v-model="newPassword" password placeholder="新密码（至少10位）"/>
<button @click="changePassword">修改密码</button><button type="warn" @click="doLogout">退出登录</button></template></view></view></template>
<script setup>
import {ref} from 'vue';import {request,logout} from '../../services/http'
const logged=ref(!!uni.getStorageSync('accessToken')),currentPassword=ref(''),newPassword=ref('')
function login(){uni.navigateTo({url:'/pages/login/index'})}
async function doLogout(){await logout();logged.value=false;uni.showToast({title:'已退出'})}
async function changePassword(){try{await request({url:'/auth-api/v1/change-password',method:'POST',data:{currentPassword:currentPassword.value,newPassword:newPassword.value}});
 await logout();logged.value=false;uni.showToast({title:'密码已修改，请重新登录',icon:'none'})}catch{uni.showToast({title:'修改失败',icon:'none'})}}
</script>
<style>.page{padding:24rpx}.card{background:#fff;padding:28rpx;border-radius:18rpx}input{background:#f5f5f5;padding:18rpx;margin:16rpx 0}</style>
