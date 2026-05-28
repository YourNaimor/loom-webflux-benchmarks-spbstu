/**
 * Тест 4 (диплом): soaktest-10k — продолжительная нагрузка на gateway.
 * Плавный подъём нагрузки до 10k VU за 17 минут, затем снижение 3 минуты.
 * Позволяет выявить поведение стека при постепенном приближении к точке насыщения.
 * Метрика: RPS, p99 латентность (деградация при ~8k-9k VU у loom-tomcat), CPU, GC.
 */
import http from 'k6/http';
import { check } from 'k6';

const approach = __ENV.APPROACH || 'loom-tomcat';
const baseUrl = __ENV.SERVICE_API_BASE_URL;
const url = `${baseUrl}/${approach}/gateway/history`;

export const options = {
    discardResponseBodies: true,
    scenarios: {
        soaktest: {
            executor: 'ramping-vus',
            stages: [
                { duration: '17m', target: 10000 },  // плавный подъём до 10k VU
                { duration: '3m',  target: 0 },      // постепенное снижение
            ],
        },
    },
};

export default function () {
    const userId = `user-${Math.floor(Math.random() * 5000)}`;
    const res = http.get(url, { headers: { 'X-User-Id': userId } });
    check(res, { 'history 200': (r) => r.status === 200 });
}
