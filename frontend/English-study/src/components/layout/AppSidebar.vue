<template>
  <div class="app-sidebar">
    <el-menu
      :default-active="activeIndex"
      class="el-menu-vertical"
      :router="true"
      :collapse="isCollapse"
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页</template>
      </el-menu-item>

      <el-menu-item index="/words">
        <el-icon><Reading /></el-icon>
        <template #title>单词学习</template>
      </el-menu-item>

      <el-menu-item index="/listening">
        <el-icon><Headset /></el-icon>
        <template #title>听力练习</template>
      </el-menu-item>

      <el-menu-item index="/reading">
        <el-icon><Document /></el-icon>
        <template #title>阅读</template>
      </el-menu-item>

      <el-menu-item index="/dialogue">
        <el-icon><ChatDotRound /></el-icon>
        <template #title>情景对话</template>
      </el-menu-item>

      <el-menu-item index="/essay">
        <el-icon><Edit /></el-icon>
        <template #title>作文批改</template>
      </el-menu-item>

      <el-menu-item index="/pronunciation">
        <el-icon><Microphone /></el-icon>
        <template #title>发音评测</template>
      </el-menu-item>

      <el-menu-item index="/exam">
        <el-icon><DocumentChecked /></el-icon>
        <template #title>考试</template>
      </el-menu-item>

      <el-menu-item index="/wordcards">
        <el-icon><Collection /></el-icon>
        <template #title>单词卡片</template>
      </el-menu-item>

      <el-menu-item index="/profile">
        <el-icon><User /></el-icon>
        <template #title>个人中心</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled,
  Reading,
  Headset,
  Document,
  ChatDotRound,
  Edit,
  Microphone,
  DocumentChecked,
  Collection,
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapse = ref(false)
const activeIndex = computed(() => route.path)
</script>

<style scoped>
.app-sidebar {
  height: 100%;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 220px;
}
</style>
