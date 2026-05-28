#!/bin/bash
# Запуск финтех-бенчмарков из дипломной работы.
# Запускает все 4 сценария для обоих стеков: loom-tomcat и webflux-netty.
#
# Использование:
#   ./run-fintech-benchmarks.sh [BASE_URL]
#   BASE_URL по умолчанию: http://localhost:8080
#
# Требования: Java 21, k6, запущенный сервис (./gradlew bootRun)

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
SCENARIOS_DIR="$(dirname "$0")/../resources/scenarios"
RESULTS_DIR="results/fintech"
mkdir -p "$RESULTS_DIR"

run_scenario() {
    local scenario="$1"
    local script="$2"
    local approach="$3"

    echo "=== Запуск: $scenario (approach=$approach) ==="
    k6 run \
        --out csv="$RESULTS_DIR/${scenario}-${approach}.csv" \
        -e SERVICE_API_BASE_URL="$BASE_URL" \
        -e APPROACH="$approach" \
        "$SCENARIOS_DIR/$script"
    echo "=== Завершён: $scenario ($approach) ==="
}

# Тест 4: soaktest — плавный подъём до 10k VU (20 мин)
run_scenario "soaktest-10k" "soaktest-10k.js" "loom-tomcat"
run_scenario "soaktest-10k" "soaktest-10k.js" "webflux-netty"

# Тест 3: sharp-spikes — резкие всплески до 20k RPS
run_scenario "sharp-spikes-20k" "sharp-spikes-20k.js" "loom-tomcat"
run_scenario "sharp-spikes-20k" "sharp-spikes-20k.js" "webflux-netty"

# Тест 2: do-transfer — глубокая цепочка 5 вызовов, 5k VU / 1k RPS
run_scenario "do-transfer-5-calls" "do-transfer-5-calls.js" "loom-tomcat"
run_scenario "do-transfer-5-calls" "do-transfer-5-calls.js" "webflux-netty"

# Тест 1: 40k-vus — устойчивая нагрузка 40k VU
run_scenario "40k-vus-get-history" "get-history-40k-vus.js" "loom-tomcat"
run_scenario "40k-vus-get-history" "get-history-40k-vus.js" "webflux-netty"

echo ""
echo "Все сценарии завершены. Результаты: $RESULTS_DIR"
