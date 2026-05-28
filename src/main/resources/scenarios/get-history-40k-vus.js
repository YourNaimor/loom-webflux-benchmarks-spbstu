/**
 * Тест 1 (диплом): 40k-vus — устойчивая нагрузка 40 000 VU на gateway (get-history).
 * 40 000 RPS в течение 3 минут. GET 75% / POST 25%.
 * Метрика: RPS, p50/p90/p99 латентность, % ошибок.
 */
import http from 'k6/http';
import { check } from 'k6';

const approach = __ENV.APPROACH || 'loom-tomcat';
const baseUrl = __ENV.SERVICE_API_BASE_URL;
const getUrl = `${baseUrl}/${approach}/gateway/history`;
const postUrl = `${baseUrl}/${approach}/transfers/transfer`;

export const options = {
    discardResponseBodies: true,
    scenarios: {
        steady_load: {
            executor: 'constant-vus',
            vus: 40000,
            duration: '3m',
        },
    },
};

export default function () {
    const userId = `user-${Math.floor(Math.random() * 10000)}`;
    if (Math.random() < 0.75) {
        const res = http.get(getUrl, {
            headers: { 'X-User-Id': userId },
        });
        check(res, { 'history 200': (r) => r.status === 200 });
    } else {
        const payload = JSON.stringify({ userId, amount: 100, toAccountId: 'ACC-999' });
        const res = http.post(postUrl, payload, {
            headers: { 'Content-Type': 'application/json', 'X-User-Id': userId },
        });
        check(res, { 'transfer 200': (r) => r.status === 200 });
    }
}
