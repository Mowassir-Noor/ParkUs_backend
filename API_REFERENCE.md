# ParkUs API Reference

## Conventions
- Auth header: `Authorization: Bearer <token>`
- Pagination: `page` (0-based), `size` (default 20), `sort=field,dir`
- Time: ISO 8601 `YYYY-MM-DDTHH:mm:ss`
- IDs: Long (BIGINT)
- Invalid JWTs return 401 immediately

## Authentication Endpoints

### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "ROLE_USER",
  "registrationDate": "2025-12-21T10:00:00"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

Response 200: (same as register)
```

---

## Parking Spot Endpoints

All require: `Authorization: Bearer <token>`

### Create Parking Spot
```http
POST /api/spots
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Downtown Parking",
  "description": "Safe and secure parking",
  "location": "123 Main St, City",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "regular",
  "pricePerHour": 5.00
}

Response 201:
{
  "spotId": 1,
  "title": "Downtown Parking",
  "description": "Safe and secure parking",
  "location": "123 Main St, City",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "regular",
  "pricePerHour": 5.00,
  "ownerId": 1
}
```

### Get All Parking Spots
```http
GET /api/spots
Authorization: Bearer <token>

Response 200: [array of spots]
```

### Get Parking Spot by ID
```http
GET /api/spots/{id}
Authorization: Bearer <token>

Response 200: (single spot object)
```

### Get Spots by Owner
```http
GET /api/spots/owner/{ownerId}
Authorization: Bearer <token>

Response 200: [array of spots owned by user]
```

### Update Parking Spot
```http
PUT /api/spots/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "location": "123 Main St, City",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "regular",
  "pricePerHour": 6.00
}

Response 200: (updated spot object)
```

### Delete Parking Spot
```http
DELETE /api/spots/{id}
Authorization: Bearer <token>

Response 204 No Content
```

---

## Availability Endpoints

All require: `Authorization: Bearer <token>`

### Create Availability
```http
POST /api/availability
Authorization: Bearer <token>
Content-Type: application/json

{
  "spotId": 1,
  "startTime": "2025-12-22T10:00:00",
  "endTime": "2025-12-22T18:00:00"
}

Response 201:
{
  "availabilityId": 1,
  "spotId": 1,
  "startTime": "2025-12-22T10:00:00",
  "endTime": "2025-12-22T18:00:00",
  "isBooked": false
}
```

### Get Availability by ID
```http
GET /api/availability/{id}
Authorization: Bearer <token>

Response 200: (availability object)
```

### Get All Availability for a Spot
```http
GET /api/availability/spot/{spotId}
Authorization: Bearer <token>

Response 200: [array of all availability slots]
```

### Get Available (Unbooked) Slots
```http
GET /api/availability/spot/{spotId}/available
Authorization: Bearer <token>

Response 200: [array of available slots only]
```

### Delete Availability
```http
DELETE /api/availability/{id}
Authorization: Bearer <token>

Response 204 No Content
```

---

## Booking Endpoints

All require: `Authorization: Bearer <token>`

### Create Booking
```http
POST /api/bookings
Authorization: Bearer <token>
Content-Type: application/json

{
  "availabilityId": 1,
  "renterId": 2
}

Response 200:
{
  "bookingId": 1,
  "spotId": 1,
  "renterId": 2,
  "ownerId": 1,
  "status": "confirmed",
  "totalAmount": 40.00,
  "startTime": "2025-12-22T10:00:00",
  "endTime": "2025-12-22T18:00:00"
}
```

### Get Booking by ID
```http
GET /api/bookings/{id}
Authorization: Bearer <token>

Response 200: (booking object)
```

### Get Bookings by Renter
```http
GET /api/bookings/renter/{renterId}
Authorization: Bearer <token>

Response 200: [array of bookings made by renter]
```

### Get Bookings by Owner
```http
GET /api/bookings/owner/{ownerId}
Authorization: Bearer <token>

Response 200: [array of bookings on owner's spots]
```

### Update Booking Status
```http
PATCH /api/bookings/{id}/status?status=completed
Authorization: Bearer <token>

Response 204 No Content

Valid statuses: pending, confirmed, cancelled, completed
Allowed transitions:
- pending -> confirmed | cancelled
- confirmed -> completed | cancelled
- completed/cancelled are terminal
Who can update: spot owner or admin (admin overrides ownership)
```

### Delete Booking
```http
DELETE /api/bookings/{id}
Authorization: Bearer <token>

Response 204 No Content

Rules:
- Renter can cancel only if booking is pending/confirmed and not started
- Admin can cancel any booking
- Cancelling frees the linked availability slot
```

---

## Admin Endpoints (ROLE_ADMIN)

All admin lists support paging: `page`, `size`, `sort`. Defaults:
- Users: sort=registrationDate,DESC
- Bookings: sort=bookedAt,DESC
- Spots: sort=createdAt,DESC
- Availability: sort=startTime,ASC

### List Users
```http
GET /admin/users
Authorization: Bearer <token>
```

### List Bookings
```http
GET /admin/bookings
Authorization: Bearer <token>
```

### Update Booking Status (admin override)
```http
PATCH /admin/bookings/{id}/status?status=cancelled
Authorization: Bearer <token>
Response 204
```

### Cancel Booking (admin override)
```http
DELETE /admin/bookings/{id}
Authorization: Bearer <token>
Response 204
```

### List Spots
```http
GET /admin/spots
Authorization: Bearer <token>
```

### List Availability
```http
GET /admin/availability
Authorization: Bearer <token>
```

---

## Error Responses

### 400 Bad Request - Validation Error
```json
{
  "status": 400,
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2025-12-21T10:00:00"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "timestamp": "2025-12-21T10:00:00"
}
```

### 401 Invalid Token
```json
{
  "status": 401,
  "message": "Invalid token"
}
```

### 403 Forbidden
```json
{
  "status": 403,
  "message": "You do not own this parking spot",
  "timestamp": "2025-12-21T10:00:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Parking spot not found",
  "timestamp": "2025-12-21T10:00:00"
}
```

### 409 Conflict
```json
{
  "status": 409,
  "message": "This time slot is already booked",
  "timestamp": "2025-12-21T10:00:00"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred"
}
```

---

## Admin Endpoints

**All admin endpoints require `ROLE_ADMIN` authority.**

### User Management

#### List All Users (Paginated)
```http
GET /admin/users?page=0&size=20&sort=registrationDate,desc
Authorization: Bearer <admin_token>

Response 200:
{
  "content": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "ROLE_USER",
      "registrationDate": "2025-12-21T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

#### Get User by ID
```http
GET /admin/users/{id}
Authorization: Bearer <admin_token>

Response 200:
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "role": "ROLE_USER",
  "registrationDate": "2025-12-21T10:00:00"
}
```

#### Update User
```http
PATCH /admin/users/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "newpassword123"  // optional
}

Response 200: (updated user object)
```

#### Delete User
```http
DELETE /admin/users/{id}
Authorization: Bearer <admin_token>

Response 204: No Content
```

#### Change User Role
```http
PATCH /admin/users/{id}/role?role=ROLE_ADMIN
Authorization: Bearer <admin_token>

Response 204: No Content

Parameters:
- role: ROLE_USER or ROLE_ADMIN
```

### Booking Management

#### List All Bookings (Paginated)
```http
GET /admin/bookings?page=0&size=20&sort=bookedAt,desc
Authorization: Bearer <admin_token>

Response 200: (paginated booking list)
```

#### Get Booking by ID
```http
GET /admin/bookings/{id}
Authorization: Bearer <admin_token>

Response 200: (booking details)
```

#### Create Booking as Admin
```http
POST /admin/bookings
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "availabilityId": 123,
  "renterId": 456
}

Response 200: (created booking)
```

#### Update Booking
```http
PATCH /admin/bookings/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "availabilityId": 789,
  "renterId": 456
}

Response 200: (updated booking with recalculated amount)

Note: Admin can change renter or availability slot
```

#### Update Booking Status
```http
PATCH /admin/bookings/{id}/status?status=confirmed
Authorization: Bearer <admin_token>

Response 204: No Content

Parameters:
- status: pending | confirmed | cancelled | completed
```

#### Delete Booking
```http
DELETE /admin/bookings/{id}
Authorization: Bearer <admin_token>

Response 204: No Content

Note: Admin can delete any booking regardless of status
```

#### Get Bookings by Status
```http
GET /admin/bookings/status/{status}
Authorization: Bearer <admin_token>

Response 200: [array of bookings]

Example: /admin/bookings/status/confirmed
```

### Parking Spot Management

#### List All Spots (Paginated)
```http
GET /admin/spots?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <admin_token>

Response 200: (paginated spot list)
```

#### Get Spot by ID
```http
GET /admin/spots/{id}
Authorization: Bearer <admin_token>

Response 200: (spot details)
```

#### Create Spot as Admin
```http
POST /admin/spots
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "Downtown Parking",
  "description": "Covered parking",
  "location": "123 Main St",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "covered",
  "pricePerHour": 5.50,
  "ownerId": 789
}

Response 200: (created spot)

Note: ownerId is required for admin to create spots
```

#### Update Parking Spot
```http
PUT /admin/spots/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "location": "456 New St",
  "latitude": 40.7580,
  "longitude": -73.9855,
  "slotType": "open",
  "pricePerHour": 7.00,
  "ownerId": 999  // optional: admin can change owner
}

Response 200: (updated spot)
```

#### Delete Parking Spot
```http
DELETE /admin/spots/{id}
Authorization: Bearer <admin_token>

Response 204: No Content

Note: Admin can delete any spot
```

#### Get Spots by Owner
```http
GET /admin/spots/owner/{ownerId}
Authorization: Bearer <admin_token>

Response 200: [array of spots]
```

### Availability Management

#### List All Availability (Paginated)
```http
GET /admin/availability?page=0&size=20&sort=startTime,asc
Authorization: Bearer <admin_token>

Response 200: (paginated availability list)
```

#### Get Availability by ID
```http
GET /admin/availability/{id}
Authorization: Bearer <admin_token>

Response 200:
{
  "availabilityId": 123,
  "spotId": 456,
  "startTime": "2025-12-25T10:00:00",
  "endTime": "2025-12-25T18:00:00",
  "isBooked": false
}
```

#### Create Availability as Admin
```http
POST /admin/availability
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "spotId": 123,
  "startTime": "2025-12-25T10:00:00",
  "endTime": "2025-12-25T18:00:00"
}

Response 200: (created availability)

Note: Admin can create availability for any spot
```

#### Update Availability
```http
PUT /admin/availability/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "spotId": 123,
  "startTime": "2025-12-25T12:00:00",
  "endTime": "2025-12-25T20:00:00"
}

Response 200: (updated availability)
```

#### Delete Availability
```http
DELETE /admin/availability/{id}
Authorization: Bearer <admin_token>

Response 204: No Content

Error 400: If availability is booked
{
  "statusCode": 400,
  "message": "Cannot delete booked availability. Cancel the booking first.",
  "timestamp": "2025-12-21T10:30:00"
}
```

---

## Common Validation Rules

### Email
- Must be valid email format
- Required for registration and login

### Password
- Minimum 8 characters
- Required

### Parking Spot
- Title: Required, max 100 characters
- Latitude: Required, -90 to 90
- Longitude: Required, -180 to 180
- Price: Required, must be > 0

### Availability
- Start time: Required, must be in future
- End time: Required, must be after start time
- Cannot overlap with existing availability (server-enforced lock)
- Cannot delete if already booked

### Booking
- Can only book your own renterId
- Cannot book past time slots
- Slot must not be already booked
- Status transitions restricted (see booking section)
- Cancelling frees the availability slot

---

## Authorization Rules

### Parking Spots
- Anyone can view all spots
- Only owner can update/delete their spots
- Admin can manage any spot

### Availability
- Only spot owner can create/delete availability
- Anyone can view availability
- Admin can manage any availability

### Bookings
- Users can only create bookings for themselves
- Users can view their own bookings
- Owners can view bookings on their spots
- Owners can update booking status; admins can update any
- Renters can cancel their bookings before start if pending/confirmed; admins can cancel any
- Admin can view/manage all bookings

---

## Testing with cURL

### Register and Login
```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","password":"password123"}'

# Save the token from response
TOKEN="eyJhbGciOiJIUzI1..."

# Use token for authenticated requests
curl -X GET http://localhost:8080/api/spots \
  -H "Authorization: Bearer $TOKEN"
```

---

## Notes

1. All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
2. JWT tokens expire after 24 hours (configurable)
3. Prices use BigDecimal with 2 decimal places
4. All dates/times are in the server's timezone
5. IDs are Long (BIGINT in database)
6. Admin lists are paged (default size 20 with sorting defaults above)
