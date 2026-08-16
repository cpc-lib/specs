# Merchant / Shop Security Isolation

Access decision:
Authenticated User
AND MerchantMembership
AND MerchantPermission
AND ShopScope
AND DataScope
AND resource belongs to merchant/shop scope.

Seller endpoints never trust request merchantId as authority.
Platform support cross-merchant access uses temporary SupportSession + audit.
