# ParkUs - Parking Rental Platform

## Latest Changes (2025-12-21)

- Admin list endpoints now support pagination and default sorting (users by registrationDate desc, bookings by bookedAt desc, spots by createdAt desc, availability by startTime asc).
- JWT filter now returns 401 immediately on invalid tokens instead of silently continuing.
- JWT secret length validated at startup (minimum 256 bits); uses validated bytes for signing.
- Removed DB_URL logging from env loader to avoid leaking credentials.
- Generic 500 responses are now generic; full stack traces are logged server-side only.

## Recent Improvements & Fixes

All critical issues have been resolved. Here's a comprehensive summary of the changes:

---

## 🔐 Security Improvements

### 1. **JWT Authentication Implementation**
- **Added JWT dependencies** (jjwt-api, jjwt-impl, jjwt-jackson v0.12.5)
- **Created `JwtUtil`** for token generation and validation
- **Created `JwtAuthenticationFilter`** to intercept requests and validate tokens
- **Updated `SecurityConfig`** to use JWT instead of HTTP Basic Auth
- **Modified auth endpoints** to return JWT tokens with user information

**How to use:**
1. Register: `POST /auth/register` returns JWT token
2. Login: `POST /auth/login` returns JWT token
3. Include token in subsequent requests: `Authorization: Bearer <token>`

### 2. **Proper Authorization**
- All booking endpoints now verify the authenticated user
- Users can only:
  - Create bookings for themselves
  - View their own bookings
  - Update/delete their own bookings
- Spot owners can:
  - View bookings on their spots
  - Update booking status
- All operations validate ownership before allowing modifications

---

## 🛡️ Exception Handling

### Created Custom Exception Classes:
- `ResourceNotFoundException` - For missing entities (404)
- `UnauthorizedException` - For authentication failures (401)
- `BookingConflictException` - For booking conflicts (409)
- `InvalidRequestException` - For validation errors (400)
- `UserAlreadyExists` - For duplicate registrations (409)

### Global Exception Handler:
- Consistent error responses across all endpoints
- Proper HTTP status codes
- Detailed error messages
- Validation error handling with field-level details

---

## ✅ Input Validation

### Added Validation to DTOs:

**RegisterUserDto & LoginUserDto:**
- Email format validation
- Required field validation
- Password minimum length (8 characters)

**ParkingSpotRequestDto:**
- Title, latitude, longitude required
- Coordinate range validation (-90 to 90 for lat, -180 to 180 for long)
- Price must be greater than 0
- Description max length: 500 chars

**BookingRequestDto:**
- Availability ID and Renter ID required

**SpotAvailabilityRequestDto:**
- Start and end times required
- Times must be in the future

---

## 🔧 Business Logic Fixes

### Booking Race Condition Fixed:
- Uses JPA's transaction locking
- Checks availability status within transaction
- Prevents double-booking
- Validates time windows before creating bookings

### Enhanced Booking Validation:
- Cannot book past time slots
- Validates end time > start time
- Minimum 1 hour duration
- Status validation (pending, confirmed, cancelled, completed)
- Authorization checks for all operations

### UserEntity Improvements:
- Fixed field naming (`userId` instead of `id`)
- Email regex validation
- Improved validation for first/last names
- Fixed password validation (doesn't check hashed passwords)

---

## 🆕 New Features

### Availability Management System:
Complete CRUD for parking spot availability:

**Endpoints:**
- `POST /api/availability` - Create availability slot
- `GET /api/availability/{id}` - Get specific availability
- `GET /api/availability/spot/{spotId}` - Get all availability for a spot
- `GET /api/availability/spot/{spotId}/available` - Get only available (unbooked) slots
- `DELETE /api/availability/{id}` - Delete availability (if not booked)

**Features:**
- Validates time overlaps
- Only owners can create/delete availability
- Cannot delete booked slots
- Filters future availability

---

## 📝 Configuration

### application.properties additions:
```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

Default expiration: 24 hours (86400000 ms)

---

## 🎯 API Changes

### Authentication Flow:
```
1. Register/Login → Receive JWT token
2. Store token client-side
3. Include in all requests: Authorization: Bearer <token>
```

### Protected Endpoints:
- `/auth/register`, `/auth/login` - Public
- `/api/**` - Requires JWT authentication
- `/admin/**` - Requires ROLE_ADMIN

---

## 🐛 Bug Fixes

1. **Password validation**: No longer checks hashed password length
2. **Exception handling**: Replaced generic RuntimeException with specific exceptions
3. **Authorization**: Added proper ownership checks throughout
4. **Validation**: Added comprehensive input validation
5. **Database mapping**: Fixed field name inconsistencies (id → userId)
6. **Email validation**: Added regex pattern validation
7. **Booking deletion**: Now frees up availability slot
8. **ParkingSpotResponseDto**: Fixed slotType mapping bug

---

## 📊 Database Schema Compatibility

The JPA entities now properly map to your PostgreSQL schema:
- Table names match (users, parkingspot, booking, etc.)
- Column names aligned (user_id, password_hash, etc.)
- Using BIGINT for all IDs
- Proper relationships maintained

---

## 🚀 Next Steps (Optional Improvements)

While all critical issues are fixed, consider these enhancements:

1. **Payment Integration**: Add payment processing for bookings
2. **Search by Location**: Implement geospatial queries to find nearby spots
3. **Rating System**: Implement the SpotRating entity endpoints
4. **Notification System**: Email/SMS notifications for bookings
5. **Admin Panel**: Complete admin endpoints for system management
6. **Refresh Tokens**: Implement token refresh mechanism
7. **Password Reset**: Add forgot password functionality
8. **File Upload**: Add photos for parking spots
9. **Cancellation Policy**: Define and implement cancellation rules
10. **Pagination**: Add pagination for listing endpoints

---

## 🧪 Testing

To test the system:

1. **Register a user:**
```bash
POST /auth/register
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

2. **Use returned token** in Authorization header for all subsequent requests

3. **Create a parking spot:**
```bash
POST /api/spots
Authorization: Bearer <token>
{
  "title": "Downtown Parking",
  "description": "Convenient spot",
  "location": "123 Main St",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "regular",
  "pricePerHour": 5.00
}
```

4. **Create availability:**
```bash
POST /api/availability
Authorization: Bearer <token>
{
  "spotId": 1,
  "startTime": "2025-12-22T10:00:00",
  "endTime": "2025-12-22T18:00:00"
}
```

5. **Book a spot** (as different user)

---

## 📌 Important Notes

- **JWT Secret**: Change the default secret in production via environment variable
- **Validation**: All DTOs now have proper validation annotations
- **Security**: JWT tokens expire after 24 hours by default
- **Database**: Ensure your PostgreSQL schema matches the DDL.sql
- **Testing**: Use tools like Postman to test with JWT tokens

---

## ✨ Summary

Your ParkUs application now has:
- ✅ Secure JWT authentication
- ✅ Proper authorization on all endpoints
- ✅ Comprehensive error handling
- ✅ Input validation
- ✅ Race condition prevention
- ✅ Complete availability management
- ✅ Business logic validation
- ✅ Clean exception hierarchy

The application is now production-ready from a security and architecture standpoint!
