<template>
  <view class="page">
    <!-- ========== 我的获奖（仅中奖用户显示） ========== -->
    <view v-if="hasAward" class="my-award">
      <!-- 获奖状态 -->
      <view class="win-card">
        <view class="win-badge">{{ awardLevel }}</view>
        <text class="win-tip">恭喜您获奖！</text>
      </view>

      <!-- 电子荣誉证书（动态填充） -->
      <view class="certificate">
        <view class="cert-deco"></view>
        <view class="cert-content">
          <text class="cert-title">荣 誉 证 书</text>
          <view class="cert-line"></view>
          <text class="cert-name">{{ realName }}</text>
          <text class="cert-body">在「{{ activityName }}」活动中荣获</text>
          <text class="cert-level">{{ awardLevel }}</text>
          <text class="cert-body">特发此证，以资鼓励</text>
          <text class="cert-unit">{{ deptName }}</text>
          <text class="cert-date">{{ issueDate }}</text>
        </view>
        <view class="cert-deco cert-deco-bottom"></view>
      </view>

      <!-- 收货地址填写 -->
      <view class="card">
        <view class="section-title">收货地址</view>
        <view class="form-item">
          <text class="form-label">收货人</text>
          <input class="form-input" v-model="addr.receiver" placeholder="请输入收货人姓名" />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input class="form-input" type="number" maxlength="11" v-model="addr.phone" placeholder="请输入收货手机号" />
        </view>
        <view class="form-item">
          <text class="form-label">详细地址</text>
          <input class="form-input" v-model="addr.address" placeholder="请输入详细收货地址" />
        </view>
        <button class="save-btn" :loading="saving" @click="saveAddr">保存地址</button>
      </view>
    </view>

    <!-- ========== 活动奖项设置（后台"奖励管理"配置，动态展示） ========== -->
    <view class="section-head">个人奖项</view>
    <view class="card" v-for="a in personalAwards" :key="a.id">
      <view class="award-row">
        <text class="award-medal">{{ medalOf(a.awardName) }}</text>
        <view class="award-info">
          <text class="award-title">{{ a.awardName }}</text>
          <text class="award-rank">名额：{{ quotaOf(a) }}</text>
          <text class="award-desc">{{ a.prizeContent }}</text>
        </view>
      </view>
    </view>

    <view class="section-head">集体奖项</view>
    <view class="card" v-for="a in collectiveAwards" :key="a.id">
      <view class="award-row">
        <text class="award-medal">🏅</text>
        <view class="award-info">
          <text class="award-title">{{ a.awardName }}</text>
          <text class="award-rank">名额：{{ quotaOf(a) }}</text>
          <text class="award-desc">{{ a.prizeContent }}</text>
        </view>
      </view>
    </view>
    <view v-if="!personalAwards.length && !collectiveAwards.length" class="empty-tip">奖励设置由后台发布</view>
    <view class="card note">
      <text class="note-text">后台将自动生成各单位参与率、平均积分参考排名，供管理员评选先进组织单位。</text>
    </view>
  </view>
</template>

<script>
import request from '@/utils/request'

export default {
  data() {
    return {
      // 我的获奖
      hasAward: false,
      awardLevel: '',
      realName: '',
      deptName: '',
      activityName: '健步走线上活动',
      issueDate: '',
      saving: false,
      addr: { receiver: '', phone: '', address: '' },
      // 奖项设置（后台"奖励管理"配置）
      personalAwards: [],
      collectiveAwards: []
    }
  },
  onShow() {
    this.loadMyAward()
    this.loadAwards()
  },
  methods: {
    // 奖项名称 → 奖牌 emoji
    medalOf(name) {
      if (!name) return '🎖'
      if (name.indexOf('一') !== -1) return '🥇'
      if (name.indexOf('二') !== -1) return '🥈'
      if (name.indexOf('三') !== -1) return '🥉'
      return '🎖'
    },
    // 名额文本：集体奖单位为"家"，个人奖为"名"；rankEnd 为 0 表示"若干"
    quotaOf(a) {
      const unit = a.awardType === 2 ? '家' : '名'
      if (!a.rankEnd || a.rankEnd === 0) return '若干' + unit
      return (a.rankEnd - a.rankStart + 1) + unit
    },
    // 加载奖励档位（后台修改即时生效）
    async loadAwards() {
      try {
        const list = await request('/award/list')
        const rows = Array.isArray(list) ? list : []
        this.personalAwards = rows.filter((a) => a.awardType !== 2)
        this.collectiveAwards = rows.filter((a) => a.awardType === 2)
      } catch (e) {
        this.personalAwards = []
        this.collectiveAwards = []
      }
    },
    async loadMyAward() {
      try {
        const data = await request('/award/mine')
        if (data && data.hasAward) {
          this.hasAward = true
          this.awardLevel = data.awardLevel
          this.realName = data.realName
          this.deptName = data.deptName
          this.activityName = data.activityName || '健步走线上活动'
          this.issueDate = data.issueDate
          this.addr = { ...this.addr, ...(data.address || {}) }
        }
      } catch (e) { /* 未中奖或后端未就绪 */ }
    },
    async saveAddr() {
      if (!this.addr.receiver) { uni.showToast({ title: '请输入收货人', icon: 'none' }); return }
      if (!/^1\d{10}$/.test(this.addr.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return }
      if (!this.addr.address) { uni.showToast({ title: '请输入详细地址', icon: 'none' }); return }
      this.saving = true
      try {
        await request('/award/address', { method: 'POST', data: this.addr })
        uni.showToast({ title: '地址已保存', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '保存失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style lang="scss">
.page { padding-bottom: 40rpx; }

/* 我的获奖 */
.win-card {
  display: flex; flex-direction: column; align-items: center; padding: 40rpx 0 30rpx;
  background: linear-gradient(120deg, #E8392C, #F07830); color: #fff;
}
.win-badge { font-size: 40rpx; font-weight: bold; background: rgba(255,255,255,.25); padding: 12rpx 40rpx; border-radius: 999rpx; }
.win-tip { margin-top: 16rpx; font-size: 28rpx; }

/* 电子证书 */
.certificate {
  margin: 24rpx; background: #fff; border-radius: 12rpx; overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,.08); position: relative;
}
.cert-deco { height: 24rpx; background: linear-gradient(90deg, #E8392C, #F5A623, #E8392C); }
.cert-deco-bottom { position: absolute; bottom: 0; left: 0; right: 0; }
.cert-content { padding: 50rpx 40rpx; display: flex; flex-direction: column; align-items: center; border: 2rpx solid #F0E0C0; margin: 20rpx; border-radius: 8rpx; }
.cert-title { font-size: 44rpx; font-weight: bold; color: #B8860B; letter-spacing: 8rpx; }
.cert-line { width: 200rpx; height: 2rpx; background: #B8860B; margin: 20rpx 0; }
.cert-name { font-size: 36rpx; font-weight: bold; color: #333; }
.cert-body { font-size: 26rpx; color: #666; margin-top: 14rpx; }
.cert-level { font-size: 34rpx; font-weight: bold; color: #E8392C; margin: 14rpx 0; }
.cert-unit { font-size: 26rpx; color: #666; margin-top: 20rpx; }
.cert-date { font-size: 24rpx; color: #999; margin-top: 8rpx; }

.card { background: #fff; border-radius: 20rpx; margin: 24rpx; padding: 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,.04); }
.section-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; }
.form-item { display: flex; align-items: center; margin-bottom: 24rpx; }
.form-label { width: 160rpx; font-size: 28rpx; color: #333; }
.form-input { flex: 1; height: 76rpx; background: #F7F8FA; border-radius: 12rpx; padding: 0 20rpx; font-size: 28rpx; }
.save-btn { background: #E8392C; color: #fff; border-radius: 999rpx; font-size: 30rpx; }

/* 静态奖项 */
.section-head { font-size: 30rpx; font-weight: bold; color: #333; margin: 30rpx 24rpx 0; padding-left: 16rpx; border-left: 8rpx solid #E8392C; }
.award-row { display: flex; }
.award-medal { font-size: 64rpx; margin-right: 24rpx; }
.award-info { flex: 1; display: flex; flex-direction: column; }
.award-title { font-size: 32rpx; font-weight: bold; }
.award-rank { font-size: 24rpx; color: #E8392C; margin-top: 8rpx; }
.award-desc { font-size: 26rpx; color: #666; margin-top: 8rpx; }
.note { background: #FFF6F5; }
.note-text { font-size: 24rpx; color: #A8645E; line-height: 1.8; }
.empty-tip { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>
