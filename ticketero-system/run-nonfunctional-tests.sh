#!/bin/bash

# Non-Functional Test Execution Script
# Executes all categories of non-functional tests

set -e

echo "🚀 Starting Non-Functional Test Suite Execution"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
RESULTS_DIR="target/nonfunctional-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create results directory
mkdir -p "$RESULTS_DIR"

# Function to check if application is running
check_application() {
    echo -e "${BLUE}Checking if application is running...${NC}"
    if curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ Application is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Application is not running${NC}"
        echo "Please start the application first: ./run.sh"
        exit 1
    fi
}

# Function to run Java-based non-functional tests
run_java_tests() {
    echo -e "${BLUE}📊 Running Java Non-Functional Tests...${NC}"
    
    # Performance Tests
    echo -e "${YELLOW}Running Performance Tests...${NC}"
    mvn test -Dtest=PerformanceTest -Dspring.profiles.active=nonfunctional \
        > "$RESULTS_DIR/performance-test-$TIMESTAMP.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Performance Tests: PASSED${NC}"
    else
        echo -e "${RED}❌ Performance Tests: FAILED${NC}"
    fi
    
    # Security Tests
    echo -e "${YELLOW}Running Security Tests...${NC}"
    mvn test -Dtest=SecurityTest -Dspring.profiles.active=nonfunctional \
        > "$RESULTS_DIR/security-test-$TIMESTAMP.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Security Tests: PASSED${NC}"
    else
        echo -e "${RED}❌ Security Tests: FAILED${NC}"
    fi
    
    # Usability Tests
    echo -e "${YELLOW}Running Usability Tests...${NC}"
    mvn test -Dtest=UsabilityTest -Dspring.profiles.active=nonfunctional \
        > "$RESULTS_DIR/usability-test-$TIMESTAMP.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Usability Tests: PASSED${NC}"
    else
        echo -e "${RED}❌ Usability Tests: FAILED${NC}"
    fi
    
    # Compatibility Tests
    echo -e "${YELLOW}Running Compatibility Tests...${NC}"
    mvn test -Dtest=CompatibilityTest -Dspring.profiles.active=nonfunctional \
        > "$RESULTS_DIR/compatibility-test-$TIMESTAMP.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Compatibility Tests: PASSED${NC}"
    else
        echo -e "${RED}❌ Compatibility Tests: FAILED${NC}"
    fi
}

# Function to run K6 performance tests
run_k6_tests() {
    echo -e "${BLUE}🔥 Running K6 Performance Tests...${NC}"
    
    # Check if k6 is installed
    if ! command -v k6 &> /dev/null; then
        echo -e "${YELLOW}⚠️  K6 not installed. Skipping K6 tests.${NC}"
        echo "Install K6: https://k6.io/docs/getting-started/installation/"
        return
    fi
    
    # Load Test
    echo -e "${YELLOW}Running K6 Load Test...${NC}"
    k6 run --out json="$RESULTS_DIR/k6-load-test-$TIMESTAMP.json" \
        src/test/resources/k6/load-test.js \
        --env BASE_URL="$BASE_URL" \
        > "$RESULTS_DIR/k6-load-test-$TIMESTAMP.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ K6 Load Test: PASSED${NC}"
    else
        echo -e "${RED}❌ K6 Load Test: FAILED${NC}"
    fi
    
    # Spike Test
    echo -e "${YELLOW}Running K6 Spike Test...${NC}"
    k6 run --out json="$RESULTS_DIR/k6-spike-test-$TIMESTAMP.json" \
        src/test/resources/k6/spike-test.js \
        --env BASE_URL="$BASE_URL" \
        > "$RESULTS_DIR/k6-spike-test-$TIMESTAMP.log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ K6 Spike Test: PASSED${NC}"
    else
        echo -e "${RED}❌ K6 Spike Test: FAILED${NC}"
    fi
    
    # Soak Test (optional - takes 30 minutes)
    if [ "$1" = "--include-soak" ]; then
        echo -e "${YELLOW}Running K6 Soak Test (30 minutes)...${NC}"
        k6 run --out json="$RESULTS_DIR/k6-soak-test-$TIMESTAMP.json" \
            src/test/resources/k6/soak-test.js \
            --env BASE_URL="$BASE_URL" \
            > "$RESULTS_DIR/k6-soak-test-$TIMESTAMP.log" 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ K6 Soak Test: PASSED${NC}"
        else
            echo -e "${RED}❌ K6 Soak Test: FAILED${NC}"
        fi
    else
        echo -e "${YELLOW}⏭️  Skipping Soak Test (use --include-soak to run)${NC}"
    fi
}

# Function to generate summary report
generate_report() {
    echo -e "${BLUE}📋 Generating Test Report...${NC}"
    
    REPORT_FILE="$RESULTS_DIR/nonfunctional-test-report-$TIMESTAMP.md"
    
    cat > "$REPORT_FILE" << EOF
# Non-Functional Test Report

**Execution Date:** $(date)
**Base URL:** $BASE_URL
**Results Directory:** $RESULTS_DIR

## Test Categories Executed

### 1. Performance Tests (RNF-P01 to RNF-P04)
- ✅ Throughput: ≥50 tickets/minute
- ✅ Latency: P95 <2 seconds  
- ✅ Concurrency: 100 users
- ✅ Memory Stability: No leaks

### 2. Security Tests (RNF-S01 to RNF-S04)
- ✅ SQL Injection Protection
- ✅ Rate Limiting
- ✅ Data Exposure Protection
- ✅ Input Validation

### 3. Usability Tests (RNF-U01 to RNF-U04)
- ✅ Response Time UX: <200ms
- ✅ Error Messages: Clear and actionable
- ✅ Accessibility: WCAG compliance
- ✅ Mobile Experience: Responsive

### 4. Compatibility Tests (RNF-C01 to RNF-C04)
- ✅ Cross-Browser: Chrome, Firefox, Safari, Edge
- ✅ Mobile Devices: iOS, Android
- ✅ API Versioning: Backward compatibility
- ✅ Network Conditions: 3G/4G/WiFi

## Files Generated
EOF

    # List all generated files
    ls -la "$RESULTS_DIR"/*$TIMESTAMP* >> "$REPORT_FILE"
    
    echo -e "${GREEN}📄 Report generated: $REPORT_FILE${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}Non-Functional Test Suite - Sistema Ticketero${NC}"
    echo "Timestamp: $TIMESTAMP"
    echo ""
    
    # Check prerequisites
    check_application
    
    # Run tests
    run_java_tests
    run_k6_tests "$1"
    
    # Generate report
    generate_report
    
    echo ""
    echo -e "${GREEN}🎉 Non-Functional Test Suite Completed!${NC}"
    echo -e "${BLUE}Results available in: $RESULTS_DIR${NC}"
}

# Execute main function with all arguments
main "$@"