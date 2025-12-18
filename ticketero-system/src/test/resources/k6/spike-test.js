import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
export let errorRate = new Rate('errors');
export let responseTime = new Trend('response_time');

// Spike test configuration
export let options = {
  stages: [
    { duration: '10s', target: 100 }, // Spike to 100 users
    { duration: '30s', target: 100 }, // Stay at 100 users
    { duration: '10s', target: 0 },   // Drop to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // Allow higher latency during spike
    http_req_failed: ['rate<0.10'],    // Allow higher error rate during spike
    errors: ['rate<0.10'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function() {
  const nationalId = `${Math.floor(Math.random() * 90000000) + 10000000}`;
  
  const payload = JSON.stringify({
    nationalId: nationalId,
    description: `Spike test ticket ${nationalId}`,
    queueType: 'GENERAL'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  let response = http.post(`${BASE_URL}/api/tickets`, payload, params);
  
  let success = check(response, {
    'status is 201 or 429': (r) => r.status === 201 || r.status === 429, // Allow rate limiting
    'response time < 3000ms': (r) => r.timings.duration < 3000,
  });

  errorRate.add(!success);
  responseTime.add(response.timings.duration);
}