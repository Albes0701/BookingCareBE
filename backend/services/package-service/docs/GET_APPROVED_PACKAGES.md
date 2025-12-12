# API Endpoint Documentation: Get Approved Packages

## Endpoint

```
GET /api/v1/packages-services/packages
```

## Description

Returns the list of approved health check packages for catalog screens. This is a public endpoint that doesn't require authentication.

## Request

### Method
```
GET
```

### Headers
```
Content-Type: application/json
```

### Path Parameters
None

### Query Parameters
None

### Request Body
None (GET request - no body required)

---

## Response

### Success Response (200 OK)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Gói khám sức khỏe tổng quát",
    "slug": "go-kham-suc-khoe-tong-quat",
    "description": "Gói khám sức khỏe toàn diện cho cả gia đình",
    "price": 2500000,
    "duration": 120,
    "status": "APPROVED",
    "createdAt": "2025-11-27T10:30:00Z",
    "updatedAt": "2025-11-27T10:30:00Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Gói khám tim mạch",
    "slug": "go-kham-tim-mach",
    "description": "Kiểm tra chức năng tim và huyết áp",
    "price": 1500000,
    "duration": 90,
    "status": "APPROVED",
    "createdAt": "2025-11-27T09:15:00Z",
    "updatedAt": "2025-11-27T09:15:00Z"
  }
]
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Unique identifier of the package |
| `name` | String | Package name |
| `slug` | String | URL-friendly identifier |
| `description` | String | Package description |
| `price` | Number | Package price in VND |
| `duration` | Number | Duration in minutes |
| `status` | String | Package status (APPROVED, PENDING, REJECTED, DRAFT) |
| `createdAt` | ISO 8601 DateTime | Creation timestamp |
| `updatedAt` | ISO 8601 DateTime | Last update timestamp |

### Error Response (500 Internal Server Error)

```json
{
  "timestamp": "2025-11-27T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Unable to fetch approved packages",
  "path": "/api/v1/packages-services/packages"
}
```

---

## Example Usage

### cURL
```bash
curl -X GET http://localhost:8222/api/v1/packages-services/packages \
  -H "Content-Type: application/json"
```

### JavaScript (Fetch)
```javascript
fetch('http://localhost:8222/api/v1/packages-services/packages', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Axios
```javascript
axios.get('http://localhost:8222/api/v1/packages-services/packages')
  .then(response => console.log(response.data))
  .catch(error => console.error(error));
```

### Python (Requests)
```python
import requests

response = requests.get('http://localhost:8222/api/v1/packages-services/packages')
packages = response.json()
print(packages)
```

---

## Notes

- This endpoint is **public** and does not require authentication
- Returns only **APPROVED** packages
- The response is a list that can be empty if no approved packages exist
- Package prices are in Vietnamese Dong (VND)
