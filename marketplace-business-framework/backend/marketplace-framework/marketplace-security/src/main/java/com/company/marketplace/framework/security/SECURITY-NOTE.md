# Security boundary
`PrincipalResolver` must validate a signed JWT/session or trusted service credential.
Never implement it by trusting client `merchantId`, `shopId` or `X-Internal-*` headers.
Seller merchant/shop scope comes from authenticated membership, not request body.
