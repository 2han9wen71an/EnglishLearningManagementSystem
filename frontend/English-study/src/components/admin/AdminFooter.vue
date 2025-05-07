<template>
  <div class="admin-footer">
    <p>© {{ currentYear }} 英语知识应用网站系统 - 管理后台</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const currentYear = ref(new Date().getFullYear())
</script>

<style scoped>
.admin-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.admin-footer p {
  margin: 0;
}
</style>
