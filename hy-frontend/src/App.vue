<template>
  <!-- 路由视图出口：所有页面（登录/大厅/功能1）都在此渲染 -->
  <router-view />
</template>

<script setup>
// 无需额外代码，仅保留路由视图
</script>

<style>
/* 移除scoped，让样式全局生效！覆盖所有页面的基础样式、滚动条、Element Plus全局变量 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  height: 100%;
  /* 全局基础字体：保证中文清晰，适配所有页面 */
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
  /* 全局默认文字颜色：贴合海洋主题，不刺眼 */
  color: #334155;
  /* 禁止文字选中，提升界面体验 */
  user-select: none;
}

/* 🌟 全局滚动条海洋主题改造（替换浏览器默认灰色滚动条） */
::-webkit-scrollbar {
  /* 滚动条宽度 */
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  /* 滚动条轨道：浅蓝磨砂 */
  background: rgba(204, 229, 255, 0.3);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb {
  /* 滚动条滑块：海洋蓝渐变 */
  background: linear-gradient(180deg, #409eff, #0066cc);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  /* 滑块悬浮：加深蓝色 */
  background: linear-gradient(180deg, #3393ee, #0052aa);
}

/* 🌟 Element Plus 全局主题变量统一（海洋色系）
   覆盖所有El组件的默认配色，让按钮、消息、标签等与海洋主题一致
*/
:root {
  /* 主色：海洋蓝（匹配改造的所有页面） */
  --el-color-primary: #409eff;
  --el-color-primary-light-3: #66b1ff;
  --el-color-primary-light-5: #99cfff;
  --el-color-primary-light-7: #cce5ff;
  --el-color-primary-light-8: #e6f7ff;
  --el-color-primary-light-9: #f0f8ff;
  --el-color-primary-dark-2: #3393ee;

  /* 成功色：海洋绿（贴合生态合规主题） */
  --el-color-success: #52c41a;
  /* 危险色：珊瑚红（海洋主题对比色，不突兀） */
  --el-color-danger: #ff4d4f;
  /* 警告色：浅海黄 */
  --el-color-warning: #faad14;
  /* 信息色：浅蓝（与主色呼应） */
  --el-color-info: #8c8c8c;

  /* 全局圆角：统一所有El组件圆角，贴合现代UI */
  --el-border-radius-base: 8px;
  --el-border-radius-small: 4px;
  --el-border-radius-large: 12px;

  /* 全局阴影：统一浅蓝阴影，贴合海洋主题 */
  --el-box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  --el-box-shadow-light: 0 2px 8px rgba(64, 158, 255, 0.08);
  --el-box-shadow-dark: 0 8px 24px rgba(64, 158, 255, 0.15);
}

/* 🌟 全局Element Plus组件细节优化 */
/* 消息提示框：磨砂玻璃+圆角，贴合整体风格 */
.el-message {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(64, 158, 255, 0.2) !important;
  border-radius: 8px !important;
  box-shadow: var(--el-box-shadow) !important;
}
/* 标签组件：统一圆角，适配海洋主题 */
.el-tag {
  border-radius: 4px !important;
}
/* 加载动画：主色为海洋蓝，贴合主题 */
.el-loading-spinner__path {
  stroke: var(--el-color-primary) !important;
}
</style>