/**
 * 数字滚动递增动效（requestAnimationFrame 驱动）
 * @param {HTMLElement} el        - 目标 DOM 元素（textContent 将被更新）
 * @param {number}      target    - 目标数值
 * @param {object}      options
 * @param {number}      options.duration - 动画时长(ms)，默认 1200
 * @param {string}      options.suffix   - 数字后缀，如 '%'
 * @param {string}      options.prefix   - 数字前缀
 */
export function countUp(el, target, { duration = 1200, suffix = '', prefix = '' } = {}) {
  if (!el || typeof target !== 'number') return
  const start = performance.now()
  const isFloat = !Number.isInteger(target)
  const decimals = isFloat
    ? (String(target.toFixed(4)).replace(/0+$/, '').split('.')[1]?.length || 1)
    : 0

  function step(now) {
    const t = Math.min((now - start) / duration, 1)
    // ease-out cubic: 先快后慢
    const eased = 1 - Math.pow(1 - t, 3)
    const current = target * eased
    const display = isFloat ? current.toFixed(decimals) : Math.round(current)
    el.textContent = `${prefix}${display}${suffix}`
    if (t < 1) {
      requestAnimationFrame(step)
    } else {
      el.textContent = `${prefix}${isFloat ? target.toFixed(decimals) : target}${suffix}`
    }
  }
  requestAnimationFrame(step)
}
