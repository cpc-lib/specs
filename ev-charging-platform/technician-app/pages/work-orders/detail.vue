<template>
  <view class="card">
    <view><b>{{no}}</b> · {{status}}</view>
    <textarea v-model="summary" placeholder="Repair summary"/>
    <button class="btn" v-if="status==='ASSIGNED'" @click="start">Start repair</button>
    <button class="btn" v-if="status==='IN_PROGRESS'" type="primary" @click="complete">Complete repair</button>
    <button class="btn" @click="photo">Upload repair photo</button>
  </view>
</template>
<script setup>
import {ref} from 'vue';import {onLoad} from '@dcloudio/uni-app';import {request,upload} from '../../services/http'
const no=ref(''),status=ref(''),summary=ref('Repaired and tested.')
onLoad(q=>{no.value=q.no;status.value=q.status})
async function start(){await request({url:`/technician-api/v1/operation/work-orders/${no.value}/start`,method:'POST'});status.value='IN_PROGRESS'}
async function complete(){await request({url:`/technician-api/v1/operation/work-orders/${no.value}/repair`,method:'POST',data:{summary:summary.value}});status.value='WAIT_VERIFY'}
function photo(){uni.chooseImage({count:1,success:async r=>{await upload(no.value,r.tempFilePaths[0]);uni.showToast({title:'Uploaded'})}})}
</script>
