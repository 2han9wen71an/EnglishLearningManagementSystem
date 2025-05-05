<template>
  <div class="admin-layout">
    <el-container class="full-height">
      <el-aside width="220px">
        <admin-sidebar />
      </el-aside>
      <el-container>
        <el-header height="60px">
          <admin-header />
        </el-header>
        <el-main>
          <router-view />
        </el-main>
        <el-footer height="60px">
          <admin-footer />
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import AdminFooter from '@/components/admin/AdminFooter.vue'
</script>

<style scoped>
.admin-layout {
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
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  padding: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.el-aside {
  background-color: #304156;
  color: #fff;
  overflow-y: auto;
  height: 100%;
}

.el-main {
  background-color: #f0f2f5;
  color: #333;
  padding: 20px;
  overflow-y: auto;
  height: 100%;
  box-sizing: border-box;
}

.el-footer {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  text-align: center;
  border-top: 1px solid #e6e6e6;
}
</style>
