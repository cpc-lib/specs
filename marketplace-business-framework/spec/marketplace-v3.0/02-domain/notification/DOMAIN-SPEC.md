# Notification Domain SPEC
Channels:
IN_APP / SMS / EMAIL / PUSH / WECHAT
Business services emit events; Notification selects template/channel/retry.
Do not directly embed provider SDK in Trade/Payment/Settlement.
