<template><view><view class="card" v-for="x in rows" :key="x.warehouseCode+x.partCode">
  <view><b>{{x.partCode}} {{x.partName}}</b></view>
  <view>{{x.warehouseCode}}: {{x.availableQty}} {{x.unit}} / min {{x.minStockQty}}</view>
</view></view></template>
<script setup>
import {ref,onMounted} from 'vue';import {request} from '../../services/http'
const rows=ref([])
onMounted(async()=>rows.value=await request({url:'/technician-api/v1/operation/spare-stock'}))
</script>
