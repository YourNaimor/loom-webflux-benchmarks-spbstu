import http from 'k6/http';
import { check } from 'k6';

const url = __ENV.SERVICE_API_BASE_URL + "/transfers/transfer"

export default function () {
    const userId = 'user-' + Math.floor(Math.random() * 1000);
    const res = http.post(url, JSON.stringify({
        userId,
        amount: Math.floor(Math.random() * 10000) + 100,
        toAccountId: 'ACC-' + Math.floor(Math.random() * 100),
    }), {
        headers: { 'Content-Type': 'application/json', 'X-User-Id': userId },
    });
    check(res, { 'status 200': (r) => r.status === 200 });
}

export const options = {
    discardResponseBodies: false,
    scenarios: {
        deep_call: {
            executor: 'constant-arrival-rate',
            rate: parseInt(__ENV.RPS || '1000'),
            timeUnit: '1s',
            preAllocatedVUs: parseInt(__ENV.VUS || '5000'),
            maxVUs: parseInt(__ENV.VUS || '5000'),
            duration: __ENV.DURATION_IN_SECONDS + 's',
        },
    },
};
