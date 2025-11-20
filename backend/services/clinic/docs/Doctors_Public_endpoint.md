# CLINIC_SERVICE_API.md

**Scope:** Public & Doctor endpoints (with **soft delete**) for Clinic Service.
**Audience:** Devs & AI agents implementing/consuming the API.
**Version:** 1.0 (Nov 2025)

---

## 🧠 Agent Instructions (read me first)

1. Follow this file as the **single source of truth** for Public & Doctor APIs.
2. Enforce the **status workflow** and **soft delete** rules exactly as specified.
3. Always return responses in the schemas below; validate inputs; include pagination metadata.

---

## 0) Domain Model & Conventions

### 0.1 Status lifecycle (with soft delete)

* `DRAFT` → `PENDING` → `APPROVED`
* From `PENDING`: `APPROVED` or `REJECTED`
* Editable by **doctor (owner)** only when: `DRAFT` or `REJECTED`
* **Soft delete** = set status to `ARCHIVED`.
* **Public visibility**: only `APPROVED`.

> Enum `clinic_status`: `DRAFT | PENDING | APPROVED | REJECTED | ARCHIVED`

### 0.2 Resource: `Clinic`

```json
{
  "id": "CLN001",
  "fullname": "Hoan My International Clinic",
  "name": "Hoan My",
  "address": "12 Nguyen Trai, District 5, HCMC",
  "clinicDetailInfo": "Long markdown / HTML allowed",
  "image": "https://cdn.example/clinics/cln001.png",
  "slug": "hoan-my",
  "status": "APPROVED"
}
```

### 0.3 General rules

* **Auth:**

  * Public endpoints: no auth.
  * Doctor endpoints: `ROLE_DOCTOR` with **ownership** check (`created_by == subject`).
* **Pagination:** `page` (0-based), `size` (default 20, max 100), `sort` (e.g., `name,asc`).
* **Errors (JSON):**

```json
{ "timestamp": "2025-11-04T09:00:00Z", "status": 400, "error": "Bad Request", "message": "Reason", "path": "/api/..." }
```

* **Idempotency:** action endpoints (`:submit`, `:archive`, `:restore`) are **idempotent**: repeated calls in the final state return the same result (200) without side effects.
* **Validation:**

  * `name`, `slug` required on create.
  * `slug` must be unique.
  * On update, mutable only in `DRAFT | REJECTED`.
* **Soft delete visibility:** `ARCHIVED` never appears in public lists. Doctors (owner) can see `ARCHIVED` in `/mine`.

---

## 1) PUBLIC API

### 1.1 List approved clinics

**GET** `/api/clinics`

**Query params**

* `q` (optional): full-text search on `name|fullname|address|slug`
* `page`, `size`, `sort`

**Response `200`**

```json
{
  "content": [
    {
      "id": "CLN001",
      "fullname": "Hoan My International Clinic",
      "name": "Hoan My",
      "address": "12 Nguyen Trai, District 5, HCMC",
      "clinicDetailInfo": "...",
      "image": "https://...",
      "slug": "hoan-my",
      "status": "APPROVED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**Rules**

* Only return `status = APPROVED`.
* Support `q` filter (optional).

---

### 1.2 Get clinic detail (approved only)

**GET** `/api/clinics/{id}`

**Response `200`** → same `Clinic` schema (must be `APPROVED`).
**Errors**

* `404` if clinic not found or not `APPROVED`.

---

## 2) DOCTOR API

> All endpoints require `ROLE_DOCTOR`. Ownership check required unless stated.

### 2.1 Create clinic (initial = DRAFT)

**POST** `/api/clinics`

**Request**

```json
{
  "name": "Hoan My",
  "fullname": "Hoan My International Clinic",
  "address": "12 Nguyen Trai, District 5, HCMC",
  "clinicDetailInfo": "Markdown text...",
  "image": "https://cdn.example/hoanmy.png",
  "slug": "hoan-my"
}
```

**Response `201 Created`**

* Body: created `Clinic` (status = `DRAFT`)
* `Location: /api/clinics/{id}`

**Errors**

* `400` validation, `409` slug duplicated.

---

### 2.2 List my clinics (all statuses, including ARCHIVED)

**GET** `/api/clinics/mine`

**Query params**: `status` (optional: one of lifecycle), `page`, `size`, `sort`, `q` (optional)
**Response `200`**: paged clinics (owner only)

---

### 2.3 Get my clinic by id (any status)

**GET** `/api/clinics/{id}`

**Response `200`**: clinic if owner
**Errors**

* `403` if not owner
* `404` if not found

---

### 2.4 Update clinic (only DRAFT | REJECTED)

**PATCH** `/api/clinics/{id}`

**Request** (partial)

```json
{
  "name": "Hoan My Updated",
  "address": "New address",
  "clinicDetailInfo": "Updated content",
  "image": "https://cdn.example/new.png",
  "slug": "hoan-my"
}
```

**Rules**

* Allowed only when status ∈ `{DRAFT, REJECTED}`.
* On conflict with `slug`, return `409`.

**Response `200`**: updated clinic
**Errors**: `403` not owner, `409` slug conflict, `409` state not editable

---

### 2.5 Submit clinic for review (DRAFT | REJECTED → PENDING)

**POST** `/api/clinics/{id}:submit`

**Response `200`**: clinic with `status = PENDING`
**Idempotent behavior**

* If already `PENDING`, return current clinic (`200`)
  **Errors**: `403` not owner, `409` invalid state

---

### 2.6 Soft delete (archive) my clinic

**POST** `/api/clinics/{id}:archive`

**Behavior**

* Allowed states: `DRAFT | REJECTED` (owner-driven archival).
* Transition: `→ ARCHIVED`.
* **Effect:** Clinic hidden from public & from non-owner queries.
* **Branches / links:** remain intact for audit; future restore possible.

**Response `200`**: clinic with `status = ARCHIVED`
**Idempotent:** re-calling on `ARCHIVED` returns `200` unchanged.
**Errors:** `403` not owner, `409` invalid state (cannot archive `PENDING | APPROVED`)

---

### 2.7 Restore my clinic from ARCHIVED

**POST** `/api/clinics/{id}:restore`

**Behavior**

* Allowed only from `ARCHIVED` → `DRAFT` (owner prepares updates before resubmitting).
* Use normal update + `:submit` after restore.

**Response `200`**: clinic with `status = DRAFT`
**Errors:** `403` not owner, `409` invalid state

---

## 3) State Machine (Doctor scope)

```text
DRAFT --submit--> PENDING
REJECTED --submit--> PENDING
ARCHIVED --restore--> DRAFT

Constraints:
- update: only DRAFT | REJECTED
- archive: only DRAFT | REJECTED
```

*(Admin transitions PENDING→APPROVED / PENDING→REJECTED sẽ được mô tả ở file ADMIN khi bạn cần.)*

---

## 4) Soft Delete Notes (Implementation)

* **DB:** không xóa record `clinics`; chỉ đổi `status` → `ARCHIVED`.
* **Public queries**: `WHERE status = 'APPROVED'`.
* **Doctor `/mine`**: có thể filter `status` tùy ý; mặc định trả tất cả (bao gồm `ARCHIVED`).
* **Logging (khuyến nghị):** ghi vào `clinic_verifications` các action `SUBMIT`, `ARCHIVE`, `RESTORE` (append-only).

**Sample verification log (Doctor actions):**

```json
{
  "clinicId": "CLN001",
  "action": "ARCHIVE",
  "actorId": "00000000-0000-0000-0000-000000000001",
  "comment": "Owner archived this clinic",
  "createdAt": "2025-11-04T09:00:00Z"
}
```

---

## 5) Validation & Error Cases (Doctor)

* `PATCH` when `status ∉ {DRAFT, REJECTED}` → `409 Conflict`
* `:submit` when `status ∉ {DRAFT, REJECTED, PENDING}` (i.e., `APPROVED`/`ARCHIVED`) → `409`
* `:archive` when `status ∉ {DRAFT, REJECTED}` → `409`
* `:restore` when `status ≠ ARCHIVED` → `409`
* Not owner → `403`
* Not found → `404`
* Duplicate `slug` → `409`

---

## 6) Example Flows

### 6.1 Create → Edit → Submit

1. `POST /api/clinics` → `DRAFT`
2. `PATCH /api/clinics/{id}` (edit)
3. `POST /api/clinics/{id}:submit` → `PENDING`

### 6.2 Reject → Edit → Submit

1. (Admin rejects → `REJECTED`)
2. Doctor `PATCH` (allowed)
3. Doctor `:submit` → `PENDING`

### 6.3 Archive & Restore

1. Doctor `:archive` from `DRAFT` → `ARCHIVED`
2. Doctor `:restore` → `DRAFT`
3. Doctor edits & `:submit` again

---

## 7) Security (Doctor)

* Must verify `owner(userId) == clinic.created_by` (stored server-side).
* Public endpoints never leak non-approved clinics.
* Rate-limit action endpoints to prevent abuse.

---

## 8) Contract Tests (suggested)

* Visibility: Public list excludes non-APPROVED.
* State guards: update/submit/archive/restore follow rules.
* Idempotency: repeat `:archive`, `:restore`, `:submit(PENDING)` returns 200 stable.
* Slug uniqueness.
