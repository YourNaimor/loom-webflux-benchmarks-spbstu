/**
 * Тест 3 (диплом): sharp-spikes-20k — серия резких всплесков нагрузки до 20k RPS.
 * Паттерн: 0 → 20k VU (мгновенно) → 0, повторяется 5 раз.
 * Глубина цепочки: 1 (gateway → history). GET + POST mix.
 * Метрика: RPS, p99 латентность, % ошибок при каждом всплеске.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

const approach = __ENV.APPROACH || 'loom-tomcat';
const baseUrl = __ENV.SERVICE_API_BASE_URL;
const getUrl = `${baseUrl}/${approach}/gateway/history`;
const postUrl = `${baseUrl}/${approach}/transfers/transfer`;

export const options = {
    discardResponseBodies: true,
    scenarios: {
        spikes: {
            executor: 'ramping-vus',
            stages: [
                { duration: '10s', target: 20000 },  // резкий подъём
                { duration: '20s', target: 20000 },  // плато
                { duration: '10s', target: 0 },      // резкий спад
                { duration: '10s', target: 0 },      // пауза
                { duration: '10s', target: 20000 },
                { duration: '20s', target: 20000 },
                { duration: '10s', target: 0 },
                { duration: '10s', target: 0 },
                { duration: '10s', target: 20000 },
                { duration: '20s', target: 20000 },
                { duration: '10s', target: 0 },
            ],
        },
    },
};

export default function () {
    const userId = `user-${Math.floor(Math.random() * 10000)}`;
    if (Math.random() < 0.75) {
        const res = http.get(getUrl, { headers: { 'X-User-Id': userId } });
        check(res, { 'history 200': (r) => r.status === 200 });
    } else {
        const payload = JSON.stringify({ userId, amount: 50, toAccountId: 'ACC-001' });
        const res = http.post(postUrl, payload, {
            headers: { 'Content-Type': 'application/json', 'X-User-Id': userId },
        });
        check(res, { 'transfer 200': (r) => r.status === 200 });
    }
}
