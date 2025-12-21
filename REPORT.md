# ParkUs Project - Database Analysis Report

**Project**: ParkUs - A Parking Spot Sharing Platform  
**Database System**: PostgreSQL  
**Date**: December 21, 2025  
**ORM Framework**: Hibernate/JPA (Spring Boot)

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Entity-Relationship Diagram](#entity-relationship-diagram)
3. [Database Functionalities](#database-functionalities)
4. [Schema Design & Architecture](#schema-design--architecture)
5. [Strengths of the Database Design](#strengths-of-the-database-design)
6. [Areas for Improvement](#areas-for-improvement)
7. [Performance Considerations](#performance-considerations)
8. [Recommendations](#recommendations)

---

## Database Overview

The ParkUs database is designed to support a peer-to-peer parking spot sharing platform where users can:
- **List** their parking spaces for rent
- **Book** available parking spots
- **Rate** parking spots
- **Manage** bookings and availability windows
- Track booking history for administrative purposes

### Key Statistics:
- **Total Tables**: 7 core tables
- **Primary Database**: PostgreSQL
- **User Roles**: Regular Users (renters/owners), Admin
- **Booking States**: Pending, Confirmed, Cancelled, Completed

---

## Entity-Relationship Diagram

```
┌─────────────────────┐
│     UserEntity      │ (Abstract base class, Single Table Inheritance)
├─────────────────────┤
│ user_id (PK)        │
│ email (UNIQUE)      │
│ first_name          │
│ last_name           │
│ password_hash       │
│ role (ENUM)         │
│ created_at          │
│ user_type (Disc.)   │ ──→ RegularUser
└──────────┬──────────┘
           │
      ┌────┴──────┬──────────┐
      │            │          │
      │1           │N         │N
      │            │          │
      ▼            ▼          ▼
┌─────────────┐ ┌────────────────┐ ┌──────────────┐
│ParkingSpot  │ │BookingLog      │ │SpotRating    │
├─────────────┤ ├────────────────┤ ├──────────────┤
│ spot_id (PK)│ │ log_id (PK)    │ │rating_id(PK) │
│ owner_id(FK)│ │ booking_id(FK) │ │ spot_id (FK) │
│ title       │ │ spot_id (FK)   │ │ user_id (FK) │
│ description │ │ owner_id (FK)  │ │ rating       │
│ location    │ │ renter_id(FK)  │ │ comment      │
│ latitude    │ │ start_time     │ │ created_at   │
│ longitude   │ │ end_time       │ └──────────────┘
│ slot_type   │ │ duration_hours │
│price_per_hr │ │ total_amount   │
│ created_at  │ │ booking_status │
└──────┬──────┘ │ logged_at      │
       │        └────────────────┘
       │1
       │
       │N
       │
       ▼
┌──────────────────────┐
│ SpotAvailability     │
├──────────────────────┤
│availability_id (PK)  │
│ spot_id (FK)         │
│ start_time           │
│ end_time             │
│ is_booked            │
└──────────────────────┘
       │1
       │
       │N
       │
       ▼
┌──────────────────────┐
│ Booking              │
├──────────────────────┤
│ booking_id (PK)      │
│ spot_id (FK)         │
│ renter_id (FK)       │
│ availability_id (FK) │
│ booked_at            │
│ status               │
│ total_amount         │
└──────────────────────┘
```

---

## Database Functionalities

### 1. **User Management**

**Table**: `users`

**Purpose**: Stores all user information with role-based access control

**Functionality**:
- User registration with email-based authentication
- Password hashing for security
- Role-based distinctions: ADMIN, ROLE_USER
- Owner flag indicating if user can list parking spots
- Timestamp tracking for account creation

**Data Flow**:
```
User Registration → UserEntity (validated) → Database
                                              ↓
Authentication → Password verification → JWT Token Generation
```

**Key Constraints**:
- Email must be unique and follow valid format
- Password is hashed before storage
- First name, last name, and email are mandatory

---

### 2. **Parking Spot Management**

**Table**: `parkingspot`

**Purpose**: Core asset table storing all listed parking spots

**Functionality**:
- Spot owners can list multiple parking spaces
- Geo-location tracking using latitude/longitude (decimal precision: 6 places)
- Categorization by spot type (regular, EV, handicapped, etc.)
- Dynamic pricing per hour
- Text-based location description for user reference

**Data Structure**:
```
Owner (UserEntity) 
    ↓
    └─→ ParkingSpot (1:N)
            │
            ├─ Geo-coordinates (41.008238, 28.978359)
            ├─ Pricing ($12.00/hour)
            ├─ Type classification (EV, Handicapped, Regular)
            └─ Timestamps (created_at)
```

**Business Logic**:
- Price calculation: `total_amount = price_per_hour × booking_duration_hours`
- Geospatial querying enabled (distance-based search)
- Spot lifecycle: Create → Available → Booked → Archive

---

### 3. **Availability Management**

**Table**: `spotavailability`

**Purpose**: Manages time windows when parking spots are available for booking

**Functionality**:
- Creates time slots for each parking spot
- Tracks booking status (available/booked) per slot
- Supports complex scheduling (overlapping time windows possible)
- Pessimistic locking to prevent race conditions during booking

**Critical Operations**:
```
1. Create Availability Windows:
   Owner sets: start_time → end_time for a spot
   
2. Query Overlapping Slots:
   Check for availability conflicts before booking
   
3. Lock & Book:
   Apply pessimistic write lock
   Mark is_booked = true
   Prevent double-booking
```

**Locking Strategy**:
- Uses `LockModeType.PESSIMISTIC_WRITE` to prevent race conditions
- Acquires database-level locks during booking process
- Ensures atomic transaction for availability updates

---

### 4. **Booking System**

**Table**: `booking`

**Purpose**: Records all parking spot reservations and rental agreements

**Functionality**:
- Links renters to available parking spots
- Status tracking: pending → confirmed → completed/cancelled
- Financial tracking with total amount calculation
- Transaction-level operations ensure consistency

**Booking Workflow**:
```
1. Renter requests booking
   ↓
2. System validates:
   - Is spot available?
   - Is time in future?
   - Is duration valid (≥ 1 hour)?
   ↓
3. Calculate total_amount = price_per_hour × duration
   ↓
4. Create booking record (status = 'pending')
   ↓
5. Lock availability and set is_booked = true
   ↓
6. Auto-confirm booking (status = 'confirmed')
   ↓
7. Trigger log_booking() function
   ↓
8. Create BookingLog record
```

**Status States**:
- **pending**: Awaiting confirmation
- **confirmed**: Accepted and active
- **cancelled**: User cancelled
- **completed**: Time window has passed

---

### 5. **Booking Audit Log**

**Table**: `bookinglog`

**Purpose**: Maintains a permanent audit trail of all confirmed bookings

**Functionality**:
- Automatic logging of confirmed bookings via PostgreSQL trigger
- Records complete financial and temporal data
- Supports business intelligence and revenue tracking
- Enables historical analysis and dispute resolution

**Trigger Logic** (`log_booking()`):
```sql
TRIGGER: trg_log_booking
EVENT: AFTER UPDATE ON Booking
CONDITION: When status changes to 'confirmed'

ACTION:
  ├─ Extract booking_id
  ├─ Extract owner_id from associated ParkingSpot
  ├─ Extract renter_id
  ├─ Calculate duration_hours from timestamps
  ├─ Record total_amount
  ├─ Record status
  └─ Set logged_at = CURRENT_TIMESTAMP
```

**Use Cases**:
- Revenue reporting per owner
- Historical booking queries by date range
- Renter booking history verification
- Dispute resolution with timestamped records

---

### 6. **Rating & Review System**

**Table**: `spotrating`

**Purpose**: Enables users to rate and review parking spots

**Functionality**:
- 5-star rating system (1-5 validated range)
- Optional comment field for detailed feedback
- Unique constraint ensures one rating per user per spot
- Tracks creation time for review ordering

**Constraint Details**:
```
UNIQUE(spot_id, user_id) 
→ Prevents duplicate ratings from same user
```

**Rating Features**:
- Star rating: 1 (worst) to 5 (best)
- Text comment: Up to TEXT field capacity
- Timestamp: When review was posted
- Example: "Very convenient spot, easy to park!" (5★)

---

## Schema Design & Architecture

### Database Normalization

**Normalization Level**: Approximately 3NF (Third Normal Form)

**Analysis**:

✅ **First Normal Form (1NF)**: SATISFIED
- All attributes contain atomic values
- No repeating groups
- Each cell contains single value

✅ **Second Normal Form (2NF)**: SATISFIED
- All non-key attributes fully depend on entire primary key
- No partial dependencies
- Example: `booking.total_amount` depends on complete booking_id

✅ **Third Normal Form (3NF)**: MOSTLY SATISFIED
- No transitive dependencies on non-key attributes
- Note: `BookingLog` denormalizes data for audit purposes (intentional)
- Justified for performance and historical record-keeping

**Denormalization Rationale**:
- `BookingLog` duplicates data from `Booking` and `ParkingSpot`
- Intentional design for immutable audit trail
- Prevents updates to historical records
- Improves query performance for reporting

---

### Key Design Patterns

#### 1. **Single Table Inheritance (STI)**
```
UserEntity (abstract base)
└─ RegularUser (discriminator value: "RegularUser")
└─ Admin (potential: discriminator value: "Admin")
```

**Advantages**:
- Unified user management
- Polymorphic queries possible
- Single authentication table

**Trade-offs**:
- NULL columns for unused fields
- Larger table size
- Limited extensibility for role-specific attributes

#### 2. **Pessimistic Locking for Concurrency**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
SELECT a FROM SpotAvailability a WHERE a.availabilityId = :id
```

**Purpose**: Prevent race conditions in high-concurrency scenarios
- Acquires exclusive database lock
- Serializes booking attempts
- Eliminates double-booking scenarios

---

### Indexes & Performance Optimization

**Existing Indexes**:
```sql
CREATE INDEX idx_booking_renter ON booking(renter_id);
CREATE INDEX idx_booking_owner ON booking(spot_id);
CREATE INDEX idx_log_time ON bookinglog(logged_at);
```

**Index Coverage**:
- Fast lookup by renter ID
- Fast lookup by spot ID (owner's perspective)
- Fast range queries on booking logs by timestamp

---

## Strengths of the Database Design

### ✅ 1. **Excellent Concurrency Control**

**Strength**: Uses pessimistic locking to prevent race conditions
- Double-booking is impossible
- Availability slots are atomic operations
- High data integrity in concurrent scenarios

**Example Implementation**:
```java
SpotAvailability availability = availabilityRepo.lockById(dto.getAvailabilityId())
    .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

if (Boolean.TRUE.equals(availability.getIsBooked())) {
    throw new BookingConflictException("This time slot is already booked");
}
```

### ✅ 2. **Automatic Audit Trail with Triggers**

**Strength**: PostgreSQL trigger automatically logs confirmed bookings
- No manual logging required
- Guaranteed consistency
- Tamper-proof historical records
- Decoupled from application logic

**Benefit**: Can query revenue data without impacting transactional tables

### ✅ 3. **Geospatial Query Support**

**Strength**: Latitude/longitude coordinates enable distance-based searches
```sql
SELECT p FROM ParkingSpot p
WHERE p.latitude BETWEEN :latMin AND :latMax
  AND p.longitude BETWEEN :lngMin AND :lngMax
```

**Use Case**: "Find parking spots within 1km of my location"

### ✅ 4. **Role-Based Authorization Integration**

**Strength**: Database schema supports admin/user segregation
- Admin can view all bookings
- Users can only see their own data
- Enforced at both database and application levels

### ✅ 5. **Financial Data Integrity**

**Strength**: Immutable booking records with precise calculations
- Price calculation happens at booking time
- Total amount stored (no recalculation risk)
- Duration calculated in trigger (single source of truth)

### ✅ 6. **Flexible Spot Categorization**

**Strength**: `slot_type` field allows for future expansion
- Current types: regular, EV, handicapped
- Easily extensible without schema changes
- Can be indexed for filtered searches

### ✅ 7. **Timestamp-based Auditing**

**Strength**: All tables include creation timestamps
- Track data lifecycle
- Detect stale records
- Enable time-based analytics

---

## Areas for Improvement

### ⚠️ 1. **Missing Foreign Key Constraints in BookingLog**

**Current Issue**:
```sql
CREATE TABLE BookingLog (
    log_id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL,           -- ❌ No FK constraint
    spot_id INT NOT NULL,              -- ❌ No FK constraint
    owner_id INT NOT NULL,             -- ❌ No FK constraint
    renter_id INT NOT NULL,            -- ❌ No FK constraint
    ...
);
```

**Problem**:
- Orphaned records possible if referenced records are deleted
- No referential integrity enforcement
- Data consistency not guaranteed at database level

**Recommendation**:
```sql
ALTER TABLE BookingLog ADD CONSTRAINT fk_bookinglog_booking
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE RESTRICT;

ALTER TABLE BookingLog ADD CONSTRAINT fk_bookinglog_owner
    FOREIGN KEY (owner_id) REFERENCES Users(user_id) ON DELETE RESTRICT;

ALTER TABLE BookingLog ADD CONSTRAINT fk_bookinglog_renter
    FOREIGN KEY (renter_id) REFERENCES Users(user_id) ON DELETE RESTRICT;
```

**Impact**: Medium Priority (Audit table, but referential integrity is important)

---

### ⚠️ 2. **No Explicit Check Constraints**

**Current Issue**:
```sql
CREATE TABLE ParkingSpot (
    price_per_hour NUMERIC(10,2) NOT NULL,  -- ❌ No check for positive values
    ...
);

CREATE TABLE Booking (
    total_amount NUMERIC(10,2) NOT NULL,    -- ❌ Can be negative or zero
    ...
);
```

**Problem**:
- Negative prices could be inserted (though prevented by application)
- No database-level business rule enforcement
- Application layer bugs could corrupt data

**Recommendation**:
```sql
ALTER TABLE ParkingSpot 
ADD CONSTRAINT check_price_positive CHECK (price_per_hour > 0);

ALTER TABLE Booking 
ADD CONSTRAINT check_amount_positive CHECK (total_amount > 0);

ALTER TABLE SpotRating 
ADD CONSTRAINT check_rating_range CHECK (rating >= 1 AND rating <= 5);
```

**Impact**: Medium Priority (Data quality)

---

### ⚠️ 3. **No Explicit Temporal Constraints**

**Current Issue**:
```sql
CREATE TABLE SpotAvailability (
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
    -- ❌ No check that end_time > start_time
);
```

**Problem**:
- Invalid time windows could be created (end before start)
- Application must validate, but database doesn't enforce

**Recommendation**:
```sql
ALTER TABLE SpotAvailability 
ADD CONSTRAINT check_time_order CHECK (end_time > start_time);

-- Also beneficial for Booking:
ALTER TABLE Booking
ADD CONSTRAINT check_booking_time CHECK (booked_at <= CURRENT_TIMESTAMP);
```

**Impact**: Low-Medium Priority (Application validation exists)

---

### ⚠️ 4. **Incomplete Schema Documentation**

**Current Issue**:
- No table or column comments
- No constraint documentation
- Trigger logic documented only in SQL file

**Problem**:
- Future developers must reverse-engineer design
- Maintenance becomes difficult
- Business logic embedded in trigger not immediately obvious

**Recommendation**:
```sql
COMMENT ON TABLE users IS 'Core user entity - supports both parking spot owners and renters';
COMMENT ON COLUMN users.role IS 'User role enum: ADMIN, ROLE_USER';
COMMENT ON TABLE spotavailability IS 'Time windows during which parking spots are available for booking';
COMMENT ON TABLE bookinglog IS 'Immutable audit trail of confirmed bookings, populated by trigger';
```

**Impact**: Low Priority (Good practice but not critical)

---

### ⚠️ 5. **No Password Expiration or Security Policies**

**Current Issue**:
- No password last_changed timestamp
- No failed login attempt tracking
- No account lockout mechanism

**Problem**:
- Stale passwords never expire
- Brute force attacks not prevented at DB level
- No security audit trail

**Recommendation**:
```sql
ALTER TABLE users ADD COLUMN password_last_changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
ALTER TABLE users ADD COLUMN failed_login_attempts INT DEFAULT 0;
ALTER TABLE users ADD COLUMN is_locked BOOLEAN DEFAULT FALSE;
```

**Impact**: Medium Priority (Important for security)

---

### ⚠️ 6. **Missing Soft Delete Support**

**Current Issue**:
```sql
CREATE TABLE users (
    ...
    -- ❌ No deleted_at or is_active flag
);
```

**Problem**:
- Deleting users cascades to all related data
- No way to deactivate accounts temporarily
- Historical data lost if user deletes account

**Recommendation**:
```sql
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP DEFAULT NULL;
ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- Then filter queries:
SELECT * FROM users WHERE deleted_at IS NULL;
```

**Impact**: Medium Priority (Better data retention)

---

### ⚠️ 7. **No Explicit Indexes for Common Queries**

**Missing Indexes**:
```sql
-- Fast location-based searches
CREATE INDEX idx_parkingspot_location ON parkingspot(latitude, longitude);

-- Fast owner-to-spot lookup
CREATE INDEX idx_parkingspot_owner ON parkingspot(owner_id);

-- Fast availability queries
CREATE INDEX idx_spotavailability_spot_time ON spotavailability(spot_id, start_time, end_time);

-- Fast booking status queries
CREATE INDEX idx_booking_status ON booking(status);

-- Fast rating lookups
CREATE INDEX idx_spotrating_spot ON spotrating(spot_id);
```

**Impact**: Medium Priority (Performance optimization)

---

### ⚠️ 8. **No Transaction Isolation Level Specification**

**Current Issue**:
- Default PostgreSQL isolation level used (Read Committed)
- Phantom reads possible in concurrent scenarios

**Problem**:
- Certain race conditions might still occur
- No explicit transaction boundaries documented

**Recommendation**:
```sql
-- For booking operations, use Serializable isolation:
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```

**Impact**: Low Priority (Current locking strategy adequate)

---

### ⚠️ 9. **No Data Retention Policy**

**Current Issue**:
- All historical data retained indefinitely
- No archival strategy for old bookings
- Database grows unbounded

**Problem**:
- Query performance degrades over time
- Storage costs increase
- GDPR compliance (right to be forgotten)

**Recommendation**:
```sql
-- Archive bookings older than 1 year
CREATE TABLE booking_archive AS 
SELECT * FROM booking WHERE booked_at < CURRENT_DATE - INTERVAL '1 year';

DELETE FROM booking WHERE booked_at < CURRENT_DATE - INTERVAL '1 year';
```

**Impact**: Low Priority (Concerns future, not current state)

---

### ⚠️ 10. **Ambiguous Booking Status Semantics**

**Current Issue**:
- Status values are strings: 'pending', 'confirmed', 'cancelled', 'completed'
- No ENUM type used in schema
- Invalid status values could be inserted

**Problem**:
- Typos possible (e.g., 'confirmd' instead of 'confirmed')
- No compile-time validation
- Business logic scattered across application

**Recommendation**:
```sql
-- Create ENUM type in PostgreSQL
CREATE TYPE booking_status AS ENUM ('pending', 'confirmed', 'cancelled', 'completed');

-- Alter table to use ENUM
ALTER TABLE booking MODIFY status booking_status DEFAULT 'pending';
```

**Impact**: Medium Priority (Data quality)

---

## Performance Considerations

### Query Performance Analysis

#### 1. **Spot Availability Queries**
```java
List<SpotAvailability> findBySpot_SpotIdAndIsBookedFalse(Long spotId);
```
**Status**: ✅ Good - Indexed by spot_id
**Potential**: Index on (spot_id, is_booked) for faster filtering

#### 2. **Booking History Queries**
```java
List<BookingLog> findLogsBetween(LocalDateTime from, LocalDateTime to);
```
**Status**: ✅ Good - Indexed on logged_at
**Performance**: O(log n) index range scan

#### 3. **Overlapping Availability Queries**
```java
List<SpotAvailability> findOverlapping(
    Long spotId, 
    LocalDateTime startTime, 
    LocalDateTime endTime
);
```
**Status**: ⚠️ Could be improved
**Issue**: No composite index on (spot_id, start_time, end_time)
**Cost**: O(n) table scan worse as data grows

#### 4. **Geospatial Queries**
```java
List<ParkingSpot> findNearby(
    BigDecimal latMin, BigDecimal latMax,
    BigDecimal lngMin, BigDecimal lngMax
);
```
**Status**: ⚠️ Suboptimal
**Issue**: Range queries on two separate columns
**Improvement**: Consider PostGIS extension for true geospatial indexing

---

### Lock Contention Analysis

**Potential Issue**: High-demand parking spots may experience lock contention
```
Multiple concurrent booking requests → All acquire lock on same availability → Serialized → Slower response times
```

**Mitigation Strategy**:
- Accept waiting times for popular spots
- Implement queue-based booking for hot spots
- Monitor lock wait times in production

---

## Recommendations

### Priority 1: Critical (Implement Immediately)

1. **Add FK Constraints to BookingLog**
   - Ensures referential integrity
   - Estimated effort: 1 hour
   - SQL provided in Improvement #1

2. **Add CHECK Constraints for Business Rules**
   - Positive prices, amounts
   - Valid rating ranges
   - Temporal ordering
   - Estimated effort: 2 hours
   - SQL provided in Improvements #2-3

3. **Implement Soft Delete for Users**
   - Preserve historical data
   - Estimated effort: 3 hours
   - Requires application code changes

---

### Priority 2: Important (Implement within next sprint)

4. **Add Geospatial Indexing**
   - Optimize location-based searches
   - Consider PostGIS if advanced geo-features needed
   - Estimated effort: 4 hours

5. **Convert Status Strings to ENUMs**
   - Enforce valid status values
   - Estimated effort: 2 hours
   - Migration required for existing data

6. **Add Security Columns**
   - Password last changed
   - Account lockout tracking
   - Estimated effort: 3 hours

---

### Priority 3: Improvements (Nice to have)

7. **Add Table Comments**
   - Improve documentation
   - Estimated effort: 2 hours

8. **Implement Partitioning Strategy**
   - Partition booking/bookinglog by date
   - Improves query performance at scale
   - Estimated effort: 6 hours

9. **Create Data Retention Policy**
   - Archive old bookings
   - Estimated effort: 4 hours

10. **Monitor and Tune Indexes**
    - Regular index maintenance
    - Estimated effort: Ongoing

---

## Conclusion

### Overall Assessment: **8.5/10**

The ParkUs database demonstrates solid architectural fundamentals with excellent concurrency control and audit trail capabilities. The use of triggers for automatic logging and pessimistic locking for preventing race conditions shows thoughtful design.

### Key Strengths:
- ✅ Strong concurrency control preventing double-booking
- ✅ Automated audit trail with triggers
- ✅ Geospatial query support
- ✅ Role-based authorization foundation
- ✅ Financial data integrity

### Key Areas for Enhancement:
- ⚠️ Add referential integrity constraints to BookingLog
- ⚠️ Implement business rule constraints (CHECK)
- ⚠️ Add security-related columns and policies
- ⚠️ Optimize indexing strategy
- ⚠️ Convert status to ENUM type

### Estimated Effort to Address All Recommendations:
- **Priority 1**: 6 hours
- **Priority 2**: 12 hours
- **Priority 3**: 12 hours
- **Total**: ~30 hours of development time

### Next Steps:
1. Begin with Priority 1 recommendations immediately
2. Plan Priority 2 work for next development sprint
3. Conduct performance testing at 10,000+ booking scale
4. Consider PostGIS migration for advanced geospatial features
5. Implement monitoring and index optimization pipeline

---

**Report Generated**: December 21, 2025  
**Database Version**: PostgreSQL (version unspecified in config)  
**Application Framework**: Spring Boot with Hibernate JPA
