import http from 'k6/http';
import { check } from 'k6';

const url = __ENV.SERVICE_API_BASE_URL + "/gateway/history"

export default function () {
    const res = http.get(url, { headers: { 'X-User-Id': 'user-' + Math.floor(Math.random() * 5000) } });
    check(res, { 'status 200': (r) => r.status === 200 });
}

export const options = {
    discardResponseBodies: false,
    scenarios: {
        soaktest: {
            executor: 'ramping-vus',
            stages: [
                { duration: '17m', target: parseInt(__ENV.VUS || '10000') },
                { duration: '3m',  target: 0 },
            ],
        },
    },
};
