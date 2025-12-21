# Admin App Implementation Prompt

## 🎯 Project Overview
Build a comprehensive Admin Dashboard for the ParkUs parking management system. The dashboard should provide full CRUD operations for managing users, bookings, parking spots, and availability slots.

---

## 📋 Requirements

### Technology Stack
**Choose one of:**
- **Web**: React/Next.js + TypeScript + Tailwind CSS + shadcn/ui
- **Mobile**: React Native + TypeScript + NativeBase/Tamagui
- **Flutter**: Dart + Flutter Material/Cupertino

### Backend API
- Base URL: `http://localhost:8080` (or your deployed backend)
- Authentication: JWT Bearer Token
- All admin endpoints require `ROLE_ADMIN` authority

---

## 🏗️ App Structure

### 1. Authentication Module
```
Features:
- Login screen with email/password
- JWT token storage (secure storage/cookies)
- Auto-login if token is valid
- Logout functionality
- Role verification (must be ROLE_ADMIN)
```

**API Endpoint:**
```
POST /auth/login
{
  "email": "admin@parkus.com",
  "password": "adminpassword"
}

Response:
{
  "token": "eyJhbGc...",
  "email": "admin@parkus.com",
  "role": "ROLE_ADMIN",
  "firstName": "Admin",
  "lastName": "User"
}
```

### 2. Dashboard Home
```
Features:
- Statistics cards (total users, bookings, spots, revenue)
- Recent bookings list
- Quick actions menu
- Navigation to all management sections
```

**API Endpoints for Stats:**
```
GET /admin/users?size=1          // Get total count from pagination
GET /admin/bookings?size=1       // Get total count
GET /admin/spots?size=1          // Get total count
GET /admin/bookings/status/confirmed  // Active bookings
```

---

## 👥 3. User Management Module

### 3.1 User List Screen
**Features:**
- Paginated table/list of all users
- Search by name/email
- Filter by role (USER/ADMIN)
- Sort by registration date
- Actions: View, Edit, Delete, Change Role

**API:**
```
GET /admin/users?page=0&size=20&sort=registrationDate,desc
Authorization: Bearer {token}
```

**UI Components:**
- Search bar
- Filter dropdown (Role)
- Data table with columns: ID, Name, Email, Role, Registration Date, Actions
- Pagination controls
- "Add User" button (optional)

### 3.2 User Detail/Edit Screen
**Features:**
- View user details
- Edit first name, last name, email
- Change password (optional field)
- Change role (dropdown: USER/ADMIN)
- Delete user (with confirmation)

**APIs:**
```
GET /admin/users/{id}
PATCH /admin/users/{id}
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "newpassword123"  // optional
}

PATCH /admin/users/{id}/role?role=ROLE_ADMIN
DELETE /admin/users/{id}
```

**UI Components:**
- Form with input fields
- Role dropdown/toggle
- Save button
- Delete button (with confirmation dialog)
- Cancel button

---

## 📅 4. Booking Management Module

### 4.1 Booking List Screen
**Features:**
- Paginated table/list of all bookings
- Filter by status (pending, confirmed, cancelled, completed)
- Filter by date range
- Sort by booking date
- Search by booking ID or user
- Color coding by status
- Actions: View, Edit Status, Edit Booking, Delete

**API:**
```
GET /admin/bookings?page=0&size=20&sort=bookedAt,desc
GET /admin/bookings/status/{status}
```

**UI Components:**
- Status filter chips/dropdown
- Date range picker
- Search bar
- Data table with columns: Booking ID, Renter, Spot, Start Time, End Time, Amount, Status, Actions
- Status badges (different colors)
- Pagination controls

### 4.2 Booking Detail/Edit Screen
**Features:**
- View full booking details
- Edit renter (dropdown of users)
- Edit availability slot (dropdown of available slots)
- Update status (dropdown: pending, confirmed, cancelled, completed)
- View associated spot details
- Delete booking

**APIs:**
```
GET /admin/bookings/{id}
PATCH /admin/bookings/{id}
{
  "availabilityId": 789,
  "renterId": 456
}

PATCH /admin/bookings/{id}/status?status=confirmed
DELETE /admin/bookings/{id}
```

**UI Components:**
- Readonly fields (Booking ID, Booked At, Total Amount)
- Editable dropdown (Renter)
- Editable dropdown (Availability Slot - show times)
- Status dropdown
- Save button
- Delete button (with confirmation)

### 4.3 Create Booking Screen (Optional)
**Features:**
- Select renter (user dropdown)
- Select spot (spot dropdown)
- Select availability slot for that spot

**API:**
```
POST /admin/bookings
{
  "availabilityId": 123,
  "renterId": 456
}
```

---

## 🅿️ 5. Parking Spot Management Module

### 5.1 Parking Spot List Screen
**Features:**
- Paginated grid/list of parking spots
- Search by title/location
- Filter by slot type (covered, open, etc.)
- Sort by creation date, price
- Map view (optional - show spots on map)
- Actions: View, Edit, Delete

**API:**
```
GET /admin/spots?page=0&size=20&sort=createdAt,desc
```

**UI Components:**
- Search bar
- Slot type filter
- Grid/List toggle
- Data cards/table with: Image (placeholder), Title, Location, Type, Price/hr, Owner, Actions
- Map view button (optional)
- "Add Spot" button
- Pagination controls

### 5.2 Parking Spot Detail/Edit Screen
**Features:**
- View/Edit all spot details
- Change owner (dropdown of users)
- View location on map
- Edit all fields (title, description, location, coordinates, type, price)
- Delete spot

**APIs:**
```
GET /admin/spots/{id}
PUT /admin/spots/{id}
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

DELETE /admin/spots/{id}
```

**UI Components:**
- Form with all fields
- Owner dropdown
- Slot type dropdown
- Map picker for coordinates (optional)
- Price input with validation (> 0)
- Save button
- Delete button (with confirmation)

### 5.3 Create Parking Spot Screen
**Features:**
- Form to create new spot
- Select owner (required for admin)
- All spot details
- Map picker for location

**API:**
```
POST /admin/spots
{
  "title": "New Spot",
  "description": "Description",
  "location": "Address",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "slotType": "covered",
  "pricePerHour": 5.50,
  "ownerId": 789
}
```

---

## ⏰ 6. Availability Management Module

### 6.1 Availability List Screen
**Features:**
- Paginated list of availability slots
- Filter by spot
- Filter by booked status
- Filter by date range
- Sort by start time
- Calendar view (optional)
- Actions: View, Edit, Delete

**API:**
```
GET /admin/availability?page=0&size=20&sort=startTime,asc
```

**UI Components:**
- Spot filter dropdown
- Booked status filter (All, Available, Booked)
- Date range picker
- Calendar view toggle (optional)
- Data table with: ID, Spot, Start Time, End Time, Status (Available/Booked), Actions
- "Add Availability" button
- Pagination controls

### 6.2 Availability Detail/Edit Screen
**Features:**
- Edit start time and end time
- View associated spot
- View booking if booked
- Delete availability (only if not booked)

**APIs:**
```
GET /admin/availability/{id}
PUT /admin/availability/{id}
{
  "spotId": 123,
  "startTime": "2025-12-25T12:00:00",
  "endTime": "2025-12-25T20:00:00"
}

DELETE /admin/availability/{id}
```

**UI Components:**
- DateTime pickers for start/end
- Readonly spot info
- Save button
- Delete button (disabled if booked, with tooltip)

### 6.3 Create Availability Screen
**Features:**
- Select spot
- Set start and end times
- Validation (end > start)

**API:**
```
POST /admin/availability
{
  "spotId": 123,
  "startTime": "2025-12-25T10:00:00",
  "endTime": "2025-12-25T18:00:00"
}
```

---

## 🎨 UI/UX Requirements

### Design System
```
Colors:
- Primary: Blue (#3B82F6)
- Success: Green (#10B981)
- Warning: Yellow (#F59E0B)
- Danger: Red (#EF4444)
- Gray: Neutral (#6B7280)

Status Colors:
- Pending: Yellow
- Confirmed: Green
- Cancelled: Red
- Completed: Blue
```

### Common Components Needed
1. **Data Table**
   - Sortable columns
   - Pagination
   - Row actions menu
   - Loading states
   - Empty states

2. **Forms**
   - Input validation
   - Error messages
   - Loading states
   - Success feedback

3. **Confirmation Dialogs**
   - For delete operations
   - Warning messages
   - Confirm/Cancel buttons

4. **Navigation**
   - Sidebar/Drawer with sections
   - Top bar with user info and logout
   - Breadcrumbs (optional)

5. **Status Badges**
   - Colored pills for status
   - Icons for visual feedback

6. **Loading States**
   - Skeleton screens
   - Spinners
   - Progress indicators

7. **Notifications/Toasts**
   - Success messages
   - Error messages
   - Info messages

---

## 🔐 Security Implementation

### 1. Token Management
```javascript
// Store token securely
const storeToken = (token) => {
  // Web: localStorage or httpOnly cookies
  // Mobile: SecureStore or Keychain
  localStorage.setItem('authToken', token);
};

// Add token to all requests
const apiCall = async (endpoint, options = {}) => {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers,
    },
  });
  
  if (response.status === 401) {
    // Token expired, redirect to login
    redirectToLogin();
  }
  
  return response;
};
```

### 2. Role Protection
```javascript
// Check if user is admin
const isAdmin = (userRole) => {
  return userRole === 'ROLE_ADMIN';
};

// Protect routes
const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  
  if (!user || !isAdmin(user.role)) {
    return <Navigate to="/login" />;
  }
  
  return children;
};
```

### 3. Error Handling
```javascript
// Handle API errors gracefully
const handleApiError = (error) => {
  if (error.status === 401) {
    showNotification('Session expired. Please login again.', 'error');
    redirectToLogin();
  } else if (error.status === 403) {
    showNotification('You do not have permission.', 'error');
  } else if (error.status === 404) {
    showNotification('Resource not found.', 'error');
  } else {
    showNotification(error.message || 'Something went wrong.', 'error');
  }
};
```

---

## 📱 Screen Flow

```
Login Screen
    ↓
Dashboard Home
    ├─→ Users Management
    │   ├─→ User List
    │   └─→ User Detail/Edit
    │
    ├─→ Bookings Management
    │   ├─→ Booking List
    │   ├─→ Booking Detail/Edit
    │   └─→ Create Booking
    │
    ├─→ Parking Spots Management
    │   ├─→ Spot List
    │   ├─→ Spot Detail/Edit
    │   └─→ Create Spot
    │
    └─→ Availability Management
        ├─→ Availability List
        ├─→ Availability Detail/Edit
        └─→ Create Availability
```

---

## 🧪 Testing Checklist

### Authentication
- [ ] Login with admin credentials
- [ ] Login with non-admin (should fail)
- [ ] Token persistence across app restarts
- [ ] Logout clears token
- [ ] Expired token redirects to login

### User Management
- [ ] View paginated user list
- [ ] Search users by name/email
- [ ] Filter users by role
- [ ] View user details
- [ ] Update user information
- [ ] Change user role
- [ ] Delete user
- [ ] Validation on form inputs

### Booking Management
- [ ] View paginated booking list
- [ ] Filter bookings by status
- [ ] View booking details
- [ ] Update booking renter
- [ ] Update booking availability
- [ ] Change booking status
- [ ] Delete booking
- [ ] Create new booking

### Parking Spot Management
- [ ] View paginated spot list
- [ ] Search spots by title/location
- [ ] View spot details
- [ ] Create new spot with owner
- [ ] Update spot details
- [ ] Change spot owner
- [ ] Delete spot

### Availability Management
- [ ] View availability list
- [ ] Filter by spot and date
- [ ] Create new availability
- [ ] Update availability times
- [ ] Delete availability (not booked)
- [ ] Cannot delete booked availability

### Error Handling
- [ ] Network errors show notification
- [ ] 401 redirects to login
- [ ] 403 shows permission error
- [ ] 404 shows not found error
- [ ] Form validation errors display correctly

---

## 🚀 Implementation Steps

### Phase 1: Setup & Authentication (Day 1)
1. Set up project with chosen framework
2. Install dependencies (UI library, state management, routing)
3. Create API service layer
4. Implement login screen
5. Implement token storage and API interceptors
6. Create protected route wrapper

### Phase 2: Dashboard & Navigation (Day 1-2)
1. Create main layout with sidebar/drawer
2. Implement dashboard home with stats
3. Create navigation structure
4. Add user profile in header
5. Implement logout

### Phase 3: User Management (Day 2-3)
1. User list screen with pagination
2. User detail/edit screen
3. Form validation
4. Delete functionality
5. Role change functionality

### Phase 4: Booking Management (Day 3-4)
1. Booking list screen with filters
2. Booking detail/edit screen
3. Status update functionality
4. Delete functionality
5. Create booking screen (optional)

### Phase 5: Parking Spot Management (Day 4-5)
1. Spot list screen
2. Spot create screen
3. Spot edit screen
4. Owner management
5. Delete functionality

### Phase 6: Availability Management (Day 5-6)
1. Availability list screen
2. Create availability screen
3. Edit availability screen
4. Delete functionality
5. Calendar view (optional)

### Phase 7: Polish & Testing (Day 6-7)
1. Add loading states everywhere
2. Implement error handling
3. Add confirmation dialogs
4. Test all CRUD operations
5. Test error scenarios
6. Add notifications/toasts
7. Mobile responsive design
8. Accessibility improvements

---

## 💡 Pro Tips

1. **State Management**: Use React Query / TanStack Query for API calls and caching
2. **Forms**: Use React Hook Form or Formik for complex forms
3. **Validation**: Use Zod or Yup for schema validation
4. **Date Handling**: Use date-fns or dayjs for date formatting
5. **Tables**: Use TanStack Table for advanced data tables
6. **Notifications**: Use react-hot-toast or sonner for notifications
7. **Icons**: Use Lucide React or Heroicons
8. **Confirmation**: Create reusable confirmation dialog component
9. **Pagination**: Create reusable pagination component
10. **Error Boundaries**: Implement error boundaries for crash protection

---

## 📦 Suggested Dependencies

### React Web App
```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "@tanstack/react-query": "^5.0.0",
    "@tanstack/react-table": "^8.10.0",
    "react-hook-form": "^7.48.0",
    "zod": "^3.22.0",
    "date-fns": "^2.30.0",
    "axios": "^1.6.0",
    "react-hot-toast": "^2.4.0",
    "lucide-react": "^0.294.0",
    "tailwindcss": "^3.3.0",
    "@radix-ui/react-dialog": "^1.0.0",
    "@radix-ui/react-dropdown-menu": "^2.0.0",
    "@radix-ui/react-select": "^2.0.0"
  }
}
```

### React Native App
```json
{
  "dependencies": {
    "react-native": "^0.72.0",
    "@react-navigation/native": "^6.1.0",
    "@react-navigation/stack": "^6.3.0",
    "@tanstack/react-query": "^5.0.0",
    "react-hook-form": "^7.48.0",
    "zod": "^3.22.0",
    "axios": "^1.6.0",
    "react-native-paper": "^5.11.0",
    "expo-secure-store": "^12.5.0"
  }
}
```

---

## 🎓 Learning Resources

- **React**: https://react.dev
- **TanStack Query**: https://tanstack.com/query
- **TanStack Table**: https://tanstack.com/table
- **React Hook Form**: https://react-hook-form.com
- **Tailwind CSS**: https://tailwindcss.com
- **shadcn/ui**: https://ui.shadcn.com

---

## 📞 API Reference
See [ADMIN_POWERS.md](./ADMIN_POWERS.md) for complete API documentation with all endpoints, request/response formats, and examples.

---

## ✅ Deliverables

1. **Working Admin Dashboard** with all CRUD operations
2. **Responsive Design** (mobile-friendly)
3. **Error Handling** for all API calls
4. **Loading States** for better UX
5. **Confirmation Dialogs** for destructive actions
6. **Form Validation** on all inputs
7. **Secure Authentication** with JWT
8. **Clean Code** with proper component structure
9. **Documentation** for setup and usage
10. **Tested Features** with manual test results

---

Good luck building the admin dashboard! 🚀
