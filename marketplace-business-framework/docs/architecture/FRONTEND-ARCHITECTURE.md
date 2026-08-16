# Marketplace Frontend Architecture

Marketplace V3.0 的正式前端由三套应用组成：

```text
marketplace-platform-web   # 平台运营/管理后台，React
marketplace-seller-web     # 商家后台，React
marketplace-buyer-app      # 买家端，UniApp
```

## 技术栈

### React
- React
- TypeScript
- Vite
- Ant Design
- Zustand
- Axios
- TailwindCSS

### UniApp
- Vue 3
- TypeScript
- UniApp
- Pinia
- 微信小程序 / H5

## 原则

- 前端不定义业务最终事实。
- Price、Inventory、Promotion、Payment、Refund、Settlement 等最终状态必须以后端返回为准。
- Seller 的 merchantId / shopId 不能由客户端作为授权依据；后端必须从已认证 Principal / Membership / Scope 解析。
- Platform 管理端不能绕过后端权限和治理 Command。
- Buyer 端支付 SDK success 不等于 Payment SUCCESS。
- 所有页面按 Marketplace V3.0 OpenAPI Contract 对接。
