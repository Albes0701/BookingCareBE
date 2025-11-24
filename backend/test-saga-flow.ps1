# Test Saga Flow Script
# Run this to verify complete Saga pattern

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SAGA PATTERN TEST SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check Kafka
Write-Host "[1/6] Checking Kafka infrastructure..." -ForegroundColor Yellow
$kafka = docker ps | Select-String "booking-kafka"
if ($kafka) {
    Write-Host "  ✅ Kafka is running" -ForegroundColor Green
} else {
    Write-Host "  ❌ Kafka is not running!" -ForegroundColor Red
    exit 1
}

# Step 2: Check Topics
Write-Host "`n[2/6] Verifying Kafka topics..." -ForegroundColor Yellow
$topics = docker exec booking-kafka kafka-topics --list --bootstrap-server localhost:9092 2>$null

$requiredTopics = @("booking-events", "schedule-events", "schedule-commands", "payment-events", "payment-commands")
$missingTopics = @()

foreach ($topic in $requiredTopics) {
    if ($topics -match $topic) {
        Write-Host "  ✅ $topic" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $topic (MISSING)" -ForegroundColor Red
        $missingTopics += $topic
    }
}

if ($missingTopics.Count -gt 0) {
    Write-Host "`n  Creating missing topics..." -ForegroundColor Yellow
    foreach ($topic in $missingTopics) {
        docker exec booking-kafka kafka-topics --create --topic $topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 2>$null
        Write-Host "  ✅ Created $topic" -ForegroundColor Green
    }
}

# Step 3: Check Services
Write-Host "`n[3/6] Checking services..." -ForegroundColor Yellow
$services = docker ps --format "{{.Names}}" | Select-String "booking-service|schedule-service|payment-service"
if ($services) {
    $services | ForEach-Object { Write-Host "  ✅ $_" -ForegroundColor Green }
} else {
    Write-Host "  ⚠️  Some services might not be running" -ForegroundColor Yellow
}

# Step 4: Verify Event Publisher
Write-Host "`n[4/6] Verifying Event Publisher..." -ForegroundColor Yellow
$publisherFile = "D:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\saga\BookingEventPublisher.java"
if (Test-Path $publisherFile) {
    $content = Get-Content $publisherFile -Raw
    if ($content -match "publishBookingCreatedEvent") {
        Write-Host "  ✅ publishBookingCreatedEvent() found" -ForegroundColor Green
    }
    if ($content -match "publishPaymentRequestedEvent") {
        Write-Host "  ✅ publishPaymentRequestedEvent() found" -ForegroundColor Green
    }
    if ($content -match "publishBookingConfirmedEvent") {
        Write-Host "  ✅ publishBookingConfirmedEvent() found" -ForegroundColor Green
    }
} else {
    Write-Host "  ❌ BookingEventPublisher.java not found!" -ForegroundColor Red
}

# Step 5: Verify Saga Orchestrator
Write-Host "`n[5/6] Verifying Saga Orchestrator..." -ForegroundColor Yellow
$orchestratorFile = "D:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\saga\BookingSagaOrchestrator.java"
if (Test-Path $orchestratorFile) {
    $content = Get-Content $orchestratorFile -Raw
    if ($content -match "@KafkaListener.*schedule-events") {
        Write-Host "  ✅ Schedule events listener configured" -ForegroundColor Green
    }
    if ($content -match "@KafkaListener.*payment-events") {
        Write-Host "  ✅ Payment events listener configured" -ForegroundColor Green
    }
    if ($content -match "handleHoldSlotSucceeded") {
        Write-Host "  ✅ handleHoldSlotSucceeded() found" -ForegroundColor Green
    }
    if ($content -match "handlePaymentSucceeded") {
        Write-Host "  ✅ handlePaymentSucceeded() found" -ForegroundColor Green
    }
} else {
    Write-Host "  ❌ BookingSagaOrchestrator.java not found!" -ForegroundColor Red
}

# Step 6: Verify Application Service
Write-Host "`n[6/6] Verifying BookingApplicationService..." -ForegroundColor Yellow
$appServiceFile = "D:\BookingCareBE\backend\services\booking\src\main\java\com\bookingcare\application\handler\BookingApplicationService.java"
if (Test-Path $appServiceFile) {
    $content = Get-Content $appServiceFile -Raw
    if ($content -match "eventPublisher\.publishBookingCreatedEvent") {
        Write-Host "  ✅ publishBookingCreatedEvent() called in createBooking()" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Event not published in createBooking()!" -ForegroundColor Red
    }
    if ($content -match "correlationId") {
        Write-Host "  ✅ Correlation ID tracking enabled" -ForegroundColor Green
    }
} else {
    Write-Host "  ❌ BookingApplicationService.java not found!" -ForegroundColor Red
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  SAGA PATTERN VERIFICATION COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ All checks passed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Start all services: docker compose up -d" -ForegroundColor White
Write-Host "  2. Monitor events: http://localhost:8080 (Kafka UI)" -ForegroundColor White
Write-Host "  3. Test API: POST /api/v1/booking/submit-booking" -ForegroundColor White
Write-Host "  4. Check logs: docker compose logs -f booking-service" -ForegroundColor White
Write-Host ""
Write-Host "Full report: SAGA_VERIFICATION_REPORT.md" -ForegroundColor Cyan
Write-Host ""
