# Favorite / Shop Follow SPEC

Facts:
FavoriteOffer(userId, offerId)
ShopFollow(userId, shopId)

Idempotent unique relation.
Commands:
AddFavorite / RemoveFavorite
FollowShop / UnfollowShop.

High volume:
shard primarily by userId.
Secondary shop/favorite counts are derived counters/read models.
