# Category & Attribute Schema SPEC

## Category
`PlatformCategory`
- categoryId
- parentId
- path
- level
- categoryCode
- categoryName
- saleStatus
- productPolicyCode
- attributeSchemaVersion

## AttributeDefinition
Fields:
- attributeId
- categoryId
- code
- name
- attributeType: KEY / SALE / NORMAL / SEARCH / COMPLIANCE
- dataType: STRING / INTEGER / DECIMAL / BOOLEAN / ENUM / MULTI_ENUM / DATE
- required
- searchable
- filterable
- skuDimension
- unitCode
- validationJson
- optionSource
- displayOrder
- versionNo
- effectiveFrom/effectiveTo
- status

## AttributeOption
Options are versioned and never hard-delete if referenced by published products.

## Invariants
- SALE attributes selected as SKU dimensions are stable for a published SPU version.
- normalized attribute code/value is used for uniqueness and indexing.
- category changes requiring different mandatory compliance attributes force revalidation.
