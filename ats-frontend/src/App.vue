<template>
  <div id="app">
    <header class="app-header">
      <div class="brand">
        <div class="logo">📋</div>
        <h1>ATS · 招聘岗位管理</h1>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push('/positions')" :type="$route.name === 'PositionList' ? 'primary' : 'default'" size="small">
          岗位列表
        </el-button>
        <el-button @click="$router.push('/dashboard')" :type="$route.name === 'Dashboard' ? 'primary' : 'default'" size="small">
          数据统计
        </el-button>
        <el-button @click="$router.push('/positions/create')" :type="$route.name === 'PositionCreate' ? 'primary' : 'default'" size="small">
          新建岗位
        </el-button>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>

<style>
@import './styles/design-system.css';
</style>
