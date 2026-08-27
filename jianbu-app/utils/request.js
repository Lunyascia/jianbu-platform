// 健步走小程序 - 请求封装
// 开发期连本地后端；微信开发者工具需勾选"不校验合法域名"
const BASE_URL = 'http://localhost:8080/walking'

const TOKEN_KEY = 'jbw_token'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function clearToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

/**
 * 判断是否为登录失效类错误
 */
function isLoginError(body) {
  if (!body) return false
  if (body.code === 401) return true
  const msg = body.msg || ''
  return msg.includes('未登录') || msg.includes('登录已过期') || msg.includes('会话已过期') || msg.includes('会员不存在')
}

/**
 * 统一请求
 * @param {string} url 接口路径（相对 /walking 前缀）
 * @param {object} options { method, data, auth(默认true需要登录) }
 */
export function request(url, options = {}) {
  const { method = 'GET', data = {}, auth = true } = options
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(auth ? { Authorization: getToken() } : {})
      },
      success: (res) => {
        const body = res.data
        if (body && body.code === 200) {
          resolve(body.data)
        } else if (isLoginError(body)) {
          // 登录失效/会话过期/会员不存在 → 清 token 并跳登录
          clearToken()
          uni.reLaunch({ url: '/pages/login/login' })
          reject(body)
        } else {
          uni.showToast({ title: (body && body.msg) || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请确认后端已启动', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 便捷方法挂在 request 上，默认导出就是 request 函数
request.get = (url, data, opts) => request(url, { ...opts, method: 'GET', data })
request.post = (url, data, opts) => request(url, { ...opts, method: 'POST', data })
request.put = (url, data, opts) => request(url, { ...opts, method: 'PUT', data })

export default request
