module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      }
    },
    // 🔥 新增：关闭webpack-dev-server的错误覆盖层弹窗
    client: {
      overlay: false
    }
  }
}