<template>
  <view class="page">
    <!-- 登录状态 -->
    <view v-if="step === 'loading'" class="center-box">
      <text class="loading-text">正在登录...</text>
    </view>

    <!-- 绑定手机号（输错只提示一次，留在本页可重输，不跳走、不循环） -->
    <view v-else-if="step === 'phone'" class="center-box">
      <view class="title-box">
        <text class="main-title">关联手机号</text>
        <text class="sub-title">请输入<text class="hl">报名时填写的手机号</text>（11位），用于匹配报名身份</text>
      </view>
      <view class="card">
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input class="form-input" type="number" maxlength="11" v-model="phone" @input="clearHint" placeholder="请输入报名时使用的手机号" />
        </view>
        <view class="phone-tip">提示：手机号必须与您在报名页填写的手机号完全一致</view>
        <button class="submit-btn" :loading="binding" @click="bindPhone">绑定并登录</button>
        <text v-if="loginError" class="bind-error">{{ loginError }}</text>
      </view>
    </view>

    <!-- 未报名提示（报名需在 H5 报名页完成，小程序不含报名入口） -->
    <view v-else-if="step === 'not-registered'" class="center-box">
      <view class="title-box">
        <text class="main-title">尚未报名</text>
        <text class="sub-title">该手机号未查询到报名记录，无法进入小程序。</text>
      </view>
      <view class="guide-box">
        <text class="guide-title">如何完成登录？</text>
        <text class="guide-step">1. 在手机浏览器打开报名页完成报名</text>
        <text class="guide-step">2. 报名时填写的手机号，必须与小程序里输入的一致</text>
        <text class="guide-step">3. 报名成功后再回来用同一手机号登录</text>
      </view>
      <button class="ghost-btn" @click="goBack">返回</button>
    </view>
  </view>
</template>

<script>
import request, { setToken } from '@/utils/request'

export default {
  data() {
    return {
      step: 'loading',      // loading | phone | not-registered
      phone: '',
      binding: false,
      loginError: ''
    }
  },
  onLoad() {
    this.wechatLogin()
  },
  methods: {
    // 微信 openid 静默登录
    async wechatLogin() {
      try {
        // #ifdef MP-WEIXIN
        const loginRes = await uni.login()
        const data = await request('/member/login', {
          method: 'POST',
          data: { xcxCode: loginRes.code },
          auth: false
        })
        // 关键：登录成功先存 token，后续绑定手机号接口需要带
        if (data && data.accessToken) {
          setToken(data.accessToken)
        }
        // 已绑定手机号 → 检查是否已报名注册
        if (data && data.needPhone === false) {
          try {
            const info = await request('/member/info')
            if (info && info.registered) {
              uni.switchTab({ url: '/pages/index/index' })
            } else {
              // 已绑手机号但未查到报名记录 → 回到绑定页让用户重输报名时的手机号（可换绑）
              // 注意：这里不设 loginError，避免一进页面就"提示输错"；只有提交后才提示
              this.step = 'phone'
            }
          } catch (e) {
            this.step = 'phone'
          }
          return
        }
        // 需要绑定手机号
        this.step = 'phone'
        // #endif
      } catch (e) {
        // 显示具体错误（排查用）
        this.loginError = (e && e.errMsg) || (e && e.message) || (e && e.msg) || '微信登录失败，请检查开发者工具登录账号是否有该小程序的权限'
        this.step = 'phone'
      }
    },

    // 绑定手机号并匹配报名记录
    async bindPhone() {
      if (!this.phone) {
        uni.showToast({ title: '请输入手机号', icon: 'none' })
        return
      }
      if (!/^1\d{10}$/.test(this.phone)) {
        uni.showToast({ title: '手机号格式不对：请输入 11 位、以 1 开头的手机号', icon: 'none' })
        return
      }
      this.binding = true
      try {
        const data = await request('/member/phone', {
          method: 'POST',
          data: { phone: this.phone }
        })
        // 匹配到 1 条报名 → 绑定成功进首页
        if (data && data.bound === true) {
          setToken(data.accessToken)
          uni.showToast({ title: '绑定成功', icon: 'success' })
          setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 600)
          return
        }
        // 匹配到 0 条报名 → 明确告知未报名（这是查无报名，不是输错）
        this.step = 'not-registered'
      } catch (e) {
        // 手机号错误/已被占用等：只提示一次，留在本页可重输，不再跳页造成循环
        this.loginError = (e && e.msg) || (e && e.message) || (e && e.errMsg) || '绑定失败，请检查网络'
        this.step = 'phone'
      } finally {
        this.binding = false
      }
    },

    // 重新输入手机号时清除上一次的提示（实现"只提示一次"）
    clearHint() {
      if (this.loginError) {
        this.loginError = ''
      }
    },

    goBack() {
      // "尚未报名" → 回到手机号输入重新绑定（避免跳首页触发 index↔login 导航循环导致超时白屏）
      if (this.step === 'not-registered') {
        this.step = 'phone'
        return
      }
      const pages = getCurrentPages()
      if (pages && pages.length > 1) {
        uni.navigateBack()
      } else {
        // 登录页是 reLaunch 进来的唯一一页，无上一页可返回，留在本页完成绑定
        uni.showToast({ title: '请先完成手机号绑定', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss">
.page { min-height: 100vh; background: #F5F6F8; }
.center-box { padding: 120rpx 40rpx; display: flex; flex-direction: column; align-items: center; }
.loading-text { font-size: 28rpx; color: #999; }
.error-box { margin: 20rpx 30rpx; padding: 24rpx; background: #FDE8E8; border: 1px solid #F5A623; border-radius: 16rpx; }
.error-title { display: block; font-size: 28rpx; font-weight: bold; color: #E8392C; margin-bottom: 8rpx; }
.error-text { display: block; font-size: 24rpx; color: #A8645E; line-height: 1.6; word-break: break-all; }
.title-box { margin-bottom: 50rpx; text-align: center; }
.main-title { display: block; font-size: 44rpx; font-weight: bold; color: #333; }
.sub-title { display: block; font-size: 26rpx; color: #999; margin-top: 16rpx; }
.hl { font-weight: bold; color: #666; }
.card { background: #fff; border-radius: 20rpx; padding: 40rpx; width: 100%; box-sizing: border-box; box-shadow: 0 4rpx 16rpx rgba(0,0,0,.04); }
.form-item { display: flex; align-items: center; margin-bottom: 40rpx; }
.form-label { width: 150rpx; font-size: 28rpx; color: #333; }
.form-input { flex: 1; height: 80rpx; background: #F7F8FA; border-radius: 12rpx; padding: 0 20rpx; font-size: 28rpx; }
.phone-tip { font-size: 22rpx; color: #999; margin-bottom: 24rpx; }
.bind-error { display: block; margin-top: 20rpx; font-size: 24rpx; color: #E8392C; line-height: 1.5; }
.submit-btn { background: #E8392C; color: #fff; border-radius: 999rpx; font-size: 30rpx; width: 100%; }
.ghost-btn { background: #F7F8FA; color: #666; border-radius: 999rpx; font-size: 30rpx; width: 100%; margin-top: 20rpx; }
.guide-box {
  width: 100%; background: #FFF6F5; border: 1px solid #F5A623; border-radius: 16rpx;
  padding: 24rpx; margin-bottom: 24rpx;
}
.guide-title { display: block; font-size: 28rpx; font-weight: bold; color: #E8392C; margin-bottom: 12rpx; }
.guide-step { display: block; font-size: 24rpx; color: #A8645E; line-height: 1.8; }
</style>
