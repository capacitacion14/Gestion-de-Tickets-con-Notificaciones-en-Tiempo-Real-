import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
export let errorRate = new Rate('errors');
export let responseTime = new Trend('response_time');

// Soak test configuration - 30 minutes at steady load
export let options = {
  stages: [
    { duration: '2m', target: 30 },   // Ramp up to 30 users
    { duration: '26m', target: 30 },  // Stay at 30 users for 26 minutes
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // Maintain performance over time
    http_req_failed: ['rate<0.05'],    // Low error rate
    errors: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function() {
  const nationalId = `${Math.floor(Math.random() * 90000000) + 10000000}`;
  
  const payload = JSON.stringify({
    nationalId: nationalId,
    description: `Soak test ticket ${nationalId} - ${new Date().toISOString()}`,
    queueType: Math.random() > 0.5 ? 'GENERAL' : 'PRIORITY'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  let response = http.post(`${BASE_URL}/api/tickets`, payload, params);
  
  let success = check(response, {
    'status is 201': (r) => r.status === 201,
    'response time < 2000ms': (r) => r.timings.duration < 2000,
    'has reference code': (r) => r.json('referenceCode') !== null,
    'memory stable': (r) => r.headers['X-Memory-Usage'] ? 
      parseInt(r.headers['X-Memory-Usage']) < 1000000000 : true, // < 1GB
  });

  errorRate.add(!success);
  responseTime.add(response.timings.duration);

  // Vary the sleep time to simulate real user behavior
  sleep(Math.random() * 3 + 1); // 1-4 seconds
}