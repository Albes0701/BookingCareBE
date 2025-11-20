# Public API – Specialties Endpoints

This document describes the implementation of **Public Specialties APIs** for the ExpertiseService. It follows the conventions used in `AGENTS.md` and is intended as a precise coding and testing reference.

---

## 🩺 Endpoint 1: GET `/api/specialties`

### 🎯 Purpose

Returns a **public list of medical specialties** managed by Admin. Only specialties that are **APPROVED** and **not deleted (`is_deleted = false`)** are visible to the public.

---

### 🔧 Request

**Method:** `GET`

**Path:** `/api/specialties`

**Query Parameters:**

| Param  | Type       | Required | Description                                                                                   |
| ------ | ---------- | -------- | --------------------------------------------------------------------------------------------- |
| `q`    | `string`   | ❌        | Full-text search by `name`, `slug`, or `code`.                                                |
| `code` | `string[]` | ❌        | Filter by one or multiple `code` values. Repeat param: `?code=OPHTHALMOLOGY&code=CARDIOLOGY`. |
| `page` | `int`      | ❌        | Page number (default `0`).                                                                    |
| `size` | `int`      | ❌        | Page size (default `20`).                                                                     |
| `sort` | `string`   | ❌        | Sorting field and direction (e.g. `sort=name,asc`).                                           |

---

### ⚙️ Processing Rules

* Query DB table `specialties` where:

  * `status = 'APPROVED'`
  * `is_deleted = false`
* Optional search (`q`): match against `LOWER(name)`, `slug`, `code`.
* Optional filter (`code`): IN condition on `code` list.
* Return a **paginated** list ordered by `sort` parameter.
* Default sort: `name,asc`.

---

### 🧩 Controller Stub

```java
@GetMapping("/api/specialties")
public Page<SpecialtiesResponseDTO> listSpecialties(
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "code") List<String> codes,
        @PageableDefault(size = 20, sort = "name") Pageable pageable) {
    return service.listSpecialties(q, codes, pageable);
}
```

---

### 🧠 Service Stub

```java
public Page<SpecialtiesResponseDTO> listSpecialties(String q, List<String> codes, Pageable pageable) {
    Page<Specialties> page = specialtiesRepo.searchApproved(q, codes, pageable);
    return page.map(mapper::toSpecialtiesResponseDTO);
}
```

---

### 🗃️ Repository Example (JPQL)

```java
@Query("""
SELECT s FROM Specialties s
WHERE s.deleted = false AND s.status = 'APPROVED'
  AND (:q IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(s.slug) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%')))
  AND (:codes IS NULL OR s.code IN :codes)
""")
Page<Specialties> searchApproved(@Param("q") String q,
                                @Param("codes") List<String> codes,
                                Pageable pageable);
```

---

### ✅ Example Response

```json
{
  "content": [
    {
      "id": "6f8e4e5f-42a9-4a55-9b52-84aa3c7b7a22",
      "code": "OPHTHALMOLOGY",
      "name": "Chuyên khoa Mắt",
      "slug": "chuyen-khoa-mat",
      "image": "https://cdn.bookingcare.vn/specialties/eye.jpg"
    },
    {
      "id": "7c9e8e6d-13b9-451f-8e25-34af2b12d391",
      "code": "CARDIOLOGY",
      "name": "Tim mạch",
      "slug": "chuyen-khoa-tim-mach",
      "image": "https://cdn.bookingcare.vn/specialties/heart.jpg"
    }
  ],
  "pageable": {"pageNumber": 0, "pageSize": 20},
  "totalElements": 2
}
```

---

### 🧪 Test Cases

| Case              | Input                                                 | Expected                                             |
| ----------------- | ----------------------------------------------------- | ---------------------------------------------------- |
| ✅ Normal          | `/api/specialties?page=0&size=10`                     | HTTP 200, JSON list of approved specialties.         |
| 🔎 Search         | `/api/specialties?q=mat`                              | Only specialties whose name/code/slug matches `mat`. |
| 🔍 Filter by code | `/api/specialties?code=OPHTHALMOLOGY&code=CARDIOLOGY` | Return only those codes.                             |
| ⚠️ Empty          | `/api/specialties?q=zzz`                              | Empty `content` array.                               |

---

## 🩺 Endpoint 2: GET `/api/specialties/{codeOrSlug}`

### 🎯 Purpose

Retrieve **a single approved specialty** by its `code` (preferred) or `slug` (fallback). Frontend should use `code` for stable lookups.

---

### 🔧 Request

**Method:** `GET`

**Path:** `/api/specialties/{codeOrSlug}`

**Path Variable:**

| Name         | Type     | Description                         |
| ------------ | -------- | ----------------------------------- |
| `codeOrSlug` | `string` | Specialty code (preferred) or slug. |

---

### ⚙️ Processing Rules

* Check for a record with `code = :codeOrSlug` (case-insensitive).
* If not found, fallback to `slug = :codeOrSlug`.
* Only return if:

  * `status = 'APPROVED'`
  * `is_deleted = false`
* Throw `ApiException(ErrorCode.SPECIALTY_NOT_FOUND)` if not found.

---

### 🧩 Controller Stub

```java
@GetMapping("/api/specialties/{codeOrSlug}")
public SpecialtiesResponseDTO getSpecialty(@PathVariable String codeOrSlug) {
    return service.getSpecialtyByCodeOrSlug(codeOrSlug);
}
```

---

### 🧠 Service Stub

```java
public SpecialtiesResponseDTO getSpecialtyByCodeOrSlug(String codeOrSlug) {
    Specialties sp = specialtiesRepo.findApprovedByCodeOrSlug(codeOrSlug)
            .orElseThrow(() -> new ApiException(ErrorCode.SPECIALTY_NOT_FOUND));
    return mapper.toSpecialtiesResponseDTO(sp);
}
```

---

### 🗃️ Repository Example (JPQL)

```java
@Query("""
SELECT s FROM Specialties s
WHERE s.deleted = false AND s.status = 'APPROVED'
  AND (LOWER(s.code) = LOWER(:key) OR LOWER(s.slug) = LOWER(:key))
""")
Optional<Specialties> findApprovedByCodeOrSlug(@Param("key") String key);
```

---

### ✅ Example Response

```json
{
  "id": "6f8e4e5f-42a9-4a55-9b52-84aa3c7b7a22",
  "code": "OPHTHALMOLOGY",
  "name": "Chuyên khoa Mắt",
  "slug": "chuyen-khoa-mat",
  "image": "https://cdn.bookingcare.vn/specialties/eye.jpg",
  "specialtyDetailInfor": "Chuyên điều trị các bệnh về mắt và thị lực."
}
```

---

### 🧪 Test Cases

| Case            | Input                              | Expected                                   |
| --------------- | ---------------------------------- | ------------------------------------------ |
| ✅ Found by code | `/api/specialties/OPHTHALMOLOGY`   | 200 + JSON specialty.                      |
| ✅ Found by slug | `/api/specialties/chuyen-khoa-mat` | 200 + JSON specialty.                      |
| ⚠️ Not found    | `/api/specialties/invalid-code`    | 404 + `{ "code": "SPECIALTY_NOT_FOUND" }`. |

---

### 🧾 Notes

* This endpoint is **public** (no auth required).
* Recommended frontend lookup key: `code`.
* Use slug for SEO URLs.
* Soft-deleted or non-approved records must never appear in results.

---

**End of Document**
