# Work Order Attachments

SPEC 7.9 provides a development local-filesystem adapter behind `WorkOrderAttachmentStorage`.

Rules:

- max file size: 10 MB
- safe file-name normalization
- generated object key
- SHA-256 persisted
- tenant/work-order ownership checked
- technician upload additionally requires assignment to the work order
- if metadata persistence fails, the newly stored object is cleaned up

Production should replace the local adapter with S3-compatible or cloud object storage without changing the application service contract.
