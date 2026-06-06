<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">商家后台</div>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/">首页</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main><router-view /></el-main>
  </el-container>
</template>

<style scoped>
.layout { min-height: 100vh; }
.logo { padding: 20px 16px; font-weight: 600; color: #409eff; }
</style>
