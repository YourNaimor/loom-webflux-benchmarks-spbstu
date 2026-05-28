/**
 * Сценарий 1: GET history — запрос истории операций через gateway.
 * Глубина цепочки вызовов: 1 (auth → history).
 * Используется в тестах: soaktest-10k, sharp-spikes-20k, 40k-vus.
 */
import http from 'k6/http';
import { check } from 'k6';

const approach = __ENV.APPROACH || 'loom-tomcat';
const baseUrl = __ENV.SERVICE_API_BASE_URL;
const url = `${baseUrl}/${approach}/gateway/history`;

export default function () {
    const res = http.get(url, {
        headers: { 'X-User-Id': `user-${Math.floor(Math.random() * 1000)}` },
    });
    check(res, { 'status 200': (r) => r.status === 200 });
}

export const options = {
    discardResponseBodies: true,
    scenarios: {
        default: {
            executor: 'constant-vus',
            vus: parseInt(__ENV.VUS || '100'),
            duration: (__ENV.DURATION_IN_SECONDS || '180') + 's',
        },
    },
};
