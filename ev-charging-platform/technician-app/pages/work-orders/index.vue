<template>
  <view>
    <view class="card" v-for="x in rows" :key="x.workOrderNo" @click="open(x)">
      <view><b>{{x.workOrderNo}}</b> · {{x.priority}} · {{x.status}}</view>
      <view>{{x.title}}</view><view>SLA: {{x.slaStatus}}</view>
    </view>
  </view>
</template>
<script setup>
import {ref,onMounted} from 'vue';import {request} from '../../services/http'
const rows=ref([])
async function load(){rows.value=await request({url:'/technician-api/v1/operation/work-orders'})}
function open(x){uni.navigateTo({url:`/pages/work-orders/detail?no=${x.workOrderNo}&status=${x.status}`})}
onMounted(load)
</script>
