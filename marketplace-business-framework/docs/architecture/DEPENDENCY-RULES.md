# Dependency rules
Allowed:
`business-module -> marketplace-framework/*`

Forbidden in v1.0:
`marketplace-trade -> marketplace-payment`
`marketplace-payment -> marketplace-settlement`
`marketplace-product -> marketplace-inventory`
(or any direct business-module dependency)

When a use case needs collaboration, add an application port/API/event in that TASK rather than Maven-coupling the domains.
