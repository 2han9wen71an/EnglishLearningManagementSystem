<template>
  <div class="main-layout">
    <el-container class="full-height">
      <el-header height="60px">
        <app-header />
      </el-header>
      <el-container class="content-container">
        <el-aside width="220px">
          <app-sidebar />
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
      <el-footer height="60px">
        <app-footer />
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
</script>

<style scoped>
.main-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.full-height {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-container {
  flex: 1;
  overflow: hidden;
}

.el-header {
  background-color: #409EFF;
  color: #fff;
  line-height: 60px;
  padding: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.el-aside {
  background-color: #f5f7fa;
  color: #333;
  border-right: 1px solid #e6e6e6;
  overflow-y: auto;
  height: 100%;
}

.el-main {
  background-color: #fff;
  color: #333;
  padding: 20px;
  overflow-y: auto;
  height: 100%;
  box-sizing: border-box;
}

.el-footer {
  background-color: #f5f7fa;
  color: #333;
  line-height: 60px;
  text-align: center;
  border-top: 1px solid #e6e6e6;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.05);
}
</style>
