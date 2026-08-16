#!/usr/bin/env python3
from dataclasses import dataclass

@dataclass(frozen=True)
class Capacity:
    online_devices:int=10_000
    concurrent_charging:int=2_000
    telemetry_per_second:int=5_000
    avg_telemetry_bytes:int=700
    websocket_clients:int=2_000
    headroom:float=1.5

c=Capacity()
wire_mbps=c.telemetry_per_second*c.avg_telemetry_bytes*8/1_000_000*c.headroom
daily_events=c.telemetry_per_second*86400
daily_gb=daily_events*c.avg_telemetry_bytes/1_000_000_000

print(f"online_devices={c.online_devices}")
print(f"concurrent_charging={c.concurrent_charging}")
print(f"telemetry_per_second={c.telemetry_per_second}")
print(f"telemetry_wire_mbps_with_headroom={wire_mbps:.2f}")
print(f"telemetry_events_per_day={daily_events}")
print(f"raw_telemetry_gb_per_day_before_compression={daily_gb:.2f}")
assert wire_mbps < 100
assert daily_events == 432_000_000
print("CAPACITY_MODEL=PASS")
