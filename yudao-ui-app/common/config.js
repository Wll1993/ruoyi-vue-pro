module.exports = {
  //后端接口地址
  baseUrl: 'http://101.200.62.14:48080/app-api',

  // baseUrl: 'http://api-dashboard.yudao.iocoder.cn/app-api',
  // 超时
  timeout: 30000,
  // 禁用 Cookie 等信息
  withCredentials: false,
  header: {
    //租户ID
    'tenant-id': 1
  }
}
