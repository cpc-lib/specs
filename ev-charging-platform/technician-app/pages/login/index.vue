<template><view class="card"><input v-model="tenantId" type="number" placeholder="Tenant"/>
<input v-model="username" placeholder="Username"/><input v-model="password" password placeholder="Password"/>
<input v-model="apiBase" placeholder="Gateway URL"/><button class="btn" type="primary" @click="login">Sign in</button></view></template>
<script setup>
import {ref} from 'vue';import {request} from '../../services/http'
const tenantId=ref(1),username=ref('technician'),password=ref('tech123456'),apiBase=ref(uni.getStorageSync('apiBase')||'http://127.0.0.1:8080')
async function login(){uni.setStorageSync('apiBase',apiBase.value);try{const r=await request({url:'/auth-api/v1/login',method:'POST',auth:false,data:{tenantId:Number(tenantId.value),username:username.value,password:password.value}});
if(!r.principal.roles.includes('TECHNICIAN'))throw new Error();uni.setStorageSync('accessToken',r.accessToken);uni.setStorageSync('refreshToken',r.refreshToken);uni.setStorageSync('sessionId',r.sessionId);uni.switchTab({url:'/pages/work-orders/index'})}catch{uni.showToast({title:'Login failed',icon:'none'})}}
</script>
