<template>
  <view>
    <view class="card" v-for="x in rows" :key="x.taskNo">
      <view><b>{{x.taskNo}}</b> · Station {{x.stationId}}</view>
      <view>{{x.scheduledDate}} · {{x.status}} <text v-if="x.overdue">· OVERDUE</text></view>
      <button v-if="x.status==='PENDING'" @click="start(x)">Start</button>
      <button v-if="x.status==='IN_PROGRESS'" type="primary" @click="complete(x)">Complete</button>
    </view>
  </view>
</template>
<script setup>
import {ref,onMounted} from 'vue';import {request} from '../../services/http'
const rows=ref([])
async function load(){rows.value=await request({url:'/technician-api/v1/operation/inspections'})}
async function start(x){await request({url:`/technician-api/v1/operation/inspections/${x.taskNo}/start`,method:'POST'});await load()}
async function complete(x){await request({url:`/technician-api/v1/operation/inspections/${x.taskNo}/complete`,method:'POST',data:{result:'PASS',note:'Checklist completed'}});await load()}
onMounted(load)
</script>
