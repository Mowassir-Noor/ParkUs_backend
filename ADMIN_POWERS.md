# Admin Powers Documentation

## Overview
Admin users now have full control over all resources in the ParkUs system. All admin endpoints require `ROLE_ADMIN` authority.

## Base URL
All admin endpoints are prefixed with `/admin`

## Authentication
All requests require:
```
Authorization: Bearer <admin_jwt_token>
```

---

## 👥 USER MANAGEMENT

### List All Users (Paginated)
```http
GET /admin/users?page=0&size=20&sort=registrationDate,desc
```
**Response**: Page of users with pagination info

### Get User by ID
```http
GET /admin/users/{id}
```
**Response**: User details

### Update User
```http
PATCH /admin/users/{id}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "newpassword123"  // Optional
}
```
**Response**: Updated user details

### Delete User
```http
DELETE /admin/users/{id}
```
**Response**: 204 No Content

### Change User Role
```http
PATCH /admin/users/{id}/role?role=ROLE_ADMIN
```
**Parameters**: 
- `role`: `ROLE_USER` or `ROLE_ADMIN`

**Response**: 204 No Content

---

## 📅 BOOKING MANAGEMENT

### List All Bookings (Paginated)
```http
GET /admin/bookings?page=0&size=20&sort=bookedAt,desc
```
**Response**: Page of bookings

### Get Booking by ID
```http
GET /admin/bookings/{id}
```
**Response**: Booking details

### Create Booking (As Admin)
```http
POST /admin/bookings
Content-Type: application/json

{
  "availabilityId": 123,
  "renterId": 456
}
```
**Response**: Created booking details

### Update Booking
```http
PATCH /admin/bookings/{id}
Content-Type: application/json

{
  "availabilityId": 789,
  "renterId": 456
}
```
**Powers**: Admin can change availability slot, change renter, system recalculates total amount
**Response**: Updated booking details

### Update Booking Status
```http
PATCH /admin/bookings/{id}/status?status=confirmed
```
**Parameters**:
- `status`: `pending`, `confirmed`, `cancelled`, `completed`

**Response**: 204 No Content

### Delete Booking
```http
DELETE /admin/bookings/{id}
```
**Powers**: Admin can delete any booking regardless of status or ownership
**Response**: 204 No Content

### Get Bookings by Status
```http
GET /admin/bookings/status/{status}
```
**Example**: `/admin/bookings/status/confirmed`
**Response**: List of bookings with that status

---

## 🅿️ PARKING SPOT MANAGEMENT

### List All Parking Spots (Paginated)
```http
GET /admin/spots?page=0&size=20&sort=createdAt,desc
```
**Response**: Page of parking spots

### Get Spot by ID
```http
GET /admin/spots/{id}
```
**Response**: Parking spot details

### Create Parking Spot (As Admin)
```http
POST /admin/spots
Content-Type: application/json

{
  "title": "Downtown Parking",
  "description": "Covered parking near mall",
  "location": "123 Main St",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "covered",
  "pricePerHour": 5.50,
  "ownerId": 789  // Required for admin creation
}
```
**Powers**: Admin can create spots for any user
**Response**: Created spot details

### Update Parking Spot
```http
PUT /admin/spots/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "location": "456 New St",
  "latitude": 40.7580,
  "longitude": -73.9855,
  "slotType": "open",
  "pricePerHour": 7.00,
  "ownerId": 999  // Admin can change owner
}
```
**Powers**: Admin can modify any field including changing ownership
**Response**: Updated spot details

### Delete Parking Spot
```http
DELETE /admin/spots/{id}
```
**Powers**: Admin can delete any spot regardless of ownership
**Response**: 204 No Content

### Get Spots by Owner
```http
GET /admin/spots/owner/{ownerId}
```
**Response**: List of spots owned by that user

---

## ⏰ AVAILABILITY MANAGEMENT

### List All Availability Slots (Paginated)
```http
GET /admin/availability?page=0&size=20&sort=startTime,asc
```
**Response**: Page of availability slots

### Get Availability by ID
```http
GET /admin/availability/{id}
```
**Response**: Availability details

### Create Availability Slot (As Admin)
```http
POST /admin/availability
Content-Type: application/json

{
  "spotId": 123,
  "startTime": "2025-12-25T10:00:00",
  "endTime": "2025-12-25T18:00:00"
}
```
**Powers**: Admin can create availability for any spot
**Response**: Created availability details

### Update Availability Slot
```http
PUT /admin/availability/{id}
Content-Type: application/json

{
  "spotId": 123,
  "startTime": "2025-12-25T12:00:00",
  "endTime": "2025-12-25T20:00:00"
}
```
**Powers**: Admin can modify availability times
**Response**: Updated availability details

### Delete Availability Slot
```http
DELETE /admin/availability/{id}
```
**Protection**: Cannot delete if booked (must cancel booking first)
**Response**: 204 No Content

---

## 🔐 Security Configuration

Admin powers are enforced through:

1. **Spring Security Configuration** ([SecurityConfig.java](src/main/java/com/gazi/ParkUs/config/SecurityConfig.java))
   ```java
   .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
   ```

2. **Service-Level Enforcement** - All admin operations in `AdminService` bypass ownership checks

3. **JWT Token Validation** - Role is encoded in JWT and verified on each request

---

## 🎯 Admin Capabilities Summary

### ✅ Admin CAN:
- View all users, bookings, spots, and availability
- Create resources for any user
- Modify any resource regardless of ownership
- Delete any resource (with some protections)
- Change user roles
- Update booking status to any state
- Change booking ownership
- Force-cancel any booking
- Change parking spot ownership
- Create/modify availability for any spot

### ⚠️ Protections (Even for Admin):
- Cannot delete booked availability (must cancel booking first)
- Password changes still require valid password format
- All database constraints still apply (foreign keys, etc.)

---

## 📊 Response Formats

### Success Responses
- `200 OK`: Successful GET/POST/PUT/PATCH with body
- `204 No Content`: Successful DELETE or update without body

### Error Responses
```json
{
  "statusCode": 404,
  "message": "Resource not found",
  "timestamp": "2025-12-21T10:30:00"
}
```

Common status codes:
- `400`: Bad Request (validation error)
- `401`: Unauthorized (not logged in or invalid token)
- `403`: Forbidden (not admin)
- `404`: Not Found
- `409`: Conflict (e.g., booking conflict)

---

## 🧪 Testing Admin Endpoints

### Step 1: Login as Admin
```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@parkus.com",
  "password": "admin123"
}
```

### Step 2: Extract Token
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "ROLE_ADMIN"
}
```

### Step 3: Use Token in Admin Requests
```http
GET /admin/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📝 Notes

1. **Pagination**: All list endpoints support `page`, `size`, and `sort` query parameters
2. **Soft Deletes**: Currently using hard deletes - consider implementing soft deletes for audit trails
3. **Audit Logging**: BookingLog tracks booking changes automatically
4. **Validation**: All DTOs have validation constraints - invalid data returns 400
5. **CORS**: Configured for localhost development - update for production

---

## 🔄 Recent Changes

- Added comprehensive CRUD operations for all resources
- Implemented admin-specific service layer (`AdminService`)
- Added ability to change resource ownership
- Enabled force operations (bypassing normal business rules)
- Added bulk management capabilities
- Integrated with existing security configuration
