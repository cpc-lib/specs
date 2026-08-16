# Search / Recommendation Experiment SPEC

Experiment:
experimentCode
trafficAllocation
subjectType USER/SESSION/DEVICE
start/end
status.

Variant:
CONTROL / TREATMENT...
config/model/rank policy refs.

Assignment:
experiment + subject -> one stable variant during configured assignment window.

Exposure event is emitted only when variant output is actually shown.
Do not infer exposure from assignment alone.
