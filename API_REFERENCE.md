# ParkUs API Reference

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
```

### Delete Booking
```http
DELETE /api/bookings/{id}
Authorization: Bearer <token>

Response 204 No Content
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
- Cannot overlap with existing availability

### Booking
- Can only book your own renterId
- Cannot book past time slots
- Slot must not be already booked

---

## Authorization Rules

### Parking Spots
- Anyone can view all spots
- Only owner can update/delete their spots

### Availability
- Only spot owner can create/delete availability
- Anyone can view availability

### Bookings
- Users can only create bookings for themselves
- Users can view their own bookings
- Owners can view bookings on their spots
- Only owners can update booking status
- Only renters can delete their bookings

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
