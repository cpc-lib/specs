# Marketplace Buyer App

UniApp 买家端。

## 核心业务链

```text
Home/Search
→ Product/SKU
→ Cart
→ Checkout
→ Trade
→ Payment
→ Fulfillment
→ Receive
→ AfterSale
→ Review
```

## 重要红线

- 客户端价格不是权威价格。
- Checkout / SubmitTrade 必须服务端重新校验价格、优惠、库存、Saleability。
- 支付 SDK success 不等于 Payment SUCCESS。
- Buyer App 不能直接决定 Refund/AfterSale 最终状态。

完整业务事实见：

`../spec/marketplace-v3.0/`
