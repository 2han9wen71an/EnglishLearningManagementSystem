<template>
  <div class="admin-sidebar">
    <div class="logo-container">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
      <h1 class="logo-text">管理后台</h1>
    </div>

    <el-menu
      :default-active="activeIndex"
      class="el-menu-vertical"
      :router="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <el-menu-item index="/admin/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <template #title>控制面板</template>
      </el-menu-item>

      <el-menu-item index="/admin/users">
        <el-icon><User /></el-icon>
        <template #title>用户管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/words">
        <el-icon><Reading /></el-icon>
        <template #title>单词管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/listening">
        <el-icon><Headset /></el-icon>
        <template #title>听力管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/reading">
        <el-icon><Document /></el-icon>
        <template #title>阅读管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/exams">
        <el-icon><DocumentChecked /></el-icon>
        <template #title>考试管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/scores">
        <el-icon><Histogram /></el-icon>
        <template #title>成绩管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/notices">
        <el-icon><Bell /></el-icon>
        <template #title>公告管理</template>
      </el-menu-item>

      <el-menu-item index="/admin/statistics">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>数据统计</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled,
  User,
  Reading,
  Headset,
  Document,
  DocumentChecked,
  Bell,
  DataAnalysis,
  Histogram
} from '@element-plus/icons-vue'

const route = useRoute()
const activeIndex = computed(() => route.path)
</script>

<style scoped>
.admin-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  border-bottom: 1px solid #1f2d3d;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.el-menu-item {
  display: flex;
  align-items: center;
}

.el-icon {
  margin-right: 10px;
}
</style>
