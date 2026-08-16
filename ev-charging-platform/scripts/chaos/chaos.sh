#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE=(docker compose -f "$ROOT/deploy/docker/docker-compose.yml")
SCENARIO="${1:-}"
DURATION="${2:-30}"

require_docker(){
  command -v docker >/dev/null 2>&1 || { echo "docker is required"; exit 2; }
  "${COMPOSE[@]}" ps >/dev/null
}
restore(){
  case "$SCENARIO" in
    mysql-outage) "${COMPOSE[@]}" start mysql >/dev/null || true ;;
    redis-outage) "${COMPOSE[@]}" start redis >/dev/null || true ;;
    kafka-outage) "${COMPOSE[@]}" start kafka >/dev/null || true ;;
    rabbitmq-outage) "${COMPOSE[@]}" start rabbitmq >/dev/null || true ;;
    nacos-outage) "${COMPOSE[@]}" start nacos >/dev/null || true ;;
  esac
}
trap restore EXIT INT TERM
require_docker

case "$SCENARIO" in
  mysql-outage) service=mysql ;;
  redis-outage) service=redis ;;
  kafka-outage) service=kafka ;;
  rabbitmq-outage) service=rabbitmq ;;
  nacos-outage) service=nacos ;;
  *)
    echo "usage: $0 {mysql-outage|redis-outage|kafka-outage|rabbitmq-outage|nacos-outage} [seconds]"
    exit 2
    ;;
esac

echo "CHAOS_START scenario=$SCENARIO duration=${DURATION}s"
"${COMPOSE[@]}" stop "$service"
sleep "$DURATION"
"${COMPOSE[@]}" start "$service"
trap - EXIT INT TERM
echo "CHAOS_RECOVERED scenario=$SCENARIO"
