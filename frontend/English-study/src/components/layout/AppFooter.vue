<template>
  <div class="app-footer">
    <p>© {{ currentYear }} 英语学习管理系统 - 版权所有</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const currentYear = ref(new Date().getFullYear())
</script>

<style scoped>
.app-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>
