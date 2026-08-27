<template>
  <view class="page">
    <!-- 活动横幅 -->
    <view class="banner">
      <view class="banner-mask"></view>
      <view class="banner-content">
        <text class="banner-title">{{ activityName || '健步走线上活动' }}</text>
        <text class="banner-sub">{{ activityDates || '快乐工作 · 健康生活' }}</text>
        <view class="banner-tag">全县工会会员</view>
      </view>
    </view>

    <!-- 1. 活动信息 -->
    <view class="card">
      <view class="section-title">活动信息</view>
      <view class="info-line">
        <text class="info-label">活动名称</text>
        <text class="info-value">{{ activityName || '—' }}</text>
      </view>
      <view class="info-line">
        <text class="info-label">活动时间</text>
        <text class="info-value">{{ activityDates || '—' }}</text>
      </view>
    </view>

    <!-- 2. 个人报名信息 -->
    <view class="card">
      <view class="section-title">我的报名</view>
      <view class="info-line">
        <text class="info-label">姓名</text>
        <text class="info-value">{{ realName || '—' }}</text>
      </view>
      <view class="info-line">
        <text class="info-label">所在单位</text>
        <text class="info-value">{{ deptName || '—' }}</text>
      </view>
      <!-- 未报名 → 提示（报名需在 H5 报名页完成，小程序不含报名入口） -->
      <view v-if="!registered" class="reg-tip">
        <text>您尚未报名，无法使用小程序，请先在报名页完成报名</text>
      </view>
    </view>

    <!-- 3/4/5. 步数打卡 + 今日步数 + 达标状态 -->
    <view class="card checkin-card">
      <view class="section-title">步数打卡</view>

      <!-- 拒绝授权 -->
      <view v-if="stepAuth === 'denied'" class="auth-deny" @click="openStepSetting">
        <text>尚未授权获取微信步数，点击开启</text>
      </view>

      <!-- 未同步 → 开始打卡按钮 -->
      <view v-else-if="!syncedToday" class="checkin-btn" @click="handleCheckIn">
        <text class="checkin-text">{{ syncing ? '同步中...' : '开始打卡' }}</text>
      </view>

      <!-- 已同步 → 展示今日步数 + 达标状态 -->
      <view v-else class="checkin-done-area">
        <view class="done-tag">已打卡</view>
        <view class="step-show">
          <text class="step-num">{{ todaySteps }}</text>
          <text class="step-unit">今日步数</text>
        </view>
        <view class="reach-line">
          <text class="reach-label">今日达标状态（目标 {{ targetSteps }} 步）：</text>
          <text class="reach-val" :class="reached ? 'reached' : 'not-reached'">
            {{ reached ? '达标' : '未达标' }}
          </text>
        </view>
      </view>

      <!-- 封顶提示 -->
      <view class="cap-note">单日有效步数封顶 15000 步，超出部分不计入统计</view>
    </view>

    <!-- 6/7. 累计打卡天数 + 当前总积分 -->
    <view class="card">
      <view class="stats-row">
        <view class="stats-item">
          <text class="stats-num">{{ checkInDays }}</text>
          <text class="stats-label">累计打卡天数</text>
        </view>
        <view class="stats-item">
          <text class="stats-num">{{ totalPoints }}</text>
          <text class="stats-label">当前总积分</text>
        </view>
      </view>
      <view class="points-note">总积分 = 基础分 + 额外奖励分</view>
    </view>

    <!-- 打卡日历 -->
    <view class="card">
      <view class="cal-header">
        <text class="section-title">打卡日历</text>
        <view class="cal-streak">
          <text class="streak-num">{{ streakDays }}</text>
          <text class="streak-label">连续打卡（天）</text>
        </view>
      </view>
      <view class="cal-week">
        <view class="cal-week-item" v-for="w in weekDays" :key="w">{{ w }}</view>
      </view>
      <view class="cal-grid">
        <view class="cal-cell" :class="cell.cls" v-for="(cell, i) in calendarCells" :key="i">
          <text class="cal-day" v-if="cell.day">{{ cell.day }}</text>
          <text class="cal-steps" v-if="cell.day && cell.isChecked">{{ cell.steps }}</text>
        </view>
      </view>
      <view class="cal-legend">
        <view class="legend-item"><view class="legend-dot lg-reached"></view><text>达标</text></view>
        <view class="legend-item"><view class="legend-dot lg-checked"></view><text>已打卡</text></view>
        <view class="legend-item"><view class="legend-dot lg-today"></view><text>今天</text></view>
        <view class="legend-item"><view class="legend-dot lg-none"></view><text>未打卡</text></view>
      </view>
    </view>
  </view>
</template>

<script>
import request, { getToken, clearToken } from '@/utils/request'

let lastRedirect = 0

export default {
  data() {
    return {
      // 活动信息
      activityName: '',
      activityDates: '',
      // 个人报名
      realName: '',
      deptName: '',
      registered: false,
      // 打卡
      syncedToday: false,   // 今日是否已同步（打卡）
      todaySteps: 0,
      targetSteps: 7000,
      dailyCap: 15000,
      reached: false,
      syncing: false,
      stepAuth: 'undecided', // undecided | granted | denied
      // 统计
      checkInDays: 0,
      totalPoints: 0,
      streakDays: 0,
      // 日历
      records: [],
      weekDays: ['日', '一', '二', '三', '四', '五', '六']
    }
  },
  computed: {
    // 生成当月日历格子
    calendarCells() {
      const now = new Date()
      const year = now.getFullYear()
      const month = now.getMonth()
      const daysInMonth = new Date(year, month + 1, 0).getDate()
      const firstOffset = new Date(year, month, 1).getDay()
      const todayStr = this.fmtDate(now)
      const cells = []
      for (let i = 0; i < firstOffset; i++) {
        cells.push({ day: null })
      }
      for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${year}-${this.pad2(month + 1)}-${this.pad2(d)}`
        const rec = this.records.find((r) => r.date === dateStr)
        const isToday = dateStr === todayStr
        const isFuture = dateStr > todayStr
        let cls = 'cal-cell'
        if (isToday) cls += ' today'
        if (isFuture) cls += ' future'
        else if (rec && rec.reached) cls += ' checked reached'
        else if (rec) cls += ' checked'
        cells.push({
          day: d,
          date: dateStr,
          steps: rec ? rec.steps : 0,
          isChecked: !!rec,
          isToday,
          cls
        })
      }
      return cells
    }
  },
  onShow() {
    // 未登录 → 跳登录页（带冷却避免循环）
    if (!getToken()) {
      const now = Date.now()
      if (now - lastRedirect > 3000) {
        lastRedirect = now
        uni.reLaunch({ url: '/pages/login/login' })
      }
      return
    }
    this.checkStepAuth().then(() => {
      this.loadData()
      // 已授权且今日未同步 → 打开小程序自动同步步数
      this.autoSyncIfNeeded()
    })
  },
  methods: {
    fmtDate(date) {
      const y = date.getFullYear()
      const m = this.pad2(date.getMonth() + 1)
      const d = this.pad2(date.getDate())
      return `${y}-${m}-${d}`
    },
    // 两位补零（兼容旧基础库，避免真机不支持 padStart 导致白屏）
    pad2(n) {
      return n < 10 ? '0' + n : String(n)
    },
    // 检查微信运动授权状态
    checkStepAuth() {
      return new Promise((resolve) => {
        uni.getSetting({
          success: (res) => {
            if (res.authSetting['scope.werun'] === true) {
              this.stepAuth = 'granted'
            } else if (res.authSetting['scope.werun'] === false) {
              this.stepAuth = 'denied'
            }
            resolve()
          },
          fail: () => resolve()
        })
      })
    },

    // 打开小程序自动同步微信步数（无需点击打卡按钮）
    async autoSyncIfNeeded() {
      if (this.stepAuth !== 'granted' || this.syncedToday) return
      try {
        // #ifdef MP-WEIXIN
        const werun = await uni.getWeRunData()
        const data = await request('/step/sync', {
          method: 'POST',
          data: {
            activityId: this.activityId || '',
            encryptedData: werun.encryptedData,
            iv: werun.iv
          }
        })
        if (data) {
          this.todaySteps = data.todaySteps || 0
          this.syncedToday = true
          this.reached = !!data.reached
          this.checkInDays = data.checkInDays || 0
          this.totalPoints = data.totalPoints || 0
        }
        // #endif
      } catch (e) { /* 自动同步失败静默处理 */ }
    },

    // 加载首页数据：活动 + 报名 + 步数统计
    async loadData() {
      try {
        const activity = await request('/activity/current', { auth: false })
        if (activity) {
          this.activityName = activity.activityName
          this.activityDates = `${activity.startDate} ~ ${activity.endDate}`
        }
      } catch (e) { /* 后端未就绪 */ }

      try {
        const info = await request('/member/info')
        if (info) {
          this.realName = info.realName || ''
          this.deptName = info.deptName || ''
          this.registered = !!info.registered
        }
      } catch (e) {
        // 会员不存在/登录失效 → 清 token 并引导登录
        clearToken()
      } finally {
        // 未注册（未绑定报名身份）→ 强制引导去登录验证（用 reLaunch 避免与 request.js 的 reLaunch 叠加冲突）
        if (!this.registered) {
          const now = Date.now()
          if (now - lastRedirect > 3000) {
            lastRedirect = now
            uni.reLaunch({ url: '/pages/login/login' })
          }
          return
        }
      }

      try {
        const stats = await request('/step/today')
        if (stats) {
          this.syncedToday = !!stats.syncedToday
          this.todaySteps = stats.todaySteps || 0
          this.reached = !!stats.reached
          this.checkInDays = stats.checkInDays || 0
          this.totalPoints = stats.totalPoints || 0
          this.streakDays = stats.streakDays || 0
        }
      } catch (e) { /* 后端未就绪 */ }

      // 打卡日历记录
      try {
        const list = await request('/step/records')
        this.records = list || []
      } catch (e) { /* 后端未就绪 */ }
    },

    // 开始打卡：授权微信运动 → 同步步数
    async handleCheckIn() {
      this.syncing = true
      try {
        // #ifdef MP-WEIXIN
        // 请求微信运动授权
        await uni.authorize({ scope: 'scope.werun' })
        this.stepAuth = 'granted'

        // 拉取微信运动数据
        const werun = await uni.getWeRunData()
        const data = await request('/step/sync', {
          method: 'POST',
          data: {
            activityId: this.activityId || '',
            encryptedData: werun.encryptedData,
            iv: werun.iv
          }
        })

        // 同步成功 → 更新界面
        if (data) {
          this.todaySteps = data.todaySteps || 0
          this.syncedToday = true
          this.reached = !!data.reached
          this.checkInDays = data.checkInDays || 0
          this.totalPoints = data.totalPoints || 0
          uni.showToast({ title: '打卡成功', icon: 'success' })
        }
        // #endif
      } catch (e) {
        // 用户拒绝授权
        this.stepAuth = 'denied'
      } finally {
        this.syncing = false
      }
    },

    // 拒绝授权后引导去微信设置页开启
    openStepSetting() {
      uni.openSetting({
        success: (res) => {
          if (res.authSetting['scope.werun']) {
            this.stepAuth = 'granted'
            uni.showToast({ title: '授权成功，请点击开始打卡', icon: 'none' })
          }
        }
      })
    },

  }
}
</script>

<style lang="scss">
.banner {
  position: relative;
  height: 300rpx;
  overflow: hidden;
  background: linear-gradient(120deg, #E8392C 0%, #F07830 60%, #F5A623 100%);
  .banner-mask { position: absolute; inset: 0; background: radial-gradient(circle at 80% 20%, rgba(255,255,255,.15), transparent 60%); }
  .banner-content {
    position: absolute; left: 40rpx; top: 90rpx; display: flex; flex-direction: column;
    .banner-title { color: #fff; font-size: 44rpx; font-weight: bold; }
    .banner-sub { color: rgba(255,255,255,.9); font-size: 26rpx; margin-top: 12rpx; }
    .banner-tag { margin-top: 20rpx; align-self: flex-start; padding: 8rpx 20rpx; background: rgba(255,255,255,.25); color: #fff; font-size: 22rpx; border-radius: 999rpx; }
  }
}
.card { background: #fff; border-radius: 20rpx; margin: 24rpx; padding: 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,.04); }
.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; }
.info-line { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #F5F5F5; }
.info-line:last-child { border-bottom: none; }
.info-label { font-size: 28rpx; color: #666; }
.info-value { font-size: 28rpx; color: #333; }
.reg-tip { display: flex; align-items: center; justify-content: space-between; padding-top: 20rpx; }
.reg-tip text { font-size: 26rpx; color: #E8392C; }
.reg-btn { background: #E8392C; color: #fff; border-radius: 999rpx; margin: 0; }
.checkin-btn {
  height: 200rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(120deg, #E8392C, #F07830);
  .checkin-text { color: #fff; font-size: 40rpx; font-weight: bold; letter-spacing: 4rpx; }
}
.checkin-done-area { display: flex; flex-direction: column; align-items: center; padding: 20rpx 0; }
.done-tag { padding: 6rpx 24rpx; background: #E8F8F0; color: #3DB87C; font-size: 24rpx; border-radius: 999rpx; }
.step-show { display: flex; align-items: baseline; margin: 20rpx 0; }
.step-num { font-size: 80rpx; font-weight: bold; color: #E8392C; }
.step-unit { font-size: 26rpx; color: #999; margin-left: 12rpx; }
.reach-line { font-size: 26rpx; color: #666; }
.reach-label { color: #666; }
.reached { color: #3DB87C; font-weight: bold; }
.not-reached { color: #F5A623; font-weight: bold; }
.auth-deny {
  height: 140rpx; border: 2rpx dashed #F5A623; border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center; color: #E8392C; font-size: 28rpx;
}
.cap-note { font-size: 22rpx; color: #999; margin-top: 20rpx; text-align: center; }
.stats-row { display: flex; }
.stats-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.stats-num { font-size: 44rpx; font-weight: bold; color: #333; }
.stats-label { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.points-note { font-size: 22rpx; color: #999; text-align: center; margin-top: 20rpx; }

/* 打卡日历 */
.cal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.cal-streak { display: flex; flex-direction: column; align-items: center; }
.streak-num { font-size: 48rpx; font-weight: bold; color: #E8392C; }
.streak-label { font-size: 22rpx; color: #999; }
.cal-week { display: flex; }
.cal-week-item { flex: 1; text-align: center; font-size: 22rpx; color: #999; padding: 8rpx 0; }
.cal-grid { display: flex; flex-wrap: wrap; }
.cal-cell {
  width: 14.28%; aspect-ratio: 1; border-radius: 12rpx; box-sizing: border-box;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  font-size: 24rpx; color: #333; position: relative;
}
.cal-cell.checked { background: #FFF3EF; }
.cal-cell.checked.reached { background: #E8F8F0; }
.cal-cell.today { border: 2rpx solid #E8392C; }
.cal-cell.future { color: #ccc; }
.cal-steps { font-size: 16rpx; color: #999; }
.cal-legend { display: flex; justify-content: space-between; margin-top: 20rpx; padding-top: 16rpx; border-top: 1rpx solid #F2F2F2; }
.legend-item { display: flex; align-items: center; font-size: 20rpx; color: #999; }
.legend-dot { width: 20rpx; height: 20rpx; border-radius: 6rpx; margin-right: 8rpx; }
.lg-reached { background: #E8F8F0; border: 1rpx solid #3DB87C; }
.lg-checked { background: #FFF3EF; }
.lg-today { border: 2rpx solid #E8392C; background: #fff; }
.lg-none { background: #F7F8FA; }
</style>
