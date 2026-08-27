<template>
  <view class="page">
    <!-- 个人信息头部 -->
    <view class="profile">
      <view class="avatar">{{ avatarText }}</view>
      <view class="profile-info">
        <text class="profile-name">{{ realName || '未登录' }}</text>
        <text class="profile-meta">{{ deptName || '请先登录' }}</text>
        <text class="profile-meta">{{ phone || '' }}</text>
      </view>
      <view class="login-btn" @click="handleLogin">{{ token ? '已登录' : '登录' }}</view>
    </view>

    <!-- 个人信息明细 -->
    <view class="card">
      <view class="info-row"><text class="info-label">姓名</text><text class="info-value">{{ realName || '-' }}</text></view>
      <view class="info-row"><text class="info-label">手机号</text><text class="info-value">{{ phone || '-' }}</text></view>
      <view class="info-row"><text class="info-label">单位</text><text class="info-value">{{ deptName || '-' }}</text></view>
      <view class="info-row"><text class="info-label">报名状态</text><text class="info-value">{{ regStatus || '-' }}</text></view>
    </view>

    <!-- 我的数据统计 -->
    <view class="card">
      <view class="section-title">我的数据</view>
      <view class="stat-grid">
        <view class="stat-box">
          <text class="stat-num">{{ checkInDays }}</text>
          <text class="stat-label">累计打卡天数</text>
        </view>
        <view class="stat-box">
          <text class="stat-num">{{ streakDays }}</text>
          <text class="stat-label">当前连续打卡</text>
        </view>
        <view class="stat-box">
          <text class="stat-num">{{ totalSteps }}</text>
          <text class="stat-label">活动总步数</text>
        </view>
        <view class="stat-box">
          <text class="stat-num">{{ totalPoints }}</text>
          <text class="stat-label">当前积分</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request, { getToken } from '@/utils/request'

export default {
  data() {
    return {
      token: '',
      realName: '',
      phone: '',
      deptName: '',
      regStatus: '',
      // 数据统计
      checkInDays: 0,
      streakDays: 0,
      totalSteps: 0,
      totalPoints: 0
    }
  },
  computed: {
    avatarText() {
      return (this.realName || '会').slice(0, 1)
    }
  },
  onShow() {
    this.token = getToken()
    if (this.token) {
      this.loadInfo()
    }
  },
  methods: {
    async loadInfo() {
      try {
        const data = await request('/member/info')
        if (data) {
          this.realName = data.realName || ''
          this.phone = data.phone || ''
          this.deptName = data.deptName || ''
          this.regStatus = data.regStatusText || ''
        }
      } catch (e) { /* 登录失效会由请求层统一跳登录页 */ }
      // 数据统计
      try {
        const stats = await request('/step/today')
        if (stats) {
          this.checkInDays = stats.checkInDays || 0
          this.streakDays = stats.streakDays || 0
          this.totalSteps = stats.totalSteps || 0
          this.totalPoints = stats.totalPoints || 0
        }
      } catch (e) { /* 后端未就绪 */ }
    },
    handleLogin() {
      if (!this.token) {
        uni.navigateTo({ url: '/pages/login/login' })
      }
    }
  }
}
</script>

<style lang="scss">
.profile {
  display: flex; align-items: center; padding: 60rpx 40rpx; color: #fff;
  background: linear-gradient(135deg, #E8392C, #F07830);
}
.avatar {
  width: 110rpx; height: 110rpx; border-radius: 50%; background: rgba(255,255,255,.3);
  display: flex; align-items: center; justify-content: center; font-size: 52rpx;
}
.profile-info { flex: 1; margin-left: 24rpx; display: flex; flex-direction: column; }
.profile-name { font-size: 36rpx; font-weight: bold; }
.profile-meta { font-size: 24rpx; opacity: .85; margin-top: 6rpx; }
.login-btn { padding: 12rpx 30rpx; background: rgba(255,255,255,.25); border-radius: 999rpx; font-size: 26rpx; }
.card { background: #fff; border-radius: 20rpx; margin: 24rpx; padding: 10rpx 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,.04); }
.info-row { display: flex; justify-content: space-between; align-items: center; height: 96rpx; border-bottom: 1rpx solid #F5F5F5; }
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 28rpx; color: #666; }
.info-value { font-size: 28rpx; color: #333; }

/* 我的数据统计 */
.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; }
.stat-grid { display: flex; flex-wrap: wrap; }
.stat-box { width: 50%; display: flex; flex-direction: column; align-items: center; padding: 24rpx 0; }
.stat-num { font-size: 44rpx; font-weight: bold; color: #E8392C; }
.stat-label { font-size: 22rpx; color: #999; margin-top: 8rpx; }
</style>
