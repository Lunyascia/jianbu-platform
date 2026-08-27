<script>
export default {
  onLaunch() {
    // 真机白屏排查：捕获全局 JS 错误，弹窗显示具体原因（定位后移除）
    if (typeof wx !== 'undefined' && wx.onError) {
      wx.onError(function (err) {
        const msg = err && (err.message || err.stack) ? (err.message || err.stack) : String(err)
        uni.showModal({
          title: '运行错误（白屏原因）',
          content: String(msg).slice(0, 300),
          showCancel: false
        })
      })
    }
  }
}
</script>

<style lang="scss">
/* 全局样式 */
page {
  background-color: #F5F6F8;
  font-size: 28rpx;
  color: #333;
}
</style>
