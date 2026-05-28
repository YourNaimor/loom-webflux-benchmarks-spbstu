import http from 'k6/http';

const url = __ENV.SERVICE_API_BASE_URL + "/gateway/history"

export default function () {
    http.get(url, { headers: { 'X-User-Id': 'user-' + Math.floor(Math.random() * 10000) } });
}

export const options = {
    discardResponseBodies: true,
    vus: __ENV.VUS,
    duration: __ENV.DURATION_IN_SECONDS + 's',
};
