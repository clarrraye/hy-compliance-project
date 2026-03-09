<template>
  <div class="ocean-app">
    <!-- 鼠标跟随光晕（CSS 变量驱动，不影响性能） -->
    <div class="ocean-cursor-glow"></div>
    <!-- 路由视图：fade-slide 淡入+上移 20px 切换动效 -->
    <RouterView v-slot="{ Component, route }">
      <Transition name="fade-slide" mode="out-in">
        <component :is="Component" :key="route.path" />
      </Transition>
    </RouterView>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'

// ── 鼠标光晕：rAF 节流更新 CSS 变量 ──
let _glowRaf = null
const _onMouseMove = (e) => {
  if (_glowRaf) return
  _glowRaf = requestAnimationFrame(() => {
    document.documentElement.style.setProperty('--cursor-x', e.clientX + 'px')
    document.documentElement.style.setProperty('--cursor-y', e.clientY + 'px')
    _glowRaf = null
  })
}

// ── 全局按钮水波纹（事件委托，无需改动各页面按钮） ──
const _onButtonClick = (e) => {
  const btn = e.target.closest(
    '.el-button, .btn-add, .btn-export, .btn-chart, .btn-back, .btn-search, .menu-item'
  )
  if (!btn) return
  const rect = btn.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height) * 2.4
  const ripple = document.createElement('span')
  ripple.className = 'ocean-ripple'
  ripple.style.cssText = [
    `width:${size}px`,
    `height:${size}px`,
    `left:${e.clientX - rect.left - size / 2}px`,
    `top:${e.clientY - rect.top - size / 2}px`
  ].join(';')
  const prevPos = getComputedStyle(btn).position
  if (prevPos === 'static') btn.style.position = 'relative'
  btn.style.overflow = 'hidden'
  btn.appendChild(ripple)
  ripple.addEventListener('animationend', () => {
    ripple.remove()
    if (prevPos === 'static') btn.style.position = ''
  }, { once: true })
}

onMounted(() => {
  document.addEventListener('mousemove', _onMouseMove, { passive: true })
  document.addEventListener('click', _onButtonClick, true)
})
onBeforeUnmount(() => {
  document.removeEventListener('mousemove', _onMouseMove)
  document.removeEventListener('click', _onButtonClick, true)
})
</script>

<style>
/* ================================================================
   海渔合规管理平台 - 全局深海主题样式
   Deep Ocean Theme - Global Styles
   ================================================================ */

/* ===== CSS Reset ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #b0d8f0;
  user-select: none;
  /* 全局深海渐变背景 — 适当提亮，避免视觉疲劳 */
  background: linear-gradient(135deg, #0d1f3c 0%, #153250 50%, #0b1d35 100%);
  background-attachment: fixed;
}

/* ================================================================
   深海 CSS 变量系统
   ================================================================ */
:root {
  /* === 海洋主题核心色 === */
  --ocean-primary: #00d4ff;
  --ocean-secondary: #0099cc;
  --ocean-deep: #001a33;
  --ocean-surface: rgba(0, 180, 255, 0.08);
  --ocean-border: rgba(0, 212, 255, 0.3);
  --ocean-glow: 0 0 20px rgba(0, 212, 255, 0.4);
  --ocean-text: #b0d8f0;
  --ocean-text-bright: #e0f0ff;
  --ocean-text-muted: #7daec8;
  --ocean-bg-card: rgba(0, 20, 50, 0.7);
  --ocean-bg-overlay: rgba(10, 20, 40, 0.95);
  --ocean-bg-input: rgba(0, 30, 60, 0.6);
  --ocean-bg-header: rgba(0, 40, 80, 0.6);

  /* === Element Plus 主题变量覆盖 === */
  --el-color-primary: #00d4ff;
  --el-color-primary-light-3: #33ddff;
  --el-color-primary-light-5: #66e7ff;
  --el-color-primary-light-7: #99f0ff;
  --el-color-primary-light-8: #bbf5ff;
  --el-color-primary-light-9: #ddfaff;
  --el-color-primary-dark-2: #00b8e6;

  --el-color-success: #00d4aa;
  --el-color-success-light-3: #33ddbb;
  --el-color-success-light-5: #66e6cc;
  --el-color-success-light-7: #99efdd;
  --el-color-success-light-8: #bbf4e9;
  --el-color-success-light-9: #ddfaf4;
  --el-color-success-dark-2: #00b893;

  --el-color-danger: #ff6b6b;
  --el-color-danger-light-3: #ff8f8f;
  --el-color-danger-light-5: #ffb2b2;
  --el-color-danger-light-7: #ffd5d5;
  --el-color-danger-light-8: #ffe3e3;
  --el-color-danger-light-9: #fff1f1;
  --el-color-danger-dark-2: #e05555;

  --el-color-warning: #ffa500;
  --el-color-info: #7bb3c8;

  /* 圆角 */
  --el-border-radius-base: 8px;
  --el-border-radius-small: 4px;
  --el-border-radius-large: 12px;

  /* 阴影 */
  --el-box-shadow: 0 4px 12px rgba(0, 212, 255, 0.15);
  --el-box-shadow-light: 0 2px 8px rgba(0, 212, 255, 0.1);
  --el-box-shadow-dark: 0 8px 24px rgba(0, 212, 255, 0.2);

  /* 文字颜色 */
  --el-text-color-primary: #e0f0ff;
  --el-text-color-regular: #b0d8f0;
  --el-text-color-secondary: #7daec8;
  --el-text-color-placeholder: #6a90a8;
  --el-text-color-disabled: #4a6a78;

  /* 边框颜色 */
  --el-border-color: rgba(0, 212, 255, 0.3);
  --el-border-color-light: rgba(0, 212, 255, 0.2);
  --el-border-color-lighter: rgba(0, 212, 255, 0.15);
  --el-border-color-extra-light: rgba(0, 212, 255, 0.1);

  /* 填充颜色 */
  --el-fill-color: rgba(0, 180, 255, 0.06);
  --el-fill-color-light: rgba(0, 180, 255, 0.04);
  --el-fill-color-lighter: rgba(0, 180, 255, 0.02);
  --el-fill-color-blank: rgba(0, 20, 50, 0.6);

  /* 背景颜色 */
  --el-bg-color: rgba(5, 15, 35, 0.9);
  --el-bg-color-page: rgba(10, 14, 39, 0.95);
  --el-bg-color-overlay: rgba(5, 15, 40, 0.95);

  /* 遮罩 */
  --el-mask-color: rgba(0, 10, 30, 0.8);
  --el-mask-color-extra-light: rgba(0, 10, 30, 0.5);

  /* 禁用 */
  --el-disabled-bg-color: rgba(0, 20, 50, 0.4);
  --el-disabled-text-color: #4a7a8a;
  --el-disabled-border-color: rgba(0, 212, 255, 0.1);
}

/* ================================================================
   全局滚动条 - 深海蓝
   ================================================================ */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track {
  background: rgba(0, 20, 50, 0.5);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #00d4ff, #0066cc);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #33ddff, #0080ff);
}

/* ================================================================
   全局标题发光效果
   ================================================================ */
h1, h2, h3 {
  font-weight: bold;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.6);
}

/* ================================================================
   Element Plus 全局组件深海主题覆盖
   ================================================================ */

/* === el-card：深海玻璃卡片 === */
.el-card {
  background: var(--ocean-bg-card) !important;
  border: 1px solid var(--ocean-border) !important;
  backdrop-filter: blur(10px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
  box-shadow: 0 4px 16px rgba(0, 212, 255, 0.1),
              inset 0 1px 0 rgba(0, 212, 255, 0.15) !important;
  color: var(--ocean-text) !important;
  border-radius: 12px !important;
}
.el-card__header {
  background: var(--ocean-bg-header) !important;
  border-bottom: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text-bright) !important;
  font-weight: bold;
}
.el-card__body { color: var(--ocean-text); }

/* === el-button === */
.el-button {
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}
.el-button--primary {
  background: linear-gradient(135deg, #0099cc, #00d4ff) !important;
  border: 1px solid rgba(0, 212, 255, 0.5) !important;
  color: #001a33 !important;
  font-weight: 600 !important;
}
.el-button--primary:hover {
  background: linear-gradient(135deg, #00b8e6, #33ddff) !important;
  box-shadow: var(--ocean-glow) !important;
  transform: translateY(-2px) !important;
  border-color: var(--ocean-primary) !important;
}
.el-button--primary:active {
  transform: translateY(0) !important;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.3) !important;
}
/* 默认按钮（非类型） */
.el-button:not([class*="el-button--"]),
.el-button.el-button--default {
  background: rgba(0, 60, 100, 0.4) !important;
  border: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text) !important;
}
.el-button:not([class*="el-button--"]):hover,
.el-button.el-button--default:hover {
  background: rgba(0, 100, 150, 0.4) !important;
  border-color: var(--ocean-primary) !important;
  color: var(--ocean-primary) !important;
}
/* 成功按钮 */
.el-button--success {
  background: linear-gradient(135deg, #009977, #00d4aa) !important;
  border-color: #00d4aa !important;
  color: #001a28 !important;
}
.el-button--success:hover {
  background: linear-gradient(135deg, #00b890, #33ddbb) !important;
  box-shadow: 0 0 15px rgba(0, 212, 170, 0.4) !important;
  transform: translateY(-2px) !important;
}
/* 危险按钮 */
.el-button--danger {
  background: linear-gradient(135deg, #cc4444, #ff6b6b) !important;
  border-color: #ff6b6b !important;
  color: #fff !important;
}
.el-button--danger:hover {
  background: linear-gradient(135deg, #e05555, #ff8888) !important;
  box-shadow: 0 0 15px rgba(255, 107, 107, 0.4) !important;
  transform: translateY(-2px) !important;
}
/* 信息按钮 */
.el-button--info {
  background: rgba(0, 100, 150, 0.5) !important;
  border-color: rgba(0, 180, 255, 0.4) !important;
  color: var(--ocean-text) !important;
}
.el-button--info:hover {
  background: rgba(0, 130, 180, 0.5) !important;
  transform: translateY(-2px) !important;
}
/* text 类型按钮 */
.el-button--text, .el-button[type="text"] {
  color: var(--ocean-primary) !important;
  background: transparent !important;
  border: none !important;
}
.el-button--text:hover, .el-button[type="text"]:hover {
  color: #33ddff !important;
  text-shadow: 0 0 6px rgba(0, 212, 255, 0.5) !important;
}

/* === el-table：深海数据表格 === */
.el-table {
  background: transparent !important;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(0, 40, 80, 0.6);
  --el-table-row-hover-bg-color: rgba(0, 180, 255, 0.08);
  --el-table-border-color: rgba(0, 212, 255, 0.2);
  --el-table-text-color: #b0d8f0;
  --el-table-header-text-color: #00d4ff;
  color: var(--ocean-text) !important;
  border-color: rgba(0, 212, 255, 0.2) !important;
}
.el-table th.el-table__cell {
  background: rgba(0, 40, 80, 0.6) !important;
  color: #00d4ff !important;
  font-weight: bold !important;
  border-bottom: 1px solid rgba(0, 212, 255, 0.3) !important;
  border-right: 1px solid rgba(0, 212, 255, 0.15) !important;
}
.el-table td.el-table__cell {
  background: transparent !important;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1) !important;
  border-right: 1px solid rgba(0, 212, 255, 0.1) !important;
  color: var(--ocean-text) !important;
}
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: rgba(0, 60, 100, 0.2) !important;
}
.el-table__body tr:hover td.el-table__cell {
  background: rgba(0, 180, 255, 0.1) !important;
}
.el-table--border::after,
.el-table--border::before,
.el-table__inner-wrapper::before {
  background-color: rgba(0, 212, 255, 0.2) !important;
}
.el-table__empty-text { color: var(--ocean-text-muted) !important; }
.el-empty__description p { color: var(--ocean-text-muted) !important; }

/* === el-input === */
.el-input__wrapper {
  background: var(--ocean-bg-input) !important;
  border: 1px solid var(--ocean-border) !important;
  box-shadow: none !important;
  transition: all 0.3s ease !important;
}
.el-input__wrapper:hover { border-color: var(--ocean-primary) !important; }
.el-input__wrapper.is-focus {
  border-color: var(--ocean-primary) !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.3) !important;
}
.el-input__inner {
  color: var(--ocean-text-bright) !important;
  background: transparent !important;
}
.el-input__inner::placeholder { color: #6a90a8 !important; }
.el-input__prefix, .el-input__suffix { color: var(--ocean-primary) !important; }
/* textarea */
.el-textarea__inner {
  background: var(--ocean-bg-input) !important;
  border: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text-bright) !important;
}
.el-textarea__inner:focus {
  border-color: var(--ocean-primary) !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.3) !important;
}

/* === el-select === */
.el-select .el-input__wrapper { background: var(--ocean-bg-input) !important; }
.el-select-dropdown,
.el-select__popper {
  background: rgba(5, 20, 45, 0.97) !important;
  border: 1px solid var(--ocean-border) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
}
.el-select-dropdown__item { color: var(--ocean-text) !important; }
.el-select-dropdown__item.hover,
.el-select-dropdown__item:hover {
  background: rgba(0, 180, 255, 0.15) !important;
  color: var(--ocean-primary) !important;
}
.el-select-dropdown__item.selected {
  color: var(--ocean-primary) !important;
  background: rgba(0, 212, 255, 0.1) !important;
  font-weight: bold;
}
.el-select-dropdown__empty { color: var(--ocean-text-muted) !important; }

/* === el-dialog：深海浮层 === */
.el-dialog {
  background: var(--ocean-bg-overlay) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid var(--ocean-border) !important;
  border-top: 2px solid var(--ocean-primary) !important;
  border-radius: 12px !important;
  box-shadow: 0 0 40px rgba(0, 212, 255, 0.2),
              0 20px 60px rgba(0, 0, 0, 0.5) !important;
}
.el-dialog__header {
  background: rgba(0, 40, 80, 0.5) !important;
  border-bottom: 1px solid var(--ocean-border) !important;
  padding: 16px 20px !important;
}
.el-dialog__title {
  color: var(--ocean-primary) !important;
  font-weight: bold !important;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.5) !important;
}
.el-dialog__headerbtn .el-dialog__close { color: var(--ocean-text) !important; }
.el-dialog__headerbtn:hover .el-dialog__close { color: var(--ocean-primary) !important; }
.el-dialog__body { color: var(--ocean-text) !important; }
.el-dialog__footer {
  border-top: 1px solid var(--ocean-border) !important;
  background: rgba(0, 20, 50, 0.3) !important;
}

/* === el-tabs：海洋主题标签页 === */
.el-tabs__nav-wrap::after {
  background-color: rgba(0, 212, 255, 0.2) !important;
}
.el-tabs__item {
  color: var(--ocean-text) !important;
  transition: all 0.3s ease !important;
}
.el-tabs__item:hover {
  color: var(--ocean-primary) !important;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.5) !important;
}
.el-tabs__item.is-active {
  color: var(--ocean-primary) !important;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.5) !important;
  font-weight: bold !important;
}
.el-tabs__active-bar {
  background: linear-gradient(90deg, #0099cc, #00d4ff) !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.6) !important;
  height: 2px !important;
}
/* card 类型 tab */
.el-tabs--card > .el-tabs__header { background: rgba(0, 10, 30, 0.5) !important; }
.el-tabs--card > .el-tabs__header .el-tabs__nav {
  border: 1px solid var(--ocean-border) !important;
}
.el-tabs--card > .el-tabs__header .el-tabs__item {
  border-left: 1px solid var(--ocean-border) !important;
  background: rgba(0, 20, 50, 0.5) !important;
  color: var(--ocean-text) !important;
}
.el-tabs--card > .el-tabs__header .el-tabs__item.is-active {
  background: rgba(0, 180, 255, 0.15) !important;
  border-bottom: 1px solid rgba(0, 180, 255, 0.15) !important;
  color: var(--ocean-primary) !important;
}

/* === el-form === */
.el-form-item__label { color: var(--ocean-text) !important; }
.el-form-item__error { color: #ff6b6b !important; }

/* === el-pagination：深海分页 === */
.el-pagination { color: var(--ocean-text) !important; }
.el-pagination button {
  background: var(--ocean-bg-input) !important;
  color: var(--ocean-text) !important;
  border: 1px solid var(--ocean-border) !important;
}
.el-pagination button:hover { color: var(--ocean-primary) !important; }
.el-pagination .el-pager li {
  background: var(--ocean-bg-input) !important;
  color: var(--ocean-text) !important;
  border: 1px solid rgba(0, 212, 255, 0.15) !important;
  transition: all 0.3s ease !important;
}
.el-pagination .el-pager li.is-active {
  background: linear-gradient(135deg, #0099cc, #00d4ff) !important;
  color: #001a33 !important;
  border-color: var(--ocean-primary) !important;
  font-weight: bold !important;
}
.el-pagination .el-pager li:hover { color: var(--ocean-primary) !important; }
.el-pagination__sizes .el-input__wrapper,
.el-pagination__jump .el-input__wrapper {
  background: var(--ocean-bg-input) !important;
  border: 1px solid var(--ocean-border) !important;
}
.el-pagination__total,
.el-pagination__jump { color: var(--ocean-text) !important; }

/* === el-message 消息提示 === */
.el-message {
  background: rgba(10, 25, 50, 0.97) !important;
  backdrop-filter: blur(12px) !important;
  border: 1px solid var(--ocean-border) !important;
  border-radius: 8px !important;
  box-shadow: var(--ocean-glow) !important;
}
.el-message--success { border-left: 3px solid #00d4aa !important; }
.el-message--warning { border-left: 3px solid #ffa500 !important; }
.el-message--error   { border-left: 3px solid #ff6b6b !important; }
.el-message--info    { border-left: 3px solid var(--ocean-primary) !important; }
.el-message .el-message__content { color: #e0f0ff !important; }
.el-message .el-message__icon { filter: drop-shadow(0 0 4px currentColor); }

/* === el-tag === */
.el-tag { border-radius: 4px !important; }
.el-tag--success {
  background: rgba(0, 212, 170, 0.15) !important;
  border-color: rgba(0, 212, 170, 0.4) !important;
  color: #00d4aa !important;
}
.el-tag--danger {
  background: rgba(255, 107, 107, 0.15) !important;
  border-color: rgba(255, 107, 107, 0.4) !important;
  color: #ff6b6b !important;
}
.el-tag--warning {
  background: rgba(255, 165, 0, 0.15) !important;
  border-color: rgba(255, 165, 0, 0.4) !important;
  color: #ffa500 !important;
}
.el-tag--info {
  background: rgba(0, 180, 255, 0.1) !important;
  border-color: rgba(0, 180, 255, 0.3) !important;
  color: var(--ocean-text) !important;
}

/* === el-radio-button === */
.el-radio-button__inner {
  background: var(--ocean-bg-input) !important;
  border: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text) !important;
  transition: all 0.3s ease !important;
}
.el-radio-button__original-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #0099cc, #00d4ff) !important;
  border-color: var(--ocean-primary) !important;
  color: #001a33 !important;
  box-shadow: -1px 0 0 0 var(--ocean-primary), var(--ocean-glow) !important;
  font-weight: 600 !important;
}
.el-radio-button__inner:hover { color: var(--ocean-primary) !important; }

/* === el-loading 加载动画 === */
.el-loading-spinner .path { stroke: var(--ocean-primary) !important; }
.el-loading-spinner .el-loading-text { color: var(--ocean-primary) !important; }
.el-loading-mask {
  background: rgba(0, 10, 30, 0.7) !important;
  backdrop-filter: blur(4px) !important;
}

/* ================================================================
   通用动效规范
   ================================================================ */

/* 页面根容器 */
.ocean-app {
  width: 100%;
  height: 100%;
  position: relative;
}

/* ── 鼠标跟随光晕 ── */
:root {
  --cursor-x: -999px;
  --cursor-y: -999px;
}
.ocean-cursor-glow {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9999;
  background: radial-gradient(
    circle 340px at var(--cursor-x) var(--cursor-y),
    rgba(0, 212, 255, 0.055) 0%,
    transparent 70%
  );
}

/* ── 路由切换：fade-slide（淡入 + 向上平移 20px） ── */
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-slide-enter-active {
  transition: opacity 0.32s ease, transform 0.32s ease;
}
.fade-slide-enter-to {
  opacity: 1;
  transform: translateY(0);
}
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}
.fade-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-14px);
}

/* ── 按钮水波纹 ── */
.ocean-ripple {
  position: absolute;
  border-radius: 50%;
  background: rgba(0, 212, 255, 0.25);
  transform: scale(0);
  animation: ocean-ripple-expand 0.6s ease-out forwards;
  pointer-events: none;
  z-index: 0;
}
@keyframes ocean-ripple-expand {
  from { transform: scale(0); opacity: 0.9; }
  to   { transform: scale(1);  opacity: 0;   }
}

/* ── v-stagger 指令：卡片从下方淡入 ── */
[data-stagger] {
  will-change: opacity, transform;
}

/* === el-dropdown-menu === */
.el-dropdown-menu,
.el-dropdown__popper {
  background: rgba(5, 20, 45, 0.97) !important;
  border: 1px solid var(--ocean-border) !important;
  backdrop-filter: blur(12px) !important;
  box-shadow: var(--ocean-glow) !important;
}
.el-dropdown-menu__item { color: var(--ocean-text) !important; }
.el-dropdown-menu__item:hover,
.el-dropdown-menu__item:focus {
  background: rgba(0, 180, 255, 0.1) !important;
  color: var(--ocean-primary) !important;
}
.el-dropdown-menu__item--divided {
  border-top: 1px solid var(--ocean-border) !important;
}

/* === el-avatar === */
.el-avatar {
  background: linear-gradient(135deg, #0099cc, #00d4ff) !important;
  color: #001a33 !important;
}

/* === el-divider === */
.el-divider { border-color: rgba(0, 212, 255, 0.2) !important; }
.el-divider__text {
  background: transparent !important;
  color: var(--ocean-text) !important;
}

/* === el-date-picker === */
.el-date-picker,
.el-picker-panel {
  background: rgba(5, 20, 45, 0.97) !important;
  border: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text) !important;
  backdrop-filter: blur(12px) !important;
}
.el-date-picker__header-label,
.el-date-table th,
.el-picker-panel__icon-btn { color: var(--ocean-text) !important; }
.el-date-table td.today span {
  color: var(--ocean-primary) !important;
  font-weight: bold !important;
}
.el-date-table td.available:hover span {
  background: rgba(0, 180, 255, 0.2) !important;
  color: var(--ocean-primary) !important;
}
.el-date-table td.current:not(.disabled) span {
  background: var(--ocean-primary) !important;
  color: #001a33 !important;
}
.el-date-table td { color: var(--ocean-text) !important; }
.el-date-picker__header .el-picker-panel__icon-btn {
  color: var(--ocean-text) !important;
}

/* === el-alert === */
.el-alert--success {
  background: rgba(0, 212, 170, 0.1) !important;
  border: 1px solid rgba(0, 212, 170, 0.3) !important;
}
.el-alert--warning {
  background: rgba(255, 165, 0, 0.1) !important;
  border: 1px solid rgba(255, 165, 0, 0.3) !important;
}
.el-alert--error {
  background: rgba(255, 107, 107, 0.1) !important;
  border: 1px solid rgba(255, 107, 107, 0.3) !important;
}
.el-alert--info {
  background: rgba(0, 180, 255, 0.1) !important;
  border: 1px solid rgba(0, 180, 255, 0.3) !important;
}
.el-alert .el-alert__title,
.el-alert .el-alert__description { color: var(--ocean-text) !important; }

/* === el-upload === */
.el-upload-dragger {
  background: rgba(0, 30, 60, 0.5) !important;
  border: 2px dashed var(--ocean-border) !important;
  color: var(--ocean-text) !important;
  transition: all 0.3s ease !important;
}
.el-upload-dragger:hover {
  border-color: var(--ocean-primary) !important;
  background: rgba(0, 180, 255, 0.08) !important;
  box-shadow: inset 0 0 20px rgba(0, 212, 255, 0.1) !important;
}
.el-upload__text { color: var(--ocean-text) !important; }
.el-upload__text em { color: var(--ocean-primary) !important; }
.el-upload__tip { color: var(--ocean-text-muted) !important; }

/* === el-progress === */
.el-progress-bar__inner {
  background: linear-gradient(90deg, #0099cc, #00d4ff) !important;
}
.el-progress-bar__outer { background: rgba(0, 30, 60, 0.5) !important; }
.el-progress__text { color: var(--ocean-text) !important; }

/* === el-page-header === */
.el-page-header { color: var(--ocean-text) !important; }
.el-page-header__title,
.el-page-header__back { color: var(--ocean-text) !important; }
.el-page-header__back:hover { color: var(--ocean-primary) !important; }
.el-page-header__content {
  color: var(--ocean-primary) !important;
  font-weight: bold !important;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.5) !important;
}

/* === el-message-box（二次确认弹窗）=== */
.el-message-box {
  background: var(--ocean-bg-overlay) !important;
  backdrop-filter: blur(20px) !important;
  border: 1px solid var(--ocean-border) !important;
  border-top: 2px solid var(--ocean-primary) !important;
  border-radius: 12px !important;
  box-shadow: 0 0 40px rgba(0, 212, 255, 0.2) !important;
}
.el-message-box__header {
  background: rgba(0, 40, 80, 0.5) !important;
  border-bottom: 1px solid var(--ocean-border) !important;
}
.el-message-box__title {
  color: var(--ocean-primary) !important;
  font-weight: bold !important;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.4) !important;
}
.el-message-box__content { color: var(--ocean-text) !important; }
.el-message-box__close { color: var(--ocean-text) !important; }

/* === el-tooltip === */
.el-popper.is-light,
.el-tooltip__popper.is-light {
  background: rgba(5, 25, 50, 0.97) !important;
  border: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text) !important;
  backdrop-filter: blur(10px) !important;
}
.el-popper.is-light .el-popper__arrow::before,
.el-tooltip__popper.is-light .el-popper__arrow::before {
  border-color: var(--ocean-border) !important;
  background: rgba(5, 25, 50, 0.97) !important;
}
.el-popper.is-dark {
  background: rgba(0, 180, 255, 0.15) !important;
  border: 1px solid var(--ocean-border) !important;
  color: var(--ocean-text-bright) !important;
}

/* === el-empty === */
.el-empty__description p { color: var(--ocean-text-muted) !important; }

/* === el-checkbox === */
.el-checkbox__label { color: var(--ocean-text) !important; }
.el-checkbox__inner {
  background: var(--ocean-bg-input) !important;
  border-color: var(--ocean-border) !important;
}
.el-checkbox.is-checked .el-checkbox__inner {
  background: var(--ocean-primary) !important;
  border-color: var(--ocean-primary) !important;
}

/* === el-radio === */
.el-radio__label { color: var(--ocean-text) !important; }
.el-radio__inner {
  background: var(--ocean-bg-input) !important;
  border-color: var(--ocean-border) !important;
}
.el-radio.is-checked .el-radio__inner {
  background: var(--ocean-primary) !important;
  border-color: var(--ocean-primary) !important;
}
</style>