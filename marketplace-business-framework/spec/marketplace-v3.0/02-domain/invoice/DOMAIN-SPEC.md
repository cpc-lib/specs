# Invoice Domain SPEC
Scopes:
- buyer invoice
- platform self invoice
- merchant service fee invoice

Use InvoiceIssuerStrategy.
Supports issue / query / red flush / reissue / email delivery.
Issued invoice is immutable fact.
Red flush creates new relation, not edit original.
