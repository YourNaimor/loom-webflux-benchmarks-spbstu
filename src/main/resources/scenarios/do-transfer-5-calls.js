/**
 * Тест 2 (диплом): do-transfer — глубокая цепочка вызовов (5 downstream).
 * 5 000 VU, 1 000 RPS нагрузки на transfers-сервис.
 * Каждый запрос порождает 5 внутренних вызовов: clients, products, auth, payment, history.
 * Метрика: p50/p99 латентность, CPU, RPS, % ошибок.
 */
import http from 'k6/http';
import { check } from 'k6';

const approach = __ENV.APPROACH || 'loom-tomcat';
const baseUrl = __ENV.SERVICE_API_BASE_URL;
const url = `${baseUrl}/${approach}/transfers/transfer`;

export const options = {
    discardResponseBodies: true,
    scenarios: {
        deep_call_stack: {
            executor: 'constant-arrival-rate',
            rate: 1000,
            timeUnit: '1s',
            preAllocatedVUs: 5000,
            maxVUs: 5000,
            duration: '3m',
        },
    },
};

export default function () {
    const userId = `user-${Math.floor(Math.random() * 1000)}`;
    const payload = JSON.stringify({
        userId,
        amount: Math.floor(Math.random() * 10000) + 100,
        toAccountId: `ACC-${Math.floor(Math.random() * 100)}`,
    });
    const res = http.post(url, payload, {
        headers: {
            'Content-Type': 'application/json',
            'X-User-Id': userId,
        },
    });
    check(res, { 'transfer 200': (r) => r.status === 200 });
}
