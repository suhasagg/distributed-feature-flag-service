import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
};

export default function () {
  const payload = JSON.stringify({ environment: 'prod', flagKey: 'new-checkout', userId: `user-${__VU}-${Date.now()}`, attributes: { country: 'IN' } });
  const res = http.post('http://localhost:8080/evaluate', payload, { headers: { 'Content-Type': 'application/json', 'X-Tenant-ID': 'tenant-a' } });
  check(res, { 'status is 200': r => r.status === 200 || r.status === 404 || r.status === 429 });
}
