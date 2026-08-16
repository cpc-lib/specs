# Frontend Repository Layout

```text
marketplace-platform-web/  # React 平台后台
marketplace-seller-web/    # React 商家后台
marketplace-buyer-app/     # UniApp 买家端
```

三个应用与后端模块处于同一 Monorepo，但不加入 Maven `<modules>`。

## Platform Web
面向平台运营、审核、财务、风控、治理、客服。

## Seller Web
面向商户与店铺团队，所有 merchant/shop scope 必须由后端鉴权上下文校验。

## Buyer App
面向买家，覆盖浏览、购物车、下单支付、履约、售后、评价和客服。
