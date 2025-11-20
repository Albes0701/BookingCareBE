# Payment Service Payment Flow and Postman Testing Guide

## Overview

The Payment Service is responsible for creating and tracking payment orders for BookingCare. It is exposed internally through the API Gateway and can also be accessed directly on port `8076` for local development and testing. The service persists payment records in PostgreSQL and integrates with PayOS to generate hosted payment links and receive webhook callbacks.

```
Base URL (direct access) : http://localhost:8076
Base URL (through gateway): http://localhost:8222/payment
Health dependencies       : config-server, discovery-service, PostgreSQL
Database                  : jdbc:postgresql://postgresql:5432/payment
```

## End-to-End Payment Flow

1. **Client requests a payment** by calling `POST /api/v1/payments` to create a local payment record for a booking.
2. **Optional PayOS link creation** is triggered with `POST /api/v1/payments/create` to obtain a hosted checkout URL from PayOS.
3. **Customer completes payment** using the PayOS link. PayOS sends a webhook to `POST /api/v1/webhooks/callback` with the transaction result.
4. **Webhook verification** runs through the PayOS SDK. When the signature is valid, the service updates the local payment record with the new status and metadata.
5. **Back-office users** can query or update payments via the REST endpoints on port 8076 or by going through the API Gateway.

## Service Configuration Highlights

- `docker-compose.yml` exposes `payment-service` on `8076` and connects it to the shared `microservices-net` network.
- `services/config-server/.../payment-service.yml` sets datasource credentials, enables Flyway, and registers the service with Eureka.
- `services/payment/Dockerfile` builds the Spring Boot application; the container image is used by the Docker Compose stack.
- `ngrok` (optional) is configured to tunnel the service from `payment-service:8076` for external callbacks when testing PayOS webhooks.

## REST API Catalogue

| Method | Endpoint                                | Purpose                                     | Notes                                            |
| ------ | --------------------------------------- | ------------------------------------------- | ------------------------------------------------ |
| POST   | `/api/v1/payments`                      | Create a payment record linked to a booking | Uses `PaymentRequestCreate`                      |
| GET    | `/api/v1/payments`                      | List all payments                           | Returns `List<PaymentResponseDTO>`               |
| GET    | `/api/v1/payments/{id}`                 | Retrieve a single payment by UUID           | Returns `404` if not found                       |
| GET    | `/api/v1/payments/search?bookingId=...` | Find payment by booking ID                  | Convenience lookup                               |
| PUT    | `/api/v1/payments/{id}`                 | Update payment status                       | Consumes `PaymentStatusUpdateRequest`            |
| POST   | `/api/v1/payments/create`               | Request a PayOS payment link                | Returns `ApiResponse<CreatePaymentLinkResponse>` |
| POST   | `/api/v1/webhooks/callback`             | Receive PayOS webhook events                | Requires valid PayOS signature                   |

All responses that wrap `ApiResponse` follow the `{ "error": number, "message": string, "data": ... }` structure.

## Postman Testing Setup (Port 8076)

1. **Create an environment** with variables:
   - `baseUrl` = `http://localhost:8076`
   - `bookingId` = sample booking ID (for example `BOOKING-001`).
2. **Import requests** listed below or create them manually. Ensure `Content-Type: application/json` is set for all bodies.
3. **Order of execution** for manual testing:
   1. Create payment record.
   2. (Optional) Generate PayOS payment link.
   3. Query payment list or details.
   4. Update payment status (manual override) if needed.
   5. Simulate webhook callback (manual invocation) when testing the signature path.

### 1. Create Payment

- **Method**: POST `{{baseUrl}}/api/v1/payments`
- **Body**:

```json
{
  "bookingId": "{{bookingId}}",
  "amount": 500000,
  "description": "Initial consultation"
}
```

- **Expected**: `201 Created` with `PaymentResponseDTO` payload containing generated `id`, `status`, and `orderCode`.

### 2. List Payments

- **Method**: GET `{{baseUrl}}/api/v1/payments`
- **Expected**: `200 OK` with an array of payments. Useful to verify records created by the previous step.

### 3. Get Payment by ID

- **Method**: GET `{{baseUrl}}/api/v1/payments/{{paymentId}}`
- **Path variable**: supply the `id` returned from step 1.
- **Expected**: `200 OK` with the matching payment or `404 Not Found` if the ID is invalid.

### 4. Search by Booking ID

- **Method**: GET `{{baseUrl}}/api/v1/payments/search?bookingId={{bookingId}}`
- **Expected**: `200 OK` with payment details linked to the booking or `404 Not Found` when no payment exists.

### 5. Update Payment Status

- **Method**: PUT `{{baseUrl}}/api/v1/payments/{{paymentId}}`
- **Body**:

```json
{
  "newStatus": "COMPLETED"
}
```

- **Expected**: `200 OK` with updated payment state. Use values supported by your service logic (for example `PENDING`, `COMPLETED`, `CANCELLED`).

### 6. Create PayOS Payment Link

- **Method**: POST `https://nonappendant-carolina-basidiosporous.ngrok-free.dev/api/v1/payments/create`
- **Body**:

```json
{
  "bookingId": "{{bookingId}}",
  "productName": "Premium health check",
  "description": "Payment for booking {{bookingId}}",
  "returnUrl": "https://example.com/payment/success",
  "cancelUrl": "https://example.com/payment/cancel",
  "price": 10000
}
```

- **Expected**: `200 OK` with an `ApiResponse` payload that contains the PayOS checkout URL and metadata inside `data`.

### 7. Simulate PayOS Webhook

The webhook endpoint expects a valid PayOS signature. For end-to-end testing, expose the service via ngrok (already configured in Docker Compose) and configure the callback URL in the PayOS dashboard. To simulate locally without PayOS:

- **Method**: POST `{{baseUrl}}/api/v1/webhooks/callback`
- **Headers**: mimic PayOS webhook headers (`x-signature`, `x-timestamp`, ...). The request will fail signature verification but is useful to confirm error handling.
- **Body example**:

```json
{
  "data": {
    "orderCode": 123456789,
    "transactionStatus": "PAID",
    "amount": 500000
  }
}
```

- **Expected**: `ApiResponse.error` unless the signature is valid. When using real PayOS callbacks, the service logs the payload and updates the payment status through `paymentService.UpdatePaymentStatusByWebhook`.

## Troubleshooting Tips

- Ensure PostgreSQL (`postgresql` service) is running and reachable before starting the payment service.
- Check `config-server` health at `http://localhost:8888/actuator/health`; configuration must be available for the payment service to start.
- Verify service registration in Eureka at `http://localhost:8761` (look for `payment-service`).
- For webhook debugging, tail the payment container logs: `docker compose logs -f payment-service`.
- If ngrok is used, the tunnel URL must match the webhook URL configured in PayOS.

## Next Steps

- Export your Postman collection and attach it to project documentation for team sharing.
- Automate regression tests by scripting Postman Newman runs against the local stack or during CI.
- Keep environment variables (PayOS keys, ngrok token) in a secure vault and avoid committing them to source control.
