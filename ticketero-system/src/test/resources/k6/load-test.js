import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
export let errorRate = new Rate('errors');
export let responseTime = new Trend('response_time');

// Test configuration
export let options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up
    { duration: '1m', target: 50 },   // Stay at 50 users
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests under 2s
    http_req_failed: ['rate<0.05'],    // Error rate under 5%
    errors: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function() {
  // Generate unique nationalId for each request
  const nationalId = `${Math.floor(Math.random() * 90000000) + 10000000}`;
  
  const payload = JSON.stringify({
    nationalId: nationalId,
    description: `Load test ticket ${nationalId}`,
    queueType: 'GENERAL'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Create ticket
  let response = http.post(`${BASE_URL}/api/tickets`, payload, params);
  
  // Check response
  let success = check(response, {
    'status is 201': (r) => r.status === 201,
    'response time < 2000ms': (r) => r.timings.duration < 2000,
    'has reference code': (r) => r.json('referenceCode') !== null,
  });

  errorRate.add(!success);
  responseTime.add(response.timings.duration);

  sleep(1);
}