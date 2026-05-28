import http from 'k6/http';
import { check } from 'k6';

const getUrl = __ENV.SERVICE_API_BASE_URL + "/gateway/history"
const postUrl = __ENV.SERVICE_API_BASE_URL + "/transfers/transfer"

export default function () {
    const userId = 'user-' + Math.floor(Math.random() * 10000);
    if (Math.random() < 0.75) {
        const res = http.get(getUrl, { headers: { 'X-User-Id': userId } });
        check(res, { 'status 200': (r) => r.status === 200 });
    } else {
        const res = http.post(postUrl, JSON.stringify({ userId, amount: 100, toAccountId: 'ACC-999' }), {
            headers: { 'Content-Type': 'application/json', 'X-User-Id': userId },
        });
        check(res, { 'status 200': (r) => r.status === 200 });
    }
}

export const options = {
    discardResponseBodies: false,
    vus: parseInt(__ENV.VUS),
    duration: __ENV.DURATION_IN_SECONDS + 's',
};
