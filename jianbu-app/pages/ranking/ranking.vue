<template>
  <view class="page">
    <!-- 榜单切换：当日 / 总 / 积分 -->
    <view class="toggle-row">
      <view
        class="toggle-item"
        :class="{ active: board === 'today' }"
        @click="switchBoard('today')"
      >当日排名</view>
      <view
        class="toggle-item"
        :class="{ active: board === 'total' }"
        @click="switchBoard('total')"
      >总排名</view>
      <view
        class="toggle-item"
        :class="{ active: board === 'points' }"
        @click="switchBoard('points')"
      >积分排名</view>
    </view>

    <!-- 更新说明 -->
    <view class="update-tip">
      <text v-if="board === 'today'">实时更新</text>
      <text v-else>每日凌晨自动更新，非实时</text>
    </view>

    <!-- 排行列表 -->
    <view class="card" v-for="(item, idx) in list" :key="idx">
      <view class="rank-row">
        <text class="rank-no" :class="{ top3: idx < 3 }">{{ idx + 1 }}</text>
        <view class="rank-info">
          <text class="rank-name">{{ item.name }}</text>
          <text class="rank-dept">{{ item.dept }}</text>
        </view>
        <text class="rank-value">{{ item.value }} {{ board === 'points' ? '分' : '步' }}</text>
      </view>
    </view>
    <view class="empty-tip" v-if="!list.length">暂无数据</view>

    <!-- 提示语 -->
    <view class="note-card">
      <view class="note-title">说明</view>
      <text class="note-text">{{ note }}</text>
    </view>
  </view>
</template>

<script>
import request from '@/utils/request'

export default {
  data() {
    return {
      board: 'today',      // today | total | points
      list: [],
      note: '当日步数排名实时更新；总步数排名与积分排名每天凌晨自动更新，非实时。微信步数需打开小程序才能同步，昨日步数需次日进入小程序才能同步进来，且要等下一次凌晨更新或管理员后台刷新后才会重新计算。缓冲期步数不计入榜单，活动结束后统一锁榜结算。'
    }
  },
  onShow() {
    this.load()
  },
  methods: {
    switchBoard(b) {
      this.board = b
      this.load()
    },
    load() {
      request('/ranking/list', { data: { board: this.board } }).then((data) => {
        this.list = Array.isArray(data) ? data : []
      }).catch(() => {
        this.list = []
      })
    }
  }
}
</script>

<style lang="scss">
.page { padding-bottom: 40rpx; }
.toggle-row { display: flex; margin: 24rpx 24rpx 0; background: #fff; border-radius: 999rpx; padding: 6rpx; }
.toggle-item { flex: 1; text-align: center; padding: 16rpx 0; font-size: 28rpx; color: #666; border-radius: 999rpx; }
.toggle-item.active { background: #E8392C; color: #fff; }
.update-tip { margin: 16rpx 24rpx 0; font-size: 22rpx; color: #999; }
.card { background: #fff; margin: 16rpx 24rpx; border-radius: 16rpx; padding: 20rpx 30rpx; }
.rank-row { display: flex; align-items: center; }
.rank-no { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; background: #F0F0F0; border-radius: 50%; font-weight: bold; color: #999; }
.rank-no.top3 { background: #E8392C; color: #fff; }
.rank-info { flex: 1; margin-left: 20rpx; display: flex; flex-direction: column; }
.rank-name { font-size: 30rpx; }
.rank-dept { font-size: 22rpx; color: #999; margin-top: 4rpx; }
.rank-value { font-size: 28rpx; color: #E8392C; font-weight: bold; }
.empty-tip { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.note-card { background: #FFF6F5; border-radius: 20rpx; margin: 24rpx; padding: 30rpx; }
.note-title { font-size: 28rpx; font-weight: bold; color: #E8392C; margin-bottom: 12rpx; }
.note-text { font-size: 24rpx; color: #A8645E; line-height: 1.8; }
</style>
